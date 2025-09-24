package bogdan.cvetanovski.pasalic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StudentViewActivity extends AppCompatActivity implements ServiceConnection {
    String fragmentName;
    SessionService sessionService;

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

        /*
         * We want the service to last throughout the entire lifecycle of this activity, so
         * that the user can get notified even when the activity is in the background, and
         * so that the service doesn't get potentially reinstantiated.
         * Once this activity is gone (the user has logged off, or closed the application),
         * so is the service.
         */
        Intent intent = new Intent(this, SessionService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    // Unbind service as the user logs off or closes the application.
    @Override
    protected void onDestroy() {
        unbindService(this);
        super.onDestroy();
    }

    // Look for new received intents in order to detect if the user has entered
    // the activity via the notification.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getBooleanExtra("fromNotification", false)) {
            onCalendarClicked(null);
        }
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

    // Activities using services need to implement ServiceConnection
    // Start checking for expiring sessions as the activity connects itself to the service
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        SessionService.SessionBinder b = (SessionService.SessionBinder)iBinder;
        sessionService = b.getService();

        sessionService.listenToSessions();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}