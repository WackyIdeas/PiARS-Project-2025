package bogdan.cvetanovski.pasalic;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StudentViewActivity extends AppCompatActivity {
    String fragmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fragmentName = getIntent().getExtras().getString("studentName");

        // Replace fragments every time in order to also get updates in case the database is changed
        ProfileFragment frag = ProfileFragment.newInstance(fragmentName);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLoader, frag).commit();
    }

    public void onProfileClicked(View view) {
        ProfileFragment frag = ProfileFragment.newInstance(fragmentName);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLoader, frag).commit();
        Button profileButton = findViewById(R.id.profileButton);
        Button calendarButton = findViewById(R.id.calendarButton);
        profileButton.setBackgroundColor(getResources().getColor(R.color.teal_200, this.getTheme()));
        calendarButton.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
    }
    public void onCalendarClicked(View view) {
        CalendarFragment frag = CalendarFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLoader, frag).commit();
        Button profileButton = findViewById(R.id.profileButton);
        Button calendarButton = findViewById(R.id.calendarButton);
        profileButton.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
        calendarButton.setBackgroundColor(getResources().getColor(R.color.teal_200, this.getTheme()));
    }
}