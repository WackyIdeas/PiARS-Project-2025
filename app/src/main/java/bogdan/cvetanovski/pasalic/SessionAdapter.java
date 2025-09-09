package bogdan.cvetanovski.pasalic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SessionAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    Context context;
    protected Session[] model;

    // Since SessionAdapter is used in multiple places, allow session
    // filtering
    String selectionFilter;
    String[] selectionFilterArgs;

    public void setSelectionFilterArgs(String[] args) {
        selectionFilterArgs = args;
        // Populate model again and update the adapter
        populateModel(selectionFilter, selectionFilterArgs);
        notifyDataSetChanged();
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

    // Helper function to remove sessions from the database
    public void removeItem(int index) {
        String msg = DatabaseManager.getInstance(context).removeItem(DatabaseManager.SESSIONS_TABLE, DatabaseManager.SessionID, index);
        if(!msg.isEmpty()) Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        populateModel(selectionFilter, selectionFilterArgs);
        // Update the adapter as the underlying data model is changed
        notifyDataSetChanged();
    }
    // Helper function to add sessions to the database
    public void addItem(Session s) {
        String result = DatabaseManager.getInstance(context).addSession(context, s);
        if(!result.isEmpty()) Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        populateModel(selectionFilter, selectionFilterArgs);
        notifyDataSetChanged();
    }

    void populateModel(String selection, String[] selectionArgs) {
        try {
            model = (Session[])DatabaseFactory.getQueryResults(context, DatabaseManager.SESSIONS_TABLE, selection, selectionArgs);
        } catch (InvalidTableException e) {
            throw new RuntimeException(e);
        }
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

        sessionDate.setText(model[position].getDate());

        LocalDate date = LocalDate.parse(model[position].getDate());
        LocalDate today = LocalDate.now();

        long daysBetween = ChronoUnit.DAYS.between(today, date);

        String pastStr = convertView.getContext().getResources().getString(R.string.SessionPast);
        String upcomingStr = convertView.getContext().getResources().getString(R.string.SessionUpcoming);
        sessionRelevance.setText(daysBetween < 0 ? pastStr : upcomingStr);

        return convertView;
    }
}
