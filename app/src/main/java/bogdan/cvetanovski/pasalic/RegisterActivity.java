package bogdan.cvetanovski.pasalic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
     * but this can lead to an arbitrary amount of redundant activities
     * being pushed onto the activity stack by repeatedly tapping on the
     * Register buttons on each newly loaded activity. This may lead to
     * undesirable effects and is generally seen as bad practice.
     *
     * (In general, the number of loaded activities on
     * the activity stack should be kept to a minimum.)
     *
     * We can simply call finish() instead to send the user back to
     * the previously loaded activity, which in our case will always be the
     * starting activity.
     */
    public void enterLoginPage(View view) {

        EditText v_username = findViewById(R.id.registerUsernameTextEdit);
        EditText v_name = findViewById(R.id.registerNameTextEdit);
        EditText v_surname = findViewById(R.id.registerSurnameTextEdit);
        EditText v_password = findViewById(R.id.registerPasswordTextEdit);

        String username = v_username.getText().toString().trim();
        String name = v_name.getText().toString().trim();
        String surname = v_surname.getText().toString().trim();
        String password = v_password.getText().toString().trim();

        if(username.isEmpty()) {
            makeToast(R.string.RegistrationFailedEmptyEntry, R.string.RegistrationIndex);
            return;
        } else if(name.isEmpty()) {
            makeToast(R.string.RegistrationFailedEmptyEntry, R.string.RegistrationName);
            return;
        } else if(password.isEmpty()) {
            makeToast(R.string.RegistrationFailedEmptyEntry, R.string.RegistrationPassword);
            return;
        }

        User user = new User();
        user.setRole(username.startsWith("!") ? 1 : 0);
        user.setName(name);
        user.setSurname(surname);
        user.setHash(password);
        user.setUsername(user.getRole() == 1 ? username.substring(1) : username);

        String msg = DatabaseManager.getInstance(this).registerUser(user);
        if(msg.isEmpty()) {
            makeToast(R.string.RegistrationSuccess, -1);
            finish();
        } else {
            makeToast(msg);
        }
    }

    public void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    public void makeToast(int stringRes, int entry) {
        String toastText;
        if(entry != -1) {
            String entryText = getResources().getString(entry);
            toastText = getResources().getString(stringRes, entryText.substring(entryText.indexOf(' ')+1));
        } else {
            toastText = getResources().getString(stringRes);
        }
        makeToast(toastText);
    }
}