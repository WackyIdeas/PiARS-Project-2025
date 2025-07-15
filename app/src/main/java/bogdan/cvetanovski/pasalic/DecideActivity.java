package bogdan.cvetanovski.pasalic;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DecideActivity extends AppCompatActivity {

    int sessionNumber;
    String description;
    String sessionDate;
    long daysLeft;

    // Keep track of which button is pressed from the button group
    int[] buttonGroup = {
            R.id.yesButton,
            R.id.noButton,
            R.id.abstainButton
    };

    int selectedButton = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_decide);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        description = getIntent().getExtras().getString("description");
        sessionDate = getIntent().getExtras().getString("sessionDate");
        daysLeft = getIntent().getExtras().getLong("daysLeft");
        sessionNumber = getIntent().getExtras().getInt("sessionNumber");

        // Update UI with the passed information
        TextView tv = findViewById(R.id.descriptionText);
        String result = getResources().getString(R.string.DescriptionText, description);
        tv.setText(result);

        tv = findViewById(R.id.sessionTitle);
        result = getResources().getString(R.string.SessionText, sessionNumber);
        tv.setText(result);

        tv = findViewById(R.id.dateText);
        result = getResources().getString(R.string.SessionDateText, sessionDate);
        tv.setText(result);

        tv = findViewById(R.id.timeLeftText);
        result = getResources().getString(R.string.TimeLeftText, daysLeft, daysLeft == 1 ? "" : "s");
        tv.setText(result);
    }

    public void castVote(View view) {
        // onClick event handler provides the button that was clicked
        selectedButton = view.getId();
        Button btn = (Button)view;
        btn.setBackgroundColor(getResources().getColor(R.color.red, this.getTheme()));
        for (int j : buttonGroup) {
            if (j != selectedButton) {
                Button b = findViewById(j);
                b.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
            }
        }
    }
}