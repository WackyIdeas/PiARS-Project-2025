package bogdan.cvetanovski.pasalic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /*
     * The assignment suggests using Intent to switch back to LoginActivity,
     * but this can lead to an arbitrary amount of activities being spawned
     * by the user by repeatedly tapping on the Register buttons on each
     * newly loaded activity. This may lead to undesirable effects and is
     * generally seen as bad practice.
     *
     * (In general, the number of loaded activities on
     * the activity stack should be kept to a minimum.)
     *
     * We can simply call finish() instead to send the user back to
     * the previously loaded activity, which in our case will always be the
     * starting activity.
     */
    public void enterLoginPage(View view) {
        //Intent intent = new Intent(this, LoginActivity.class);
        //startActivity(intent);
        finish();
    }
}