package bogdan.cvetanovski.pasalic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * This is the database helper class which manages the
 * underlying local database. This class also manages
 * session information
 */
public class DatabaseManager extends SQLiteOpenHelper {

    // Database filename as stored in /data/data/package.name/databases
    private static final String DATABASE_NAME = "decideit_table";
    private static final int DATABASE_VERSION = 1;

    // Names of all tables in the database
    public static final String USERS_TABLE = "Users";
    public static final String SESSIONS_TABLE = "Sessions";
    public static final String VOTES_TABLE = "Votes";
    public static final String VOTESAFETY_TABLE = "VoteSafety";

    // User table columns
    public static final String UserID = "UserID";
    public static final String User_Name = "Name";
    public static final String Surname = "Surname";
    public static final String Username = "Username";
    public static final String Password = "Password";
    public static final String Role = "Role";

    public static final int STUDENT_ROLE = 0;
    public static final int ADMIN_ROLE = 1;

    // Sessions table columns
    public static final String SessionID = "SessionID";
    public static final String SessionHexID = "SessionHexID";
    public static final String Date = "Date";
    public static final String Session_Name = "Name";
    public static final String Description = "Description";
    public static final String EndDate = "EndDate";

    // Votes table columns
    public static final String VoteID = "VoteID";
    public static final String VotesYes = "VotesYes";
    public static final String VotesNo = "VotesNo";
    public static final String VotesAbstain = "VotesAbstain";

    // VoteSafety table columns
    public static final String VoteSafetyID = "VoteSafetyID";
    public static final String Hash = "Hash";

    /*
     * Store an intermediate hash used for vote spam prevention.
     * See generateIntermediateUserID(String, String) for more details.
     */
    public void resetCredentials() {
        intermediateUserID = "";
    }

    private String intermediateUserID = "";

    /*
        SQLite commands for creating the relevant tables.

        Notes:
            Users:
            - Surname is optional
            - The role is limited to values [0, 1], 0 meaning student,
              1 meaning admin role.
            Sessions:
            - Use CHECK constraint to ensure the EndDate doesn't come before Date
            - Store MongoDB ID using SessionHexID
            Votes:
            - VotesYes, VotesNo, VotesAbstain are all non-negative.
            - Establish foreign key relationship between Votes and Sessions
            - Also delete all corresponding rows in cascade to a session being
              deleted
            VoteSafety:
            - Establish foreign key relationship between VoteSafety and Votes
            - Also delete all corresponding rows in cascade to a vote row being
              deleted

        CREATE TABLE Users (
            UserID INTEGER PRIMARY KEY,
            Name TEXT NOT NULL,
            Surname TEXT,
            Username TEXT NOT NULL UNIQUE,
            Password TEXT NOT NULL,
            Role INTEGER NOT NULL CHECK(Role >= 0 AND Role < 2)
        );

        CREATE TABLE Sessions (
             SessionID INTEGER PRIMARY KEY,
             SessionHexID TEXT NOT NULL UNIQUE,
             Date TEXT NOT NULL,
             Name TEXT NOT NULL,
             Description TEXT,
             EndDate TEXT NOT NULL,
             CHECK(Date < EndDate)
        );

        CREATE TABLE Votes (
             VoteID INTEGER PRIMARY KEY,
             VotesYes INTEGER NOT NULL CHECK(VotesYes >= 0),
             VotesNo INTEGER NOT NULL CHECK(VotesNo >= 0),
             VotesAbstain INTEGER NOT NULL CHECK(VotesAbstain >= 0),
             SessionID INTEGER NOT NULL UNIQUE,
             FOREIGN KEY (SessionID) REFERENCES Sessions(SessionID) ON DELETE CASCADE
        );

        CREATE TABLE VoteSafety (
             VoteSafetyID INTEGER PRIMARY KEY,
             Hash TEXT NOT NULL UNIQUE,
             VoteID INTEGER NOT NULL,
             FOREIGN KEY (VoteID) REFERENCES Votes(VoteID) ON DELETE CASCADE
        );

     */

