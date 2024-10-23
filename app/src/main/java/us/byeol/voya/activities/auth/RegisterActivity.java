package us.byeol.voya.activities.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import us.byeol.voya.R;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.misc.popup.PopUp;
import us.byeol.voya.auth.AuthValidator;

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
            if (!AuthValidator.hasInternet(this))
                PopUp.instance.showText(view, getString(R.string.no_internet), PopUp.Length.LENGTH_LONG);
            else if (fullNameInput.getText().toString().isEmpty() || fullNameInput.getText().toString().isBlank() ||
                    usernameInput.getText().toString().isEmpty() || usernameInput.getText().toString().isBlank() ||
                    passwordInput.getText().toString().isEmpty() || passwordInput.getText().toString().isBlank() ||
                    confirmPasswordInput.getText().toString().isEmpty() || confirmPasswordInput.getText().toString().isBlank())
                PopUp.instance.showText(view, getString(R.string.empty_fields), PopUp.Length.LENGTH_LONG);
            else if (!fullNameInput.getText().toString().contains(" "))
                PopUp.instance.showText(view, getString(R.string.invalid_fullname), PopUp.Length.LENGTH_LONG);
            else if (!AuthValidator.isValidUsername(usernameInput.getText().toString()))
                PopUp.instance.showText(view, getString(R.string.invalid_username), PopUp.Length.LENGTH_LONG);
            else if (!AuthValidator.isValidPassword(passwordInput.getText().toString()))
                PopUp.instance.showText(view, getString(R.string.invalid_password), PopUp.Length.LENGTH_LONG);
            else if (!passwordInput.getText().toString().equals(confirmPasswordInput.getText().toString()))
                PopUp.instance.showText(view, getString(R.string.mismatching_passwords), PopUp.Length.LENGTH_LONG);
            else {
                // Smth
            }
        });
    }

}
