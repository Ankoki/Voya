package us.byeol.voya.storage;

import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import live.ditto.Ditto;
import mx.kenzie.argo.Json;
import us.byeol.voya.misc.Log;
import us.byeol.voya.users.User;

/**
 * This is mainly for MongoDB, however we also need to access a dropbox
 * folder for the images for profile pictures and page pictures.
 */
public class IOHandler {

    public static String PROFILE_IMAGE = "voya_profile_pictures",
            PAGE_IMAGE = "voya_page_pictures";
    private static IOHandler instance = null;

    /**
     * Initiates the connection to our MongoDB and allows the use of the rest of this class.
     *
     * @param ditto              the ditto object.
     * @param dropboxAccessToken the access token to use for dropbox.
     * @param voyaToken          the voya token.
     */
    public static void initiate(Ditto ditto, String dropboxAccessToken, String voyaToken) {
        instance = new IOHandler(ditto, dropboxAccessToken, voyaToken);
    }

    /**
     * Returns the instance of the loaded Mongo database.
     *
     * @return the instance.
     */
    public static IOHandler getInstance() {
        return instance;
    }

    private URL dropboxDownload = null;
    private final String dropboxAccessToken;
    private final String voyaToken;
    private final Ditto ditto;
    private final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Creates a new handler to handle IO operations.
     *
     * @param ditto              the ditto object.
     * @param dropboxAccessToken the access token to use for dropbox.
     * @param voyaToken          the token to use.
     */
    public IOHandler(Ditto ditto, String dropboxAccessToken, String voyaToken) {
        this.ditto = ditto;
        this.dropboxAccessToken = dropboxAccessToken;
        this.voyaToken = voyaToken;
        try {
            this.dropboxDownload = new URL("https://content.dropboxapi.com/2/files/download");
        } catch (IOException ex) {
            Log.error(ex);
        }
    }

    /**
     * Checks if a username is available to register.
     *
     * @param username the username to check.
     * @return A pair with the response. If a successful response, the boolean value will be present.
     */
    public Pair<Response, Boolean> isAvailable(String username) {
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herkouapp.com/is-available/", WebRequest.RequestType.GET)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addParameter("username", username);
            Optional<String> response = web.execute();
            if (response.isPresent())
                return Pair.create(Response.SUCCESS, (boolean) Json.fromJson(response.get()).get("available"));
            else
                return Pair.create(Response.NO_RESPONSE, false);
        } catch (IOException ex) {
            return Pair.create(Response.EXCEPTION, false);
        }
    }

    /**
     * @param fullName
     * @param username
     * @param hashedPassword
     * @return
     */
    public User registerUser(String fullName, String username, String hashedPassword) {
        return null;
    }

    /**
     * Loads the userdata of the given username and returns the usable object.
     *
     * @param username the username.
     * @return the loaded User object.
     */
    public User loadUser(String username) {
        return null;
    }

    /**
     * Gets the desired image from Dropbox.
     *
     * @param folderName the name of the folder.
     *                   For accurate results, use the {@link IOHandler#PAGE_IMAGE}
     *                   or {@link IOHandler#PROFILE_IMAGE} field.
     * @param imageName  the name of the image including extension.
     * @return the data of the image. If any errors are logged, will return nothing.
     */
    public byte[] getImage(String folderName, String imageName) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) this.dropboxDownload.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + this.dropboxAccessToken);
            connection.setRequestProperty("Dropbox-API-Arg", "{\"path\":\"" + folderName + "\\" + imageName + "\"}");
            connection.setDoOutput(true);
            int response = connection.getResponseCode();
            if (response != HttpsURLConnection.HTTP_OK) {
                Log.error("Getting image response failed. [ " + response + " | " + connection.getResponseMessage() + "] ");
                return new byte[0];
            }
            try (BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1)
                    outputStream.write(buffer, 0, bytesRead);
                return outputStream.toByteArray();
            }
        } catch (IOException ex) {
            Log.error(ex);
        }
        return new byte[0];
    }

    /**
     * Closes the current Ditto client.
     * </br>
     * Please note that if you use this method, you will need to re-initiate Ditto to use
     * {@link IOHandler#getInstance()} without issues.
     */
    public void close() {
        this.ditto.close();
        IOHandler.instance = null;
    }

    public enum Response {
        SUCCESS,
        NO_INTERNET,
        NO_RESPONSE,
        EXCEPTION;
    }

}
