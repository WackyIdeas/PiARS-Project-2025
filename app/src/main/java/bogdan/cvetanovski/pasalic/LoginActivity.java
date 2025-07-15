package bogdan.cvetanovski.pasalic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void enterRegistrationPage(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void tryLogin(View view) {
        // As of SDK >=26, findViewById no longer requires
        // explicit casts as the return type is <T extends View>.
        EditText nameInput = findViewById(R.id.usernameTextEdit);
        EditText passInput = findViewById(R.id.passwordTextEdit);

        /*
         * Currently only allow login access for two fake users:
         * 1. student / student
         * 2. admin / admin
         * If logging in as an admin, show a completely different
         * activity.
         */
        if(nameInput.getText().toString().equals("student") &&
           passInput.getText().toString().equals("student")) {
            // Enter student page
            Intent intent = new Intent(this, StudentViewActivity.class);
            Bundle params = new Bundle();
            params.putString("studentName", nameInput.getText().toString());
            intent.putExtras(params);
            startActivity(intent);
        } else if(nameInput.getText().toString().equals("admin") &&
                  passInput.getText().toString().equals("admin")) {
            // Enter admin page
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
        } else {
            // Login failed, notify user about it
            // https://developer.android.com/guide/topics/ui/notifiers/toasts
            String toastText = getResources().getString(R.string.LoginFailedToast);
            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
        }
    }
}