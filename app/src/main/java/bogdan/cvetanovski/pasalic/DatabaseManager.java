package bogdan.cvetanovski.pasalic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "decideit_table";
    private static final int DATABASE_VERSION = 1;

    public static final String USERS_TABLE = "Users";
    public static final String SESSIONS_TABLE = "Sessions";
    public static final String VOTES_TABLE = "Votes";

    // User table
    public static final String UserID = "UserID";
    public static final String User_Name = "Name";
    public static final String Surname = "Surname";
    public static final String Username = "Username";
    public static final String Password = "Password";
    public static final String Role = "Role";

    public static final int STUDENT_ROLE = 0;
    public static final int ADMIN_ROLE = 1;

    // Sessions table
    public static final String SessionID = "SessionID";
    public static final String Date = "Date";
    public static final String Session_Name = "Name";
    public static final String Description = "Description";
    public static final String EndDate = "EndDate";

    // Votes table
    public static final String VoteID = "VoteID";
    public static final String VotesYes = "VotesYes";
    public static final String VotesNo = "VotesNo";
    public static final String VotesAbstain = "VotesAbstain";


    // Use Singleton design pattern for
    private static DatabaseManager sInstance;

    /*
        SQLite commands:

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

     */

    public static synchronized DatabaseManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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

        db.execSQL(userTable);
        db.execSQL(sessionTable);
        db.execSQL(votesTable);
    }

    public String registerUser(User u) {
        String msg = "";
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues q = new ContentValues();
            q.putNull(UserID);
            q.put(User_Name, u.getName());
            q.put(Surname, u.getSurname());
            q.put(Username, u.getUsername());
            q.put(Password, hash(u.getHash()));
            q.put(Role, u.getRole());
            db.insertOrThrow(USERS_TABLE, null, q);
        } catch (SQLException e) { // Rely on database for sanity checks
            msg = e.getMessage();
        }
        return msg;
    }
    // Role value of -1 is assumed to be a failed login
    public Pair<String, Integer> login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String h = hash(password);
        Cursor cursor = db.query(USERS_TABLE, new String[]{User_Name, Surname, Role}, Username + "=? AND " + Password + "=?",
                            new String[]{username, h}, null, null, null);

        String name_surname = "";
        int role = -1;
        if(cursor.getCount() == 1) { // We expect exactly one match for a correctly created user
            cursor.moveToFirst();
            String name = cursor.getString(0);
            String surname = cursor.getString(1);
            name_surname += name;
            if(!surname.isEmpty()) name_surname += " " + surname;
            role = cursor.getInt(2);
        }
        if(!cursor.isClosed()) cursor.close();
        return new Pair<String, Integer>(name_surname, role);
    }

    public String removeItem(String table, String idQual, int id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(table, idQual+"="+String.valueOf(id), null);
            return "";
        } catch (SQLiteException e) {
            return e.getMessage();
        }
    }

    public String addSession(Context c, Session s) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // Check if session with the same name already exists for the given date
            Cursor cursor = db.query(SESSIONS_TABLE, null, Session_Name+"=? AND "+Date+"=?",
                                     new String[]{ s.getName(), s.getDate() }, null, null, null);
            // If one already exists, throw error
            if(cursor.getCount() > 0) {
                if(!cursor.isClosed()) cursor.close();
                return c.getString(R.string.SessionFormExistsError);
            }
            if(!cursor.isClosed()) cursor.close();

            ContentValues q = new ContentValues();
            q.putNull(SessionID);
            q.put(Session_Name, s.getName());
            q.put(Date, s.getDate());
            q.put(EndDate, s.getEndDate());
            q.put(Description, s.getDescription());
            db.insert(SESSIONS_TABLE, null, q);
            // Create a matching votes table
            db.execSQL("INSERT INTO " + VOTES_TABLE + " VALUES(NULL, 0, 0, 0, last_insert_rowid())");
            return "";
        } catch (SQLiteException e) {
            return e.getMessage();
        }
    }

    public Vote getResults(int sessionID) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.query(VOTES_TABLE, null, SessionID+"="+String.valueOf(sessionID), null, null, null, null);
            if(cursor.getCount() == 0) {
                if(!cursor.isClosed()) cursor.close();
                return null;
            }
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + VOTES_TABLE); // Drop Votes first due to FK constraint
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
        MessageDigest md = null;
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


