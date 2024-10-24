package us.byeol.voya.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import live.ditto.Ditto;
import live.ditto.DittoDependencies;
import live.ditto.DittoIdentity;
import live.ditto.DittoLogLevel;
import live.ditto.DittoLogger;
import live.ditto.android.DefaultAndroidDittoDependencies;
import live.ditto.transports.DittoSyncPermissions;
import us.byeol.voya.R;
import us.byeol.voya.activities.auth.LoginActivity;
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
        DittoSyncPermissions permissions = new DittoSyncPermissions(this);
        String[] missing = permissions.missingPermissions(permissions.requiredPermissions());
        if (missing.length > 0)
            this.requestPermissions(missing, 0);
        DittoLogger.setMinimumLogLevel(DittoLogLevel.DEBUG);
        DittoDependencies dependencies = new DefaultAndroidDittoDependencies(this);
        DittoIdentity identity = new DittoIdentity.OnlinePlayground(
                dependencies,
                properties.getProperty("ditto_app_id"),
                properties.getProperty("ditto_playground_token")
        );
        IOHandler.initiate(new Ditto(dependencies, identity), properties.getProperty("dropbox_access_token"), properties.getProperty("voya_token"));
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences("userdata", 0);
        String user = preferences.getString("username", "");
        long lastAuth = preferences.getLong("last-authentication", System.currentTimeMillis());
        if ((System.currentTimeMillis() - lastAuth) >= 6.048e+8) {
            user = "";
            preferences.edit().putString("username", "").apply();
        }
        if (user.isEmpty())
            this.startActivity(new Intent(this.getBaseContext(), LoginActivity.class));
        else {
            this.setContentView(R.layout.activity_main);
            // TODO redirect to a different screen for the actual program.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Not removing because I can't remember why I added this and I need to figure out if it was something important.
        // IOHandler.getInstance().refreshPermissions();
    }

}