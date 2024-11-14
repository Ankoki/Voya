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
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.storage.IOHandler;

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
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences("userdata", 0);
        String user = preferences.getString("username", "");
        long lastAuth = preferences.getLong("last-authentication", System.currentTimeMillis());
        if ((System.currentTimeMillis() - lastAuth) >= 6.048e+8) {
            user = "";
            preferences.edit().putString("username", "").apply();
        }
        if (user.isEmpty())
            this.startActivity(new Intent(this.getBaseContext(), LoginActivity.class));
        else
           this.startActivity(new Intent(this.getBaseContext(), HomeActivity.class));
    }

}