package us.byeol.voya.storage;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import live.ditto.Ditto;
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
     * @param ditto           the ditto object.
     * @param dropboxAccessToken the access token to use for dropbox.
     */
    public static void initiate(Ditto ditto, String dropboxAccessToken) {
        instance = new IOHandler(ditto, dropboxAccessToken);
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
    private final Ditto ditto;

    /**
     * Creates a new mongo client.
     *
     * @param ditto           the ditto object.
     * @param dropboxAccessToken the access token to use for dropbox.
     */
    public IOHandler(Ditto ditto, String dropboxAccessToken) {
        this.ditto = ditto;
        this.dropboxAccessToken = dropboxAccessToken;
        try {
            this.dropboxDownload = new URL("https://content.dropboxapi.com/2/files/download");
        } catch (IOException ex) { Log.error(ex); }
    }

    /**
     *
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
        } catch (IOException ex) { Log.error(ex); }
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

}
