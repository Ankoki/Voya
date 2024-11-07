package us.byeol.voya.storage;

import android.util.Pair;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import lombok.Getter;
import lombok.SneakyThrows;
import mx.kenzie.argo.Json;
import us.byeol.voya.auth.PasswordHasher;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.users.Book;
import us.byeol.voya.users.User;

/**
 * This is mainly for MongoDB, however we also need to access a dropbox
 * folder for the images for profile pictures and page pictures.
 */
public class IOHandler {

    public static String PROFILE_IMAGE = "voya_profile_pictures",
            PAGE_IMAGE = "voya_page_pictures";
    /**
     * -- GETTER --
     *  Returns the instance of the loaded Mongo database.
     *
     * @return the instance.
     */
    @Getter
    private static IOHandler instance = null;

    /**
     * Initiates the connection to our MongoDB and allows the use of the rest of this class.
     *
     * @param dropboxAccessToken the access token to use for dropbox.
     * @param voyaToken          the voya token.
     */
    public static void initiate(String dropboxAccessToken, String voyaToken) {
        instance = new IOHandler(dropboxAccessToken, voyaToken);
    }

    private URL dropboxDownload = null;
    private final String dropboxAccessToken;
    private final String voyaToken;
    private final List<User> userCache = new ArrayList<>();
    private final List<Book> bookCache = new ArrayList<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    private final Random random = new Random();

    /**
     * Creates a new handler to handle IO operations.
     *
     * @param dropboxAccessToken the access token to use for dropbox.
     * @param voyaToken          the token to use.
     */
    public IOHandler(String dropboxAccessToken, String voyaToken) {
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
    @SneakyThrows
    public Pair<Response, Boolean> isAvailable(String username) {
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/is-available/", WebRequest.RequestType.GET)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("authorization", this.voyaToken)
                    .addHeader("username", username);
            CompletableFuture<Optional<String>> future = web.execute();
            while (!future.isDone()) {} // Show loading bubble.
            Optional<String> response = future.get();
            if (response.isPresent()) {
                Map<String, Object> map = Json.fromJson(response.get());
                if (map.containsKey("available") && map.getOrDefault("available", true).equals(true))
                    return Pair.create(Response.SUCCESS, true);
                return Pair.create(Response.ERROR, false);
            } else
                return Pair.create(Response.NO_RESPONSE, false);
        } catch (IOException ex) {
            return Pair.create(Response.EXCEPTION, false);
        }
    }

