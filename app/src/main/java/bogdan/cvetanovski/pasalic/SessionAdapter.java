package bogdan.cvetanovski.pasalic;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

public class SessionAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    Context context;
    protected Session[] model;

    // Since SessionAdapter is used in multiple places, allow session
    // filtering
    String selectionFilter;
    String[] selectionFilterArgs;

    private Handler handler = new Handler(Looper.getMainLooper());

    public void setSelectionFilterArgs(String[] args) {
        selectionFilterArgs = args;
        // Populate model again and update the adapter
        populateModel(selectionFilter, selectionFilterArgs);
    }

    @Override
    public int getCount() {
        if(model == null) return 0;
        return model.length;
    }

    @Override
    public Object getItem(int position) {
        if(model == null) return null;
        return model[position];
    }

    @Override
    public long getItemId(int position) {
        if(model == null) return -1;
        return model[position].getId();
    }

    /*
    // Helper function to remove sessions from the database
    public void removeItem(int index) {
        String msg = DatabaseManager.getInstance(context).removeItem(DatabaseManager.SESSIONS_TABLE, DatabaseManager.SessionID, index);
        if(!msg.isEmpty()) Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        populateModel(selectionFilter, selectionFilterArgs);
        // Update the adapter as the underlying data model is changed
        notifyDataSetChanged();
    }*/
    // Helper function to add sessions to the database
    public void addItem(Session s) {
        new Thread(() -> {
            String result = DatabaseManager.getInstance(context).addSession(s);
            if(!result.isEmpty()) handler.post(() -> { Toast.makeText(context, result, Toast.LENGTH_LONG).show(); });
            populateModel(selectionFilter, selectionFilterArgs);
        }).start();
    }
    /*
     * We need to synchronize the SQLite database with the MongoDB
     * server, which needs to be run on a separate thread. The problem
     * with this is that subsequent UI updates after interacting with
     * the HTTP server cannot run on threads that aren't the main UI
     * rendering loop.
     *
     * To fix this, use the "Main Looper" which represents the main
     * rendering thread, where any and all UI updates should be
     * directed to.
     *
     */
    void populateModel(String selection, String[] selectionArgs) {
        model = new Session[0];
        handler.post(this::notifyDataSetChanged);
        new Thread(() -> {
            try {
                String result = DatabaseManager.getInstance(context).synchronizeDatabase();
                if(!result.isEmpty()) {
                    handler.post(() -> { Toast.makeText(context, result, Toast.LENGTH_LONG).show(); });
                    return;
                }
                model = (Session[])DatabaseFactory.getQueryResults(context, DatabaseManager.SESSIONS_TABLE, selection, selectionArgs, DatabaseManager.Date + " ASC");

                handler.post(() -> {
                    if(model != null && model.length == 0) {
                        String toastText = context.getResources().getString(R.string.EmptyListIndicator);
                        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
                    }
                    notifyDataSetChanged();
                });
            } catch (InvalidTableException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
    public SessionAdapter(Context c, LayoutInflater inflater, String selection, String[] selectionArgs) {
        layoutInflater = inflater;
        context = c;
        selectionFilter = selection;
        selectionFilterArgs = selectionArgs;
        populateModel(selectionFilter, selectionFilterArgs);
    }

    public SessionAdapter(Context c, LayoutInflater inflater) {
        layoutInflater = inflater;
        context = c;
        populateModel(selectionFilter, selectionFilterArgs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.session_delegate, parent, false);
        }
        TextView sessionName = convertView.findViewById(R.id.sessionName);
        TextView sessionDate = convertView.findViewById(R.id.sessionDate);
        TextView sessionRelevance = convertView.findViewById(R.id.sessionRelevance);

        sessionName.setText(model[position].getName());
        sessionName.setTag(model[position].getId());

        // Use custom DateTimeFormatter to parse ISO 8601 which is the datetime format used by both SQLite and MongoDB
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        DateTimeFormatter readableFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        LocalDateTime sDate = LocalDateTime.parse(model[position].getDate(), fmt);
        sessionDate.setText(sDate.format(readableFormat));

        LocalDateTime datetime = LocalDateTime.parse(model[position].getEndDate(), fmt);
        LocalDateTime datetimetoday = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(datetimetoday, datetime);

        String pastStr = convertView.getContext().getResources().getString(R.string.SessionPast);
        String upcomingStr = convertView.getContext().getResources().getString(R.string.SessionUpcoming);
        sessionRelevance.setText(daysBetween < 0 ? pastStr : upcomingStr);

        return convertView;
    }
}
