package us.byeol.voya.auth;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import java.util.regex.Pattern;

public class AuthValidator {

    private static final Pattern USERNAME_REGEX = Pattern.compile("^[A-Za-z]\\w{4,14}$");
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$");

    /**
     * Checks if the current device is connected to the internet.
     *
     * @param context the context.
     * @return true if connected.
     */
    public static boolean hasInternet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null; // Maybe && manager.getActiveNetworkInfo().isAvailable();
    }

    /**
     * Checks if the given username is valid.
     *
     * @param username the string to test.
     * @return true if valid.
     */
    public static boolean isValidUsername(String username) {
        return USERNAME_REGEX.matcher(username).matches();
    }

    /**
     * Checks if the given password is valid.
     * Must be 8-20 characters.
     * Must contain 1 lowercase and 1 uppercase letter.
     * Must contain 1 number.
     *
     * @param password the string to test.
     * @return true if valid.
     */
    public static boolean isValidPassword(String password) {
        return PASSWORD_REGEX.matcher(password).matches();
    }

}