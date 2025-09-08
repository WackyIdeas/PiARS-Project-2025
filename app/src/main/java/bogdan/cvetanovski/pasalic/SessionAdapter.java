package bogdan.cvetanovski.pasalic;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

public class SessionAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    Context context;
    protected Session[] model;
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

    public void removeItem(int index) {
        String msg = DatabaseManager.getInstance(context).removeItem(DatabaseManager.SESSIONS_TABLE, DatabaseManager.SessionID, index);
        if(!msg.isEmpty()) Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        populateModel();
        // Update the adapter as the underlying data model is changed
        notifyDataSetChanged();
    }
    public void addItem(Session s) {
        String result = DatabaseManager.getInstance(context).addSession(context, s);
        if(!result.isEmpty()) Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        populateModel();
        notifyDataSetChanged();
    }

    void populateModel() {
        try {
            model = (Session[])DatabaseFactory.getQueryResults(context, DatabaseManager.SESSIONS_TABLE);
        } catch (InvalidTableException e) {
            throw new RuntimeException(e);
        }
    }
    public SessionAdapter(Context c, LayoutInflater inflater) {
        layoutInflater = inflater;
        context = c;
        populateModel();
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