    /*
     * Use Singleton design pattern for accessing database functionality across
     * the codebase. Use synchronized keyword to prevent potential thread-related
     * issues.
     */
    private static DatabaseManager dbInstance;
    public static synchronized DatabaseManager getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DatabaseManager(context.getApplicationContext());
        }
        return dbInstance;
    }

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
         * This runs only when the application determines that
         * there is no database file in /data/data/package.name/databases
         */
        String userTable =
                "CREATE TABLE " + USERS_TABLE + " (" +
                    UserID + " INTEGER PRIMARY KEY," +
                    User_Name  + " TEXT NOT NULL," +
                    Surname + " TEXT," +
                    Username + " TEXT NOT NULL UNIQUE," +
                    Password + " TEXT NOT NULL," +
                    Role + " INTEGER NOT NULL CHECK(" + Role + " >= 0 AND " + Role + " < 2));";
        String sessionTable =
               "CREATE TABLE "+ SESSIONS_TABLE +" (" +
                    SessionID + " INTEGER PRIMARY KEY," +
                    SessionHexID + " TEXT NOT NULL UNIQUE," +
                    Date + " TEXT NOT NULL," +
                    Session_Name + " TEXT NOT NULL," +
                    Description + " TEXT," +
                    EndDate + " TEXT NOT NULL," +
                    "CHECK("+ Date +" < "+ EndDate +"));";
        String votesTable =
                "CREATE TABLE "+ VOTES_TABLE +" (" +
                    VoteID + " INTEGER PRIMARY KEY," +
                    VotesYes + " INTEGER NOT NULL CHECK("+ VotesYes +" >= 0)," +
                    VotesNo + " INTEGER NOT NULL CHECK("+ VotesNo +" >= 0)," +
                    VotesAbstain + " INTEGER NOT NULL CHECK("+ VotesAbstain +" >= 0)," +
                    SessionID + " INTEGER NOT NULL UNIQUE," +
                    "FOREIGN KEY ("+SessionID+") REFERENCES "+SESSIONS_TABLE+"("+SessionID+") ON DELETE CASCADE);";
        String voteSafetyTable =
                "CREATE TABLE "+ VOTESAFETY_TABLE +" (" +
                    VoteSafetyID + " INTEGER PRIMARY KEY," +
                    Hash + " TEXT NOT NULL UNIQUE," +
                    VoteID + " INTEGER NOT NULL," +
                    "FOREIGN KEY ("+ VoteID +") REFERENCES "+VOTES_TABLE+"("+VoteID+") ON DELETE CASCADE);";

        db.execSQL(userTable);
        db.execSQL(sessionTable);
        db.execSQL(votesTable);
        db.execSQL(voteSafetyTable);
    }

    /*
     * Get the session and vote information from the HTTP server.
     * As every session needs to have a followup row in the votes table,
     * and the expressjs server returns sessions and votes in the correct
     * mutual order, we can use the same index to iterate through the JSON
     * array response. We can interrogate the local SQLite database for
     * every session, and insert new sessions, while updating existing rows.
     *
     * This is done in order to reduce the number of HTTP requests sent to
     * the server.
     */
    public String synchronizeDatabase() {
        JSONArray sessions;
        JSONArray votes;
        try {
            sessions = HttpHelper.getJSONArrayFromURL("sessions");
            votes = HttpHelper.getJSONArrayFromURL("votes");
        } catch (JSONException | IOException e) {
            return e.getMessage();
        }
        if(sessions == null || votes == null) {
            return "Invalid response from server";
        }
        SQLiteDatabase db = getWritableDatabase();
        // Make a strong assumption that len(sessions) == len(votes)
        // As the expressjs server will create a new vote entry for every session
        if(sessions.length() != votes.length()) {
            return "Possibly corrupt MongoDB database!";
        }
        Cursor cursor = null;
        try {
            // For every session and vote, check if they exist in the local database
            for(int i = 0; i < sessions.length(); i++) {
                JSONObject sessionObj = sessions.getJSONObject(i);
                JSONObject voteObj = votes.getJSONObject(i);
                String sessionMongoID = voteObj.getString("sessionId");
                // Check to see if the corresponding session exists, and return the corresponding Vote row ID if so
                cursor = db.rawQuery("SELECT "+ SESSIONS_TABLE +"."+SessionHexID+", "+VoteID+" FROM "
                                        +SESSIONS_TABLE+" JOIN "+VOTES_TABLE+" ON "
                                        +SESSIONS_TABLE+"."+SessionID+" = "+VOTES_TABLE+"."+SessionID+" WHERE "+SessionHexID+" = ?",
                                     new String[] { sessionMongoID });
                ContentValues q = new ContentValues();
                q.put(SessionHexID, sessionMongoID);
                q.put(Session_Name, sessionObj.getString("sessionName"));
                q.put(Date, sessionObj.getString("date"));
                q.put(EndDate, sessionObj.getString("endOfVotingTime"));
                q.put(Description, sessionObj.getString("description"));

                if(cursor.getCount() == 0) {
                    // Doesn't exist, insert it into the database
                    // No need to error check here, as the MongoDB data should be valid already
                    q.putNull(SessionID);
                    db.insertOrThrow(SESSIONS_TABLE, null, q);
                    // Create a matching votes table, use last_insert_rowid() to get the ID of the newly inserted session
                    // Use raw SQL query for this
                    String yes = String.valueOf(voteObj.getInt("yes"));
                    String no = String.valueOf(voteObj.getInt("no"));
                    String abstain = String.valueOf(voteObj.getInt("abstain"));
                    db.execSQL("INSERT INTO " + VOTES_TABLE + " VALUES(NULL, "+yes+", "+no+", "+abstain+", last_insert_rowid())");
                } else {
                    // The session already exists, update the session
                    db.update(SESSIONS_TABLE, q, SessionHexID + " = ?", new String[] { sessionMongoID });
                    // Update the corresponding vote
                    cursor.moveToFirst();
                    String voteID = String.valueOf(cursor.getInt(1));
                    ContentValues v = new ContentValues();
                    v.put(VotesYes, voteObj.getInt("yes"));
                    v.put(VotesNo, voteObj.getInt("no"));
                    v.put(VotesAbstain, voteObj.getInt("abstain"));
                    db.update(VOTES_TABLE, v, VoteID + " = ?", new String[]{ voteID });
                }
                if(!cursor.isClosed())
                    cursor.close();
            }
        } catch(JSONException e) {
            return e.getMessage();
        } catch(SQLiteException e) {
            if(cursor != null && !cursor.isClosed()) cursor.close();
            return e.getMessage();
        }
        return "";
    }
    /*
     * Attempt to register the user to the database.
     * Return String as a potential message for UI elements to
     * display to the user via Toasts or other means.
     *
     * This query can fail in the following cases:
     * - Providing null values for username, name, password, etc.
     * - Providing a username that already exists (UNIQUE constraint)
     * - Generic database errors (bad permissions, etc.)
     *
     */
    public String registerUser(User u) {
        String msg = "";
        SQLiteDatabase db = getWritableDatabase();
        try {
            // Use ContentValues to provide data to the insert query
            ContentValues q = new ContentValues();
            q.putNull(UserID); // Let SQLite figure out the new ID for the user
            q.put(User_Name, u.getName());
            q.put(Surname, u.getSurname());
            q.put(Username, u.getUsername());
            q.put(Password, hash(u.getHash())); // Store password as a SHA-256 hash
            q.put(Role, u.getRole());
            db.insertOrThrow(USERS_TABLE, null, q);
        } catch (SQLException e) { // Rely on database for sanity checks
            msg = e.getMessage();
        }
        return msg;
    }
    /*
     * Generate an intermediate hash that will be used to verify that the user has
     * already voted, with the goal to make it very difficult to trace back the
     * cast vote to the corresponding user. This is an intermediate hash value as
     * the final hash stored in the database will additionally rely on the vote ID
     * as well.
     *
     * result = hash( hash(|username| + username) + hash(|pass| + pass) + voteID )
     *
     * We use the vote ID in order to further alter the hash for every
     * individual vote, making it more difficult to identify the user.
     *
     * Additionally, the extra table doesn't store information about the vote
     * decision.
     *
     */
    private String generateIntermediateUserID(String s1, String s2) {
        String q = s1.length() + s1;
        String p = s2.length() + s2;
        return hash(q) + hash(p);
    }
    /*
     * Attempt to authenticate the user at the login activity.
     * On success, return Name+Surname and the role of the user,
     * which will decide the application's next activity.
     *
     * On failure, return an empty string and -1, the login activity
     * should notify the user that authentication has failed.
     *
     */
    public Pair<String, Integer> login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String h = hash(password);
        // Cursor lets us go through the result of the query
        Cursor cursor = db.query(USERS_TABLE, new String[]{User_Name, Surname, Role, UserID}, Username + "=? AND " + Password + "=?",
                            new String[]{username, h}, null, null, null);

        String name_surname = "";
        int role = -1;
        if(cursor.getCount() == 1) { // We expect exactly one match for a correctly created user
            cursor.moveToFirst();
            String name = cursor.getString(0);
            String surname = cursor.getString(1);
            name_surname += name;
            if(!surname.isEmpty()) name_surname += " " + surname; // Surname may be empty
            role = cursor.getInt(2);
            intermediateUserID = generateIntermediateUserID(username, password); // Generate intermediate hash value on login
        }
        if(!cursor.isClosed()) cursor.close(); // Cursor needs to be closed afterwards
        return new Pair<>(name_surname, role);
    }

    // Remove any item from the corresponding table, based on the item's ID
    public String removeItem(String table, String idQual, int id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(table, idQual+"="+ id, null);
            return "";
        } catch (SQLiteException e) {
            return e.getMessage();
        }
    }

    // Send HTTP POST request with appropriate session information
    // The local SQLite database will be automatically updated using the
    // SessionAdapter
    public String addSession(Session s) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("sessionName", s.getName());
            msg.put("description", s.getDescription());
            msg.put("endOfVotingTime", s.getEndDate());
            msg.put("date", s.getDate());
            HttpHelper.postJSONObjectFromURL("session", msg);
        } catch (IOException | JSONException e) {
            return e.getMessage();
        }
        return "";
    }

    // Get vote results for a given session
    public Vote getResults(int sessionID) {
        try {
            synchronizeDatabase();
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(VOTES_TABLE, null, SessionID+"="+ sessionID, null, null, null, null);
            if(cursor.getCount() == 0) {
                if(!cursor.isClosed()) cursor.close();
                return null;
            }
            // There can only be one vote for a given session (1:1 relationship)
            Vote v = new Vote();
            cursor.moveToFirst();
            v.setId(cursor.getInt(0));
            v.setVotesYes(cursor.getInt(1));
            v.setVotesNo(cursor.getInt(2));
            v.setVotesAbstain(cursor.getInt(3));
            v.setSessionId(cursor.getInt(4));
            if(!cursor.isClosed()) cursor.close();
            return v;
        } catch (SQLiteException e) {
            return null;
        }
    }

    // Use enum to avoid having to handle error cases for unknown values
    public enum VoteDecision { YES, NO, ABSTAIN }
    public String castVote(String sessionHexID, int voteID, VoteDecision decision) {
        SQLiteDatabase db = getWritableDatabase();

        String columnName = "";
        switch(decision) {
            case YES:
                columnName = "yes";
                break;
            case NO:
                columnName = "no";
                break;
            case ABSTAIN:
                columnName = "abstain";
                break;
        }
        // POST to HTTP server
        try {
            JSONObject msg = new JSONObject();
            msg.put("vote", columnName);
            msg.put("sessionId", sessionHexID);
            HttpHelper.postJSONObjectFromURL("results/vote", msg);
        } catch (IOException | JSONException e) {
            return e.getMessage();
        }

        // Generate unique vote ID to prevent user from voting multiple times
        String h = hash(intermediateUserID + voteID);
        ContentValues q = new ContentValues();
        q.putNull(VoteSafetyID);
        q.put(Hash, h);
        q.put(VoteID, voteID);
        try {
            // Should throw exception if a vote with the same hash already exists in the database
            // The exception shouldn't happen in normal circumstances
            db.insertOrThrow(VOTESAFETY_TABLE, null, q);
            return "";
        } catch (SQLiteException e) {
            return e.getMessage();
        }
    }

    /*
     * Check if the user can vote for a given vote/session.
     * Returns true if the user hasn't previously voted on a given
     * session.
     */
    public boolean canVote(Context c, int voteID) {
        String h = hash(intermediateUserID + voteID);
        try {
            VoteSafety[] results = (VoteSafety[])DatabaseFactory.getQueryResults(c, VOTESAFETY_TABLE,
                    VoteID + "=" + voteID + " AND " + Hash + "=?", new String[] {h}, null);
            if(results == null) return true;
            return results.length == 0;
        } catch (SQLiteException e) {
            System.err.println(e.getMessage());
            return false;
        } catch (InvalidTableException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Runs when the database on the filesystem doesn't match the
        // version defined in this class.
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + VOTESAFETY_TABLE); // Drop tables in reverse order to reflect FK constraints
            db.execSQL("DROP TABLE IF EXISTS " + VOTES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SESSIONS_TABLE);
            onCreate(db);
        }
    }

    // We need to enable foreign key constraints for SQLite, as they're not enabled by default.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public static String hash(String s) {
        // Use SHA-256 via MessageDigest, which is always supported on any Java implementation
        // https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] h = md.digest(s.getBytes(StandardCharsets.UTF_8));
        // Convert byte array to hex string for the final result
        char[] result = new char[h.length * 2]; // Avoid String concatenation/builder
        String hexdigits = "0123456789ABCDEF";
        for(int i = 0; i < h.length; i++) {
            char val = (char)(h[i] & 0xFF); // Force interpret the value as unsigned, since Java doesn't directly support unsigned values
            result[i * 2] = hexdigits.charAt(val >>> 4); // 0xF0 portion, shifted by 4 bits
            result[i * 2 + 1] = hexdigits.charAt(val & 0x0F); // 0x0F portion
        }
        return new String(result);
    }
}


