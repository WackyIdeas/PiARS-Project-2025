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

    ProfileFragment profileFragment;
    CalendarFragment calendarFragment;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        String fragmentName = getIntent().getExtras().getString("studentName");
        profileFragment = ProfileFragment.newInstance(fragmentName);
        calendarFragment = CalendarFragment.newInstance();
        // Keeping both fragments loaded in memory and the buttons simply
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentLoader, calendarFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentLoader, profileFragment).commit();
        getSupportFragmentManager().beginTransaction().hide(calendarFragment).commit();
    }

    public void onProfileClicked(View view) {
        getSupportFragmentManager().beginTransaction().hide(calendarFragment).commit();
        getSupportFragmentManager().beginTransaction().show(profileFragment).commit();
        Button profileButton = findViewById(R.id.profileButton);
        Button calendarButton = findViewById(R.id.calendarButton);
        profileButton.setBackgroundColor(getResources().getColor(R.color.teal_200, this.getTheme()));
        calendarButton.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
    }
    public void onCalendarClicked(View view) {
        getSupportFragmentManager().beginTransaction().show(calendarFragment).commit();
        getSupportFragmentManager().beginTransaction().hide(profileFragment).commit();
        Button profileButton = findViewById(R.id.profileButton);
        Button calendarButton = findViewById(R.id.calendarButton);
        profileButton.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
        calendarButton.setBackgroundColor(getResources().getColor(R.color.teal_200, this.getTheme()));
    }
}