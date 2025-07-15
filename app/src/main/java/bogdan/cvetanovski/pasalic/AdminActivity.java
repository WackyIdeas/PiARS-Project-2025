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

    StudentListFragment studentListFragment;
    SessionListFragment sessionListFragment;
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
        studentListFragment = StudentListFragment.newInstance();
        sessionListFragment = SessionListFragment.newInstance();
        // Keeping both fragments loaded in memory and the buttons simply
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentLoader2, studentListFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentLoader2, sessionListFragment).commit();
        getSupportFragmentManager().beginTransaction().hide(sessionListFragment).commit();
    }


    public void onStudentsClicked(View view) {
        getSupportFragmentManager().beginTransaction().hide(sessionListFragment).commit();
        getSupportFragmentManager().beginTransaction().show(studentListFragment).commit();
        Button studentsButton = findViewById(R.id.studentsButton);
        Button sessionsButton = findViewById(R.id.sessionsButton);
        studentsButton.setBackgroundColor(getResources().getColor(R.color.teal_200, this.getTheme()));
        sessionsButton.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
    }
    public void onSessionsClicked(View view) {
        getSupportFragmentManager().beginTransaction().show(sessionListFragment).commit();
        getSupportFragmentManager().beginTransaction().hide(studentListFragment).commit();
        Button studentsButton = findViewById(R.id.studentsButton);
        Button sessionsButton = findViewById(R.id.sessionsButton);
        studentsButton.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
        sessionsButton.setBackgroundColor(getResources().getColor(R.color.teal_200, this.getTheme()));
    }
}
