package bogdan.cvetanovski.pasalic;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StudentListFragment extends Fragment {

    public StudentListFragment() {
        // Required empty public constructor
    }

    public static StudentListFragment newInstance() {
        return new StudentListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_list, container, false);
    }
}