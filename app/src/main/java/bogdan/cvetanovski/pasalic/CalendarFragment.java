package bogdan.cvetanovski.pasalic;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
        // Add event handler for OnDateChangeListener
        // https://developer.android.com/reference/android/widget/CalendarView.OnDateChangeListener
        CalendarView cv = v.findViewById(R.id.calendarView);
        cv.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            /*
             * For now, assume that any session has a time limit of
             * 2 weeks. Use ChronoUnit to calculate difference between
             * two dates.
             * https://docs.oracle.com/javase/8/docs/api/java/time/temporal/ChronoUnit.html
             */
            LocalDate date = LocalDate.of(year, month+1, dayOfMonth);
            LocalDate expiryDate = date.plusWeeks(2);
            long daysBetween = ChronoUnit.DAYS.between(date, expiryDate);
            // Use DateTimeFormatter to get localized date format instead of ISO date
            DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

            Intent intent = new Intent(getActivity(), DecideActivity.class);
            Bundle params = new Bundle();
            // Placeholder values
            params.putLong("daysLeft", daysBetween);
            params.putInt("sessionNumber", 1);
            params.putString("sessionDate", date.format(dtf));
            params.putString("description", "Sample description");
            intent.putExtras(params);
            startActivity(intent);
        });
        return v;
    }
}