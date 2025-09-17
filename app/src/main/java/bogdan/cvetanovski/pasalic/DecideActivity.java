package bogdan.cvetanovski.pasalic;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

public class DecideActivity extends AppCompatActivity {

    int sessionID;
    String sessionName;
    String description;
    String sessionDate;
    String endDate;

    String sessionHexID;
    private Handler handler = new Handler(Looper.getMainLooper());

    int voteID;

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
        sessionHexID = getIntent().getExtras().getString("sessionHexID");

        // Update UI with the passed information
        TextView tv = findViewById(R.id.descriptionText);
        String result = getResources().getString(R.string.DescriptionText, description);
        tv.setText(result);

        tv = findViewById(R.id.sessionTitle);
        result = sessionName;
        tv.setText(result);

        tv = findViewById(R.id.dateText);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        LocalDateTime sessionDateTime = LocalDateTime.parse(sessionDate, fmt);
        DateTimeFormatter readableFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        result = getResources().getString(R.string.SessionDateText, sessionDateTime.format(readableFormat));
        tv.setText(result);

        LocalDateTime endingDate = LocalDateTime.parse(endDate, fmt);
        LocalDateTime today = LocalDateTime.now();

        long daysBetween = ChronoUnit.DAYS.between(today, endingDate);

        // Disable the submit button by default
        Button b = findViewById(R.id.submitVoteButton);
        b.setEnabled(false);
        for (int j : buttonGroup) {
            b = findViewById(j);
            b.setEnabled(false);
        }

        new Thread(() -> {
                // Synchronize the database with the MongoDB server
                String res = DatabaseManager.getInstance(this).synchronizeDatabase();
                handler.post(() -> {
                    if(!res.isEmpty()) {
                        Toast.makeText(this, res, Toast.LENGTH_LONG).show();
                        return;
                    }
                    TextView timeLeftView = findViewById(R.id.timeLeftText);
                    // Check if the user has voted before for the given session
                    // This is done to prevent abuse of the voting system, while
                    // attempting to anonymize each user who voted.
                    boolean canVote = false;
                    try {
                        Vote[] votes = (Vote[])DatabaseFactory.getQueryResults(this, DatabaseManager.VOTES_TABLE,
                                DatabaseManager.SessionID + "=" + sessionID, null, null);
                        if(votes != null && votes.length > 0) {
                            voteID = votes[0].getId();
                            canVote = DatabaseManager.getInstance(this).canVote(this, voteID);
                        }
                    } catch (InvalidTableException e) {
                        throw new RuntimeException(e);
                    }

                    if(!canVote) {
                        TextView label = findViewById(R.id.alreadyVotedTextView);
                        label.setText(getResources().getString(R.string.AlreadyVoted));
                    }
                    String txt;
                    if(daysBetween <= 0) {
                        txt = getResources().getString(R.string.VoteEnded);
                    } else {
                        txt = getResources().getString(R.string.TimeLeftText, daysBetween, daysBetween == 1 ? "" : "s");
                    }
                    timeLeftView.setText(txt);
                    // Enable all the other buttons if the following conditions are met
                    Button btn;
                    if(canVote && daysBetween > 0) {
                        for (int j : buttonGroup) {
                            btn = findViewById(j);
                            btn.setEnabled(true);
                        }
                    }
                });
        }).start();

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
            DatabaseManager.VoteDecision vote;
            if(selectedButton == R.id.yesButton) vote = DatabaseManager.VoteDecision.YES;
            else if(selectedButton == R.id.noButton) vote = DatabaseManager.VoteDecision.NO;
            else if(selectedButton == R.id.abstainButton) vote = DatabaseManager.VoteDecision.ABSTAIN;
            else {
                // If, somehow, selectedButton is invalid, throw an error
                String toastText = getResources().getString(R.string.VoteError);
                Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
                dialog.cancel();
                return;
            }
            new Thread(() -> {
                // Perform the vote itself
                String msg = DatabaseManager.getInstance(this).castVote(sessionHexID,voteID,vote);
                handler.post(() -> {
                    if(!msg.isEmpty()) {
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    } else {
                        TextView label = findViewById(R.id.alreadyVotedTextView);
                        label.setText(getResources().getString(R.string.AlreadyVoted));
                        // Disable all buttons after a successful vote
                        Button b = findViewById(R.id.submitVoteButton);
                        b.setEnabled(false);
                        for (int j : buttonGroup) {
                            b = findViewById(j);
                            b.setEnabled(false);
                        }
                    }
                });
            }).start();
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
        Button submitButton = findViewById(R.id.submitVoteButton);
        submitButton.setEnabled(true);
        for (int j : buttonGroup) {
            if (j != selectedButton) {
                Button b = findViewById(j);
                b.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
            }
        }
    }
}