    /**
     * Registers a user to the database and returns the created user.
     *
     * @param fullName the full name of the user.
     * @param username the username.
     * @param hashedPassword the hashed password.
     * @return the registered user. May be null.
     */
    @Nullable
    @SneakyThrows
    public User registerUser(String fullName, String username, String hashedPassword) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("username", username);
        map.put("password", hashedPassword);
        map.put("uuid", UUID.randomUUID().toString());
        map.put("profile-picture", "default_avatar_" + (random.nextInt(4) + 1));
        map.put("full-name", fullName);
        map.put("bio", "hey:) i'm " + fullName.split(" ")[0] + ".");
        map.put("friend-requests", new String[0]);
        map.put("book-invites", new String[0]);
        map.put("standard-books", new String[0]);
        map.put("admin-books", new String[0]);
        User user = User.deserialize(map);
        user.pushChanges();
        try {
            WebRequest request = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/update-uuid-username", WebRequest.RequestType.POST)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("authorization", this.voyaToken)
                    .addHeader("User-Agent","Mozilla/5.0 ( compatible ) ")
                    .addHeader("Accept", "*/*")
                    .addParameter(user.getUsername(), user.getUuid());
            request.execute();
        } catch (IOException ex) {
            Log.error(ex);
        }
        this.userCache.add(user);
        return user;
    }

    /**
     * Loads the userdata of the given uuid and returns the usable object.
     * If this user has been loaded before, will get it from the cache.
     *
     * @param uuid the uuid.
     * @return the loaded User object.
     */
    @SneakyThrows
    public User getCachedUser(String uuid) {
        for (User user : this.userCache) {
            if (user.getUuid().equals(uuid)) {
                user.fetch();
                return user;
            }
        }
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/fetch-userdata/", WebRequest.RequestType.GET)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addParameter("uuid", uuid);
            Optional<String> response = web.execute().get();
            if (response.isPresent()) {
                User user = User.deserialize(Json.fromJson(response.get()));
                if (user == null)
                    return null;
                this.userCache.add(user);
                return user;
            }
        } catch (IOException ex) { Log.error(ex); }
        return null;
    }

    /**
     * Loads the userdata of the given uuid and returns the usable object.
     * Will remove the password field for security purposes.
     *
     * @param uuid the uuid.
     * @return a map of userdata.
     */
    @SneakyThrows
    public Map<String, Object> getUser(String uuid) {
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/fetch-userdata/", WebRequest.RequestType.GET)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addHeader("uuid", uuid);
            Optional<String> response = web.execute().get();
            if (response.isPresent()) {
                Map<String, Object> json = Json.fromJson(response.get());
                json.remove("password");
                return json;
            }
        } catch (IOException ex) { Log.error(ex); }
        return null;
    }

    /**
     * Checks if the given password is correct for the given user.
     *
     * @param uuid the uuid of the user.
     * @param input the password to check.
     * @return true if correct.
     * @throws GeneralSecurityException if the algorithm is not found or the key specification is invalid.
     */
    @SneakyThrows
    public boolean validatePassword(String uuid, String input) throws GeneralSecurityException {
        if (uuid == null)
            return false;
        String password = null;
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/fetch-userdata/", WebRequest.RequestType.GET)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("authorization", this.voyaToken)
                    .addHeader("uuid", uuid);
            CompletableFuture<Optional<String>> future = web.execute();
            while (!future.isDone()) {} // Loading bubble or smth.
            Optional<String> response = future.get();
            if (response.isPresent()) {
                Map<String, Object> json = Json.fromJson(response.get());
                if (json == null || !json.containsKey("password"))
                    return false;
                password = Misc.castKey(json, "password", String.class);
            }
        } catch (IOException ex) {
            Log.error(ex);
            return false;
        }
        if (password == null || password.isEmpty())
            return false;
        PasswordHasher hasher = new PasswordHasher();
        return hasher.compare(input, password);
    }

    @Nullable
    @SneakyThrows
    public String getUuid(String username) {
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/get-uuid-from-username", WebRequest.RequestType.GET)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addHeader(Pair.create("username", username));
            CompletableFuture<Optional<String>> future = web.execute();
            while (!future.isDone()) {} // Loading bubble.
            Optional<String> response = future.get();
            if (response.isPresent()) {
                return Misc.castKey(Json.fromJson(response.get()), "uuid", String.class);
            }
        } catch (IOException ex) {
            Log.error(ex);
        }
        return null;
    }

    /**
     * Pushes a user's data to the database.
     *
     * @param user the user to push the data of.
     * @return true if successful.
     */
    @SneakyThrows
    public boolean pushUser(User user) {
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/push-userdata", WebRequest.RequestType.POST)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addParameter(user.serialize());
            for (Map.Entry<String, Object> entry : user.serialize().entrySet())
                Log.debug(entry.getKey() + "," + entry.getValue());
            Log.debug(web.getEncodedParameters());
            CompletableFuture<Optional<String>> future = web.execute();
            while (!future.isDone()) {} // Loading bubble.
            Optional<String> response = future.get();
            if (response.isPresent()) {
                Log.debug(response.get());
                return true;
            }
            // TODO finish this, check how error responses are sent.
            return true;
        } catch (IOException ex) { Log.error(ex); }
        return false;
    }

    /**
     * Loads the bookdata of the given uuid and returns the usable object.
     *
     * @param uuid the uuid.
     * @return the loaded Book object.
     */
    public Book loadBook(String uuid) {
        // Make sure to store these and when returning them, update them first on the object.
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
        CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
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
        });
        while (!future.isDone()) {} // Show loading bubble [or do in background?
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            Log.error(ex);
            return new byte[0];
        }
    }

    /**
     * // TODO Is this needed? If i'm syncing manually using {@link WebRequest} then I shouldn't need to close anything.
     * Closes the current client.
     * </br>
     * Please note that if you use this method, you will need to re-initiate this class to use
     * {@link IOHandler#getInstance()} without issues.
     */
    public void close() {
        IOHandler.instance = null;
    }

    public enum Response {
        SUCCESS,
        ERROR,
        NO_INTERNET,
        NO_RESPONSE,
        EXCEPTION;
    }

}
