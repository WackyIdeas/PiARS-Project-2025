package bogdan.cvetanovski.pasalic;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class ResultsActivity extends AppCompatActivity {

    int sessionID;
    Vote vote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sessionID = Objects.requireNonNull(getIntent().getExtras()).getInt("sessionID");
    }
    void updateResults() {
        vote = DatabaseManager.getInstance(this).getResults(sessionID);
        TextView votesYes = findViewById(R.id.yesVotes);
        TextView votesNo = findViewById(R.id.noVotes);
        TextView votesAbstain = findViewById(R.id.abstainVotes);
        votesYes.setText(getString(R.string.VotedYES, vote.getYesVotes()));
        votesNo.setText(getString(R.string.VotedNO, vote.getNoVotes()));
        votesAbstain.setText(getString(R.string.VotedABSTAIN, vote.getAbstainVotes()));
    }
    @Override
    protected void onStart() {
        super.onStart();
        updateResults();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Update the vote scores on resume as well
        updateResults();
    }
}