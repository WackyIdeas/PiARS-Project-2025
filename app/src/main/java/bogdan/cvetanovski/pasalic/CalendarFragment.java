package bogdan.cvetanovski.pasalic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ListView;

import java.time.LocalDate;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    public CalendarFragment() {
        // Required empty public constructor
    }
    public SessionAdapter adapterModel;
    Context context;
    private LocalDate selectedDate;

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);
        // Keep reference for other parts of the code
        context = getActivity();
        selectedDate = LocalDate.now();
        // Show only sessions for the selected day.
        adapterModel = new SessionAdapter(context, inflater, DatabaseManager.Date+"=?", new String[]{ selectedDate.toString() });
        ListView listView = v.findViewById(R.id.sessionListView);
        listView.setAdapter(adapterModel);

        // Add event handler for OnDateChangeListener
        // https://developer.android.com/reference/android/widget/CalendarView.OnDateChangeListener
        CalendarView calendar = v.findViewById(R.id.calendarView);
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month+1, dayOfMonth); // Month weirdly enough takes values in the range [0, 11]
            adapterModel.setSelectionFilterArgs(new String[]{ selectedDate.toString() });
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this.getActivity(), DecideActivity.class);
            Bundle params = new Bundle();
            Session s = (Session)adapterModel.getItem(position);
            params.putInt("sessionID", (int)id);
            params.putString("description", s.getDescription());
            params.putString("sessionName", s.getName());
            params.putString("sessionDate", s.getDate());
            params.putString("endDate", s.getEndDate());
            intent.putExtras(params);
            startActivity(intent);
        });

        return v;
    }
}