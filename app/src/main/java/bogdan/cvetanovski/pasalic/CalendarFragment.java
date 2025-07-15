package bogdan.cvetanovski.pasalic;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    public CalendarFragment() {
        // Required empty public constructor
    }

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
        CalendarView cv = v.findViewById(R.id.calendarView);
        cv.setOnDateChangeListener((view, year, month, dayOfMonth) -> {

            LocalDate date = LocalDate.of(year, month, dayOfMonth);

            LocalDate expiryDate = LocalDate.of(year, month, dayOfMonth);
            expiryDate = expiryDate.plusWeeks(2);
            long daysBetween = ChronoUnit.DAYS.between(date, expiryDate);

            Intent intent = new Intent(getActivity(), DecideActivity.class);
            Bundle params = new Bundle();
            // Placeholder values
            params.putLong("daysLeft", daysBetween);
            params.putInt("sessionNumber", 1);
            params.putString("sessionDate", date.toString());
            params.putString("description", "Sample description");
            intent.putExtras(params);
            startActivity(intent);
        });
        return v;
    }
}