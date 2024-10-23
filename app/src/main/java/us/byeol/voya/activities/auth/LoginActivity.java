package us.byeol.voya.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import us.byeol.voya.R;
import us.byeol.voya.activities.MainActivity;
import us.byeol.voya.auth.AuthValidator;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.misc.popup.PopUp;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Misc.setStatusBarColour(this.getWindow(), this.getResources().getColor(R.color.sage_green));
        this.setContentView(R.layout.activity_login);
        this.findViewById(R.id.root).setZ(-100F);

        authentication_button : {
            EditText usernameInput = this.findViewById(R.id.username_field);
            EditText passwordInput = this.findViewById(R.id.password_field);
            Button loginButton = this.findViewById(R.id.login_button);
            loginButton.setOnClickListener(view -> {
                if (!AuthValidator.hasInternet(this))
                    PopUp.instance.showText(view, getString(R.string.no_internet), PopUp.Length.LENGTH_LONG);
                else if (usernameInput.getText().toString().isEmpty() || usernameInput.getText().toString().isBlank() ||
                    passwordInput.getText().toString().isEmpty() || passwordInput.getText().toString().isBlank())
                    PopUp.instance.showText(view, getString(R.string.empty_fields), PopUp.Length.LENGTH_LONG);
                else if (!AuthValidator.isValidUsername(usernameInput.getText().toString()))
                    PopUp.instance.showText(view, getString(R.string.invalid_username), PopUp.Length.LENGTH_LONG);
                else if (!AuthValidator.isValidPassword(passwordInput.getText().toString()))
                    PopUp.instance.showText(view, getString(R.string.invalid_password), PopUp.Length.LENGTH_LONG);
                else {
                    boolean authenticated = true; // TODO Authentication System [Database?]
                    if (authenticated) {
                        this.getApplicationContext()
                                .getSharedPreferences("userdata", 0)
                                .edit()
                                .putString("username", usernameInput.getText().toString())
                                .putLong("last-authentication", System.currentTimeMillis())
                                .apply();
                        PopUp.instance.showText(view, "You have been logged in!", PopUp.Length.LENGTH_LONG);
                        this.startActivity(new Intent(this.getBaseContext(), MainActivity.class));
                    } else
                        PopUp.instance.showText(this.findViewById(android.R.id.content), "Authentication Failed", PopUp.Length.LENGTH_LONG);
                }
            });
        }

        register_button : {
            TextView text = this.findViewById(R.id.register_prompt);
            text.setOnClickListener(view -> this.startActivity(new Intent(this.getBaseContext(), RegisterActivity.class)));
        }
    }

    public void onClick(View view) {

    }

}