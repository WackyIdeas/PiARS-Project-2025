package bogdan.cvetanovski.pasalic;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DecideActivity extends AppCompatActivity {

    int sessionID;
    String sessionName;
    String description;
    String sessionDate;
    String endDate;

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
        sessionName = getIntent().getExtras().getString("sessionName");
        sessionID = getIntent().getExtras().getInt("sessionID");
        sessionDate = getIntent().getExtras().getString("sessionDate");
        endDate = getIntent().getExtras().getString("endDate");

        // Update UI with the passed information
        TextView tv = findViewById(R.id.descriptionText);
        String result = getResources().getString(R.string.DescriptionText, description);
        tv.setText(result);

        tv = findViewById(R.id.sessionTitle);
        result = sessionName;
        tv.setText(result);

        tv = findViewById(R.id.dateText);
        result = getResources().getString(R.string.SessionDateText, sessionDate);
        tv.setText(result);

        LocalDate endingDate = LocalDate.parse(endDate);
        LocalDate today = LocalDate.now();

        long daysBetween = ChronoUnit.DAYS.between(today, endingDate);
        tv = findViewById(R.id.timeLeftText);
        if(daysBetween <= 0) { // Disable all buttons and notify that the voting period has ended
            result = getResources().getString(R.string.VoteEnded);
            for (int j : buttonGroup) {
                Button b = findViewById(j);
                b.setEnabled(false);
            }
            Button b = findViewById(R.id.submitVoteButton);
            b.setEnabled(false);
        } else {
            result = getResources().getString(R.string.TimeLeftText, daysBetween, daysBetween == 1 ? "" : "s");
        }
        tv.setText(result);
    }

    public void submitVote(View view) {
        // Finalize vote decision
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = getResources().getString(R.string.VoteConfirmTitle);
        String text = getResources().getString(R.string.VoteConfirmBody);
        String stringYes = getResources().getString(R.string.VoteYes);
        String stringNo = getResources().getString(R.string.VoteNo);
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setCancelable(true);
        builder.setPositiveButton(stringYes, (dialog, which) -> {
            // Do voting shenanigans here
            try {
                User[] user = (User[])DatabaseFactory.getQueryResults(this, DatabaseManager.USERS_TABLE,
                        DatabaseManager.UserID+"="+String.valueOf(DatabaseManager.getInstance(this).getLoggedInID()), null);

                if(user != null) {
                    if(user.length != 1) {
                        throw new RuntimeException();
                    }


                }
            } catch (InvalidTableException e) {
                throw new RuntimeException(e);
            }

        });
        builder.setNegativeButton(stringNo, (dialog, which) -> dialog.cancel());
        AlertDialog d = builder.create();
        d.show();
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