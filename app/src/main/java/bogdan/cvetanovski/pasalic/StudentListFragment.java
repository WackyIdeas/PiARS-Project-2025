package bogdan.cvetanovski.pasalic;

import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class StudentListFragment extends Fragment {

    public StudentAdapter adapterModel;
    public StudentListFragment() {
        // Required empty public constructor
    }

    public static StudentListFragment newInstance() {
        return new StudentListFragment();
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

        View result = inflater.inflate(R.layout.fragment_student_list, container, false);
        adapterModel = new StudentAdapter(inflater);

        ListView listView = result.findViewById(R.id.studentListView);
        listView.setAdapter(adapterModel);

        tryNotifyEmptyList();
        adapterModel.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                tryNotifyEmptyList();
            }
        });

        return result;
    }
}