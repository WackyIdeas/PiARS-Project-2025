package bogdan.cvetanovski.pasalic;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class SessionListFragment extends Fragment {

    private LocalDate selectedDate;
    public SessionAdapter adapterModel;
    public SessionListFragment() {
        // Required empty public constructor
    }

    public static SessionListFragment newInstance() {
        return new SessionListFragment();
    }

    public void tryNotifyEmptyList() {
        if(adapterModel.getCount() == 0) {
            String toastText = getResources().getString(R.string.EmptyListIndicator);
            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_session_list, container, false);
        adapterModel = new SessionAdapter(inflater);

        ListView listView = v.findViewById(R.id.sessionListView);
        listView.setAdapter(adapterModel);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this.getActivity(), ResultsActivity.class);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            String title = getResources().getString(R.string.DeleteConfirmationTitle);
            String text = getResources().getString(R.string.DeleteConfirmationText);
            String stringYes = getResources().getString(R.string.VoteYes);
            String stringNo = getResources().getString(R.string.VoteNo);
            builder.setTitle(title);
            builder.setMessage(text);
            builder.setCancelable(true);
            builder.setPositiveButton(stringYes, (dialog, which) -> {
                TextView sessionName = view.findViewById(R.id.sessionName);
                adapterModel.removeItem((int)sessionName.getTag());
            });
            builder.setNegativeButton(stringNo, (dialog, which) -> dialog.cancel());
            AlertDialog d = builder.create();
            d.show();
            return true;
        });

        tryNotifyEmptyList();
        adapterModel.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                tryNotifyEmptyList();
            }
        });

        selectedDate = LocalDate.now();
        CalendarView calendar = v.findViewById(R.id.sessionCalendar);
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month+1, dayOfMonth); // Month for some reason takes values in the range [0, 11]
        });
        Button submitButton = v.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(view -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            adapterModel.addItem(adapterModel.getCount()+1, selectedDate.format(dtf));
        });

        return v;
    }
}