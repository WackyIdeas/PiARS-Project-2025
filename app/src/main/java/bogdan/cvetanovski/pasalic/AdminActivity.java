package bogdan.cvetanovski.pasalic;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Largely the same as StudentViewActivity
        StudentListFragment slf = StudentListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLoader2, slf).commit();
    }

    public void onStudentsClicked(View view) {
        StudentListFragment slf = StudentListFragment.newInstance();
        // Replace fragment every time in order to get changes from the database
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLoader2, slf).commitNow();
        Button studentsButton = findViewById(R.id.studentsButton);
        Button sessionsButton = findViewById(R.id.sessionsButton);
        studentsButton.setBackgroundColor(getResources().getColor(R.color.teal_200, this.getTheme()));
        sessionsButton.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
        slf.tryNotifyEmptyList();
    }
    public void onSessionsClicked(View view) {
        SessionListFragment slf = SessionListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLoader2, slf).commitNow();
        Button studentsButton = findViewById(R.id.studentsButton);
        Button sessionsButton = findViewById(R.id.sessionsButton);
        studentsButton.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
        sessionsButton.setBackgroundColor(getResources().getColor(R.color.teal_200, this.getTheme()));
    }
}
