package bogdan.cvetanovski.pasalic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

public class SessionListFragment extends Fragment {

    // Store the date from the CalendarView because CalendarView
    // has no proper way to provide its currently selected date
    private LocalDate selectedDate;
    Context context;
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
        context = getActivity();
        adapterModel = new SessionAdapter(context, inflater);

        ListView listView = v.findViewById(R.id.sessionListView);
        listView.setAdapter(adapterModel);

        // Open the respective voting results page when an item delegate is tapped
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this.getActivity(), ResultsActivity.class);
            Bundle params = new Bundle();
            params.putInt("sessionID", (int)id);
            intent.putExtras(params);
            startActivity(intent);
        });

        // Build an alert dialog for deletion confirmation
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
            return true; // Important to mark the event as "finished" so that the tap event defined above isn't triggered
        });


        selectedDate = LocalDate.now();
        CalendarView calendar = v.findViewById(R.id.sessionCalendar);
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month+1, dayOfMonth); // Month weirdly enough takes values in the range [0, 11]
        });
        Button submitButton = v.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(view -> {
            // Create a custom dialog form for additional session information
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.session_form);
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            Button cancel = dialog.findViewById(R.id.sessionCancel);
            Button submit = dialog.findViewById(R.id.sessionSubmit);
            EditText sName = dialog.findViewById(R.id.sessionNameTextEdit);
            EditText sDesc = dialog.findViewById(R.id.sessionDescriptionTextEdit);

            cancel.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            submit.setOnClickListener(v1 -> {
                String name = sName.getText().toString().trim();
                String desc = sDesc.getText().toString().trim();
                // Prevent empty inputs for non-null columns
                if(name.isEmpty()) {
                    Toast.makeText(context, getString(R.string.SessionFormError), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    return;
                }

                Session s = new Session();
                s.setDate(selectedDate.toString());
                s.setEndDate(selectedDate.plusWeeks(1).toString());
                s.setName(name);
                s.setDescription(desc);

                adapterModel.addItem(s);
                dialog.dismiss();
            });
            dialog.show();
        });

        adapterModel.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                tryNotifyEmptyList();
            }
        });
        //tryNotifyEmptyList();
        return v;
    }
}
