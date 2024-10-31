package us.byeol.voya.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.security.GeneralSecurityException;

import us.byeol.voya.R;
import us.byeol.voya.activities.main.HomeActivity;
import us.byeol.voya.auth.PasswordHasher;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.misc.popup.PopUp;
import us.byeol.voya.auth.AuthValidator;
import us.byeol.voya.storage.IOHandler;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Misc.setStatusBarColour(this.getWindow(), this.getResources().getColor(R.color.sage_green));
        this.setContentView(R.layout.activity_register);
        EditText fullNameInput = this.findViewById(R.id.full_name);
        EditText usernameInput = this.findViewById(R.id.username_field);
        EditText passwordInput = this.findViewById(R.id.password_field);
        EditText confirmPasswordInput = this.findViewById(R.id.confirm_password_field);
        Button registerButton = this.findViewById(R.id.register_button);
        registerButton.setOnClickListener(view -> {
            String fullName = fullNameInput.getText().toString();
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();
            if (!AuthValidator.hasInternet(this))
                PopUp.instance.showText(view, getString(R.string.no_internet), PopUp.Length.LENGTH_LONG);
            else if (fullName.isEmpty() || fullName.isBlank() ||
                    username.isEmpty() || username.isBlank() ||
                    password.isEmpty() || password.isBlank() ||
                    confirmPassword.isEmpty() || confirmPassword.isBlank())
                PopUp.instance.showText(view, getString(R.string.empty_fields), PopUp.Length.LENGTH_LONG);
            else if (!fullName.contains(" "))
                PopUp.instance.showText(view, getString(R.string.invalid_fullname), PopUp.Length.LENGTH_LONG);
            else if (!AuthValidator.isValidUsername(username))
                PopUp.instance.showText(view, getString(R.string.invalid_username), PopUp.Length.LENGTH_LONG);
            else if (!AuthValidator.isValidPassword(password))
                PopUp.instance.showText(view, getString(R.string.invalid_password), PopUp.Length.LENGTH_LONG);
            else if (!password.equals(confirmPassword))
                PopUp.instance.showText(view, getString(R.string.mismatching_passwords), PopUp.Length.LENGTH_LONG);
            else {
                Pair<IOHandler.Response, Boolean> availablePair = IOHandler.getInstance().isAvailable(username);
                if (availablePair.first != IOHandler.Response.SUCCESS) {
                    switch (availablePair.first) {
                        case NO_RESPONSE:
                        case EXCEPTION:
                            PopUp.instance.showText(view, getString(R.string.exception_popup), PopUp.Length.LENGTH_LONG);
                            break;
                        case NO_INTERNET:
                            PopUp.instance.showText(view, getString(R.string.no_internet), PopUp.Length.LENGTH_LONG);
                    }
                    return;
                }
                try {
                    IOHandler.getInstance().registerUser(fullName, username, new PasswordHasher().hash(password));
                    PopUp.instance.showText(view, getString(R.string.registered), PopUp.Length.LENGTH_LONG);
                    this.getApplicationContext()
                            .getSharedPreferences("userdata", 0)
                            .edit()
                            .putString("username", username)
                            .putLong("last-authentication", System.currentTimeMillis())
                            .apply();
                    this.startActivity(new Intent(this.getBaseContext(), HomeActivity.class));
                } catch (GeneralSecurityException ex) {
                    Log.error(ex);
                    PopUp.instance.showText(view, getString(R.string.exception_popup), PopUp.Length.LENGTH_LONG);
                }
            }
        });
    }

}
