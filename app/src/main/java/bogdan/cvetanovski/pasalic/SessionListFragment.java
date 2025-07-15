package bogdan.cvetanovski.pasalic;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SessionListFragment extends Fragment {

    public SessionListFragment() {
        // Required empty public constructor
    }

    public static SessionListFragment newInstance() {
        return new SessionListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_session_list, container, false);
    }
}