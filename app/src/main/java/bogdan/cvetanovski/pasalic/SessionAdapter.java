package bogdan.cvetanovski.pasalic;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

public class SessionAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    protected SessionModel model;
    @Override
    public int getCount() {
        return model.size();
    }

    @Override
    public Object getItem(int position) {
        return model.getItem(position, true);
    }

    @Override
    public long getItemId(int position) {
        return model.getItem(position, false).getSessionNumber();
    }

    public void removeItem(int index) {
        model.removeItem(index);
        notifyDataSetChanged();
    }
    public void addItem(int index, String date) {
        model.addItem(index, date);
        notifyDataSetChanged();
    }

    public SessionAdapter(LayoutInflater inflater) {
        layoutInflater = inflater;
        model = new SessionModel();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.session_delegate, parent, false);
        }
        TextView sessionName = convertView.findViewById(R.id.sessionName);
        TextView sessionDate = convertView.findViewById(R.id.sessionDate);
        TextView sessionRelevance = convertView.findViewById(R.id.sessionRelevance);

        String nameStr = convertView.getContext().getResources().getString(R.string.SessionText, model.getItem(position, false).getSessionNumber());
        sessionName.setText(nameStr);
        sessionName.setTag(model.getItem(position, false).getSessionNumber());

        sessionDate.setText(model.getItem(position, false).getSessionDate());

        // Figure out session relevance based on the date
        DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        LocalDate date = LocalDate.parse(model.getItem(position, false).getSessionDate(), dtf);
        LocalDate today = LocalDate.now();

        long daysBetween = ChronoUnit.DAYS.between(today, date);

        String pastStr = convertView.getContext().getResources().getString(R.string.SessionPast);
        String upcomingStr = convertView.getContext().getResources().getString(R.string.SessionUpcoming);
        sessionRelevance.setText(daysBetween < 0 ? pastStr : upcomingStr);

        return convertView;
    }
}
