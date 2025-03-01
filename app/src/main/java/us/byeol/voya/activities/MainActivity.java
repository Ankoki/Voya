package us.byeol.voya.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import us.byeol.voya.R;
import us.byeol.voya.activities.auth.LoginActivity;
import us.byeol.voya.activities.main.HomeActivity;
import us.byeol.voya.auth.AuthValidator;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.web.IOHandler;
import us.byeol.voya.api.User;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Misc.setStatusBarColour(this.getWindow(), this.getResources().getColor(R.color.sage_green));
        EdgeToEdge.enable(this);
        Properties properties = new Properties();
        try (InputStream stream = this.getResources().openRawResource(R.raw.config)) {
            properties.load(stream);
        } catch (IOException ex) { Log.error(ex); }
        IOHandler.initiate(properties.getProperty("dropbox_access_token"), properties.getProperty("voya_token"));
        if (!AuthValidator.hasInternet(this)) {
            this.startActivity(new Intent(this.getBaseContext(), LoginActivity.class));
            return;
        }
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences("userdata", 0);
        String uuid = preferences.getString("current-uuid", "");
        long lastAuth = preferences.getLong("last-authentication", System.currentTimeMillis());
        if ((System.currentTimeMillis() - lastAuth) >= 6.048e+8) {
            uuid = "";
            preferences.edit().putString("current-uuid", "").apply();
        }
        if (uuid.isEmpty())
            this.startActivity(new Intent(this.getBaseContext(), LoginActivity.class));
        else {
            User initialFetch = IOHandler.getInstance().fetchUser(uuid);
            if (initialFetch != null && initialFetch.isValid())
                this.startActivity(new Intent(this.getBaseContext(), HomeActivity.class));
            else {
                this.startActivity(new Intent(this.getBaseContext(), LoginActivity.class));
            }
        }
    }

}