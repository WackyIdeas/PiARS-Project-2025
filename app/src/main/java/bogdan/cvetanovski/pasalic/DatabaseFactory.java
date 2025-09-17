package bogdan.cvetanovski.pasalic;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/*
 * Factory class for fetching data from the database.
 * Returns an array of objects corresponding to rows of the resulting query,
 * based on selection filters (WHERE clauses).
 *
 * Throws an InvalidTableException if passing an unknown table name.
 * The table name should always be fetched from DatabaseManager.
 *
 * TODO: Implement sorting
 */
public class DatabaseFactory {
    public static ObjectRow[] getQueryResults(Context c, String table, String selection, String[] selectionArgs, String orderBy) throws InvalidTableException {
        SQLiteDatabase db = DatabaseManager.getInstance(c).getReadableDatabase();

        Cursor cursor;
        cursor = db.query(table, null, selection, selectionArgs, null, null, orderBy);

        switch (table) {
            case DatabaseManager.USERS_TABLE:
                User[] users = new User[cursor.getCount()];
                try {
                    cursor.moveToFirst();
                    do {
                        int i = cursor.getPosition();
                        users[i] = new User();
                        users[i].setId(cursor.getInt(0));
                        users[i].setName(cursor.getString(1));
                        users[i].setSurname(cursor.getString(2));
                        users[i].setUsername(cursor.getString(3));
                        users[i].setHash(cursor.getString(4));
                        users[i].setRole(cursor.getInt(5));
                    } while (cursor.moveToNext());
                } catch (Exception e) {
                    System.err.print("Exception: ");
                    System.err.println(e.getMessage());
                    return null;
                } finally {
                    if (!cursor.isClosed()) cursor.close();
                }
                return users;
            case DatabaseManager.SESSIONS_TABLE:
                Session[] sessions = new Session[cursor.getCount()];
                try {
                    cursor.moveToFirst();
                    do {
                        int i = cursor.getPosition();
                        sessions[i] = new Session();
                        sessions[i].setId(cursor.getInt(0));
                        sessions[i].setSessionHexID(cursor.getString(1));
                        sessions[i].setDate(cursor.getString(2));
                        sessions[i].setName(cursor.getString(3));
                        sessions[i].setDescription(cursor.getString(4));
                        sessions[i].setEndDate(cursor.getString(5));
                    } while (cursor.moveToNext());
                } catch (Exception e) {
                    System.err.print("Exception: ");
                    System.err.println(e.getMessage());
                    return null;
                } finally {
                    if (!cursor.isClosed()) cursor.close();
                }
                return sessions;
            case DatabaseManager.VOTES_TABLE:
                Vote[] votes = new Vote[cursor.getCount()];
                try {
                    cursor.moveToFirst();
                    do {
                        int i = cursor.getPosition();
                        votes[i] = new Vote();
                        votes[i].setId(cursor.getInt(0));
                        votes[i].setVotesYes(cursor.getInt(1));
                        votes[i].setVotesNo(cursor.getInt(2));
                        votes[i].setVotesAbstain(cursor.getInt(3));
                        votes[i].setSessionId(cursor.getInt(4));
                    } while (cursor.moveToNext());
                } catch (Exception e) {
                    System.err.print("Exception: ");
                    System.err.println(e.getMessage());
                    return null;
                } finally {
                    if (!cursor.isClosed()) cursor.close();
                }
                return votes;
            case DatabaseManager.VOTESAFETY_TABLE:
                VoteSafety[] voteHashes = new VoteSafety[cursor.getCount()];
                try {
                    cursor.moveToFirst();
                    do {
                        int i = cursor.getPosition();
                        voteHashes[i] = new VoteSafety();
                        voteHashes[i].setId(cursor.getInt(0));
                        voteHashes[i].setHash(cursor.getString(1));
                        voteHashes[i].setVoteID(cursor.getInt(2));
                    } while (cursor.moveToNext());
                } catch (Exception e) {
                    System.err.print("Exception: ");
                    System.err.println(e.getMessage());
                    return null;
                } finally {
                    if (!cursor.isClosed()) cursor.close();
                }
                return voteHashes;
            default:
                if (!cursor.isClosed()) cursor.close();
                throw new InvalidTableException("Invalid table name!");
        }
    }
}
