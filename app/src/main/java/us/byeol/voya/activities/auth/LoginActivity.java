package us.byeol.voya.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.security.GeneralSecurityException;

import us.byeol.voya.R;
import us.byeol.voya.activities.main.HomeActivity;
import us.byeol.voya.auth.AuthValidator;
import us.byeol.voya.auth.PasswordHasher;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.misc.popup.PopUp;
import us.byeol.voya.web.IOHandler;
import us.byeol.voya.api.User;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Misc.setStatusBarColour(this.getWindow(), this.getResources().getColor(R.color.sage_green));
        this.setContentView(R.layout.activity_login);
        this.findViewById(R.id.root).setZ(-100F);
        try {
            Log.debug(new PasswordHasher().hash("T3stP4ssw0rd!"));
        } catch (GeneralSecurityException ex) {
            ex.printStackTrace();
        }
        authentication_button : {
            EditText usernameInput = this.findViewById(R.id.username_field);
            EditText passwordInput = this.findViewById(R.id.password_field);
            Button loginButton = this.findViewById(R.id.login_button);
            CoordinatorLayout coordinator = this.findViewById(R.id.coordinator);
            loginButton.setOnClickListener(view -> {
                if (!AuthValidator.hasInternet(this))
                    PopUp.instance.showText(coordinator, getString(R.string.no_internet), PopUp.Length.LENGTH_LONG);
                else if (usernameInput.getText().toString().isEmpty() || usernameInput.getText().toString().isBlank() ||
                    passwordInput.getText().toString().isEmpty() || passwordInput.getText().toString().isBlank())
                    PopUp.instance.showText(coordinator, getString(R.string.empty_fields), PopUp.Length.LENGTH_LONG);
                else if (!AuthValidator.isValidUsername(usernameInput.getText().toString()))
                    PopUp.instance.showText(coordinator, getString(R.string.invalid_username), PopUp.Length.LENGTH_LONG);
                else if (!AuthValidator.isValidPassword(passwordInput.getText().toString()))
                    PopUp.instance.showText(coordinator, getString(R.string.invalid_password), PopUp.Length.LENGTH_LONG);
                else {
                    String uuid = IOHandler.getInstance().fetchUuid(usernameInput.getText().toString());
                    try {
                        boolean authenticated = IOHandler.getInstance().validatePassword(uuid, passwordInput.getText().toString());
                        if (authenticated) {
                            User user = IOHandler.getInstance().fetchUser(uuid);
                            if (user == null || !user.isValid()) {
                                PopUp.instance.showText(coordinator, getString(R.string.exception_popup), PopUp.Length.LENGTH_LONG);
                                return;
                            }
                            this.getApplicationContext()
                                    .getSharedPreferences("userdata", 0)
                                    .edit()
                                    .putString("current-uuid", uuid)
                                    .putLong("last-authentication", System.currentTimeMillis())
                                    .apply();
                            PopUp.instance.showText(coordinator, getString(R.string.logged_in), PopUp.Length.LENGTH_LONG);
                            this.startActivity(new Intent(this.getBaseContext(), HomeActivity.class));
                        } else
                            PopUp.instance.showText(coordinator, getString(R.string.incorrect_password), PopUp.Length.LENGTH_LONG);
                    } catch (GeneralSecurityException ex) {
                        Log.error(ex);
                        PopUp.instance.showText(coordinator, getString(R.string.exception_popup), PopUp.Length.LENGTH_LONG);
                    }
                }
            });
        }

        register_button : {
            TextView text = this.findViewById(R.id.register_prompt);
            text.setOnClickListener(coordinator -> this.startActivity(new Intent(this.getBaseContext(), RegisterActivity.class)));
        }

    }

}