package us.byeol.voya.web;

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

import javax.net.ssl.HttpsURLConnection;

import lombok.SneakyThrows;
import mx.kenzie.argo.Json;
import us.byeol.voya.auth.PasswordHasher;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.api.Book;
import us.byeol.voya.api.User;

/**
 * This is mainly for MongoDB, however we also need to access a dropbox
 * folder for the images for profile pictures and page pictures.
 */
public class IOHandler {

    //<editor-fold desc="Instance Handling">

    private static IOHandler instance = null;

    /**
     * Returns the instance of the class that has been initiated last. 
     * <br>
     * If you have called the {@link IOHandler#initiate(String, String)} method, 
     * you can assert this is not null.
     *
     * @return the instance.
     */
    public static IOHandler getInstance() {
        return IOHandler.instance;
    }
    
    /**
     * Initiates the connection to our MongoDB and allows the use of the rest of this class.
     *
     * @param dropboxAccessToken the access token to use for dropbox.
     * @param voyaToken          the voya token.
     */
    public static void initiate(String dropboxAccessToken, String voyaToken) {
        instance = new IOHandler(dropboxAccessToken, voyaToken);
    }

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
        } catch (IOException ex) { Log.error(ex); }
    }

    //</editor-fold>

    //<editor-fold desc="Fields">

    public static String PROFILE_IMAGE = "voya_profile_pictures",
            BOOK_IMAGE = "voya_book_pictures",
            PAGE_IMAGE = "voya_page_pictures";

    private URL dropboxDownload = null;
    private final String dropboxAccessToken;
    private final String voyaToken;
    private final List<User> userCache = new ArrayList<>();
    private final List<Book> bookCache = new ArrayList<>();

    private final Random random = new Random();

    //</editor-fold>

    //<editor-fold desc="Registration">

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
            while (!future.isDone()) {}
            Optional<String> response = future.get();
            if (response.isPresent()) {
                Map<String, Object> map = Json.fromJson(response.get());
                if (map.containsKey("available") && Misc.castKey(map, "available", boolean.class).equals(true))
                    return Pair.create(Response.SUCCESS, true);
                return Pair.create(Response.ERROR, false);
            } else
                return Pair.create(Response.NO_RESPONSE, false);
        } catch (IOException ex) {
            return Pair.create(Response.EXCEPTION, false);
        }
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
            while (!future.isDone()) {}
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
        String uuid = UUID.randomUUID().toString();
        map.put("uuid", uuid);
        map.put("profile-picture", "default_avatar_" + (random.nextInt(4) + 1));
        map.put("full-name", fullName);
        map.put("bio", "hey:) i'm " + fullName.split(" ")[0] + ".");
        map.put("friend-requests", new ArrayList<String>());
        map.put("book-invites", new ArrayList<String>());
        map.put("standard-books", new ArrayList<String>());
        map.put("admin-books", new ArrayList<String>());
        this.pushUserdata(username, map);
        try {
            WebRequest uuidRequest = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/update-uuid-username", WebRequest.RequestType.POST)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("authorization", this.voyaToken)
                    .addHeader("User-Agent","Mozilla/5.0 ( compatible ) ")
                    .addHeader("Accept", "*/*")
                    .addParameter(Map.of(username, uuid));
            CompletableFuture<Optional<String>> future = uuidRequest.execute();
            while (!future.isDone()) {}
        } catch (IOException ex) {
            Log.error(ex);
            return null;
        }
        User user = User.deserialize(map);
        this.userCache.add(user);
        return user;
    }

    //</editor-fold>

    //<editor-fold desc="Userdata">

    /**
     * Loads the userdata of the given uuid and returns the usable object.
     * If this user has been loaded before, will get it from the cache.
     *
     * @param uuid the uuid.
     * @return the loaded User object.
     */
    @SneakyThrows
    public User fetchUser(String uuid) {
        for (User user : this.userCache) {
            if (user.getUuid().equals(uuid))
                return user;
        }
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/fetch-userdata/", WebRequest.RequestType.GET)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addHeader("uuid", uuid);
            Optional<String> response = web.execute().get();
            if (response.isPresent()) {
                User user = User.deserialize(Json.fromJson(response.get()));
                if (!user.isValid())
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
    public Map<String, Object> fetchUserdata(String uuid) {
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
     * Pushes a user's data to the database.
     *
     * @param username the username.
     * @param userdata the result of {@link User#serialize()}, or a copy.
     * @return true if successful.
     */
    @SneakyThrows
    public boolean pushUserdata(String username, Map<String, Object> userdata) {
        try {
            Map<String, Object> parent = new LinkedHashMap<>();
            parent.put(username, userdata);
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/push-userdata", WebRequest.RequestType.POST)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addParameter(parent);
            CompletableFuture<Optional<String>> future = web.execute();
            while (!future.isDone()) {}
            Optional<String> response = future.get();
            if (response.isPresent()) {
                Log.debug(response.get());
                return true;
            }
            return true;
        } catch (IOException ex) { Log.error(ex); }
        return false;
    }

    //</editor-fold>

    //<editor-fold desc="Bookdata">

    /**
     * Loads the bookdata of the given uuid and returns the usable object.
     *
     * @param uuid the uuid.
     * @return the loaded Book object.
     */
    @SneakyThrows
    public Book fetchBook(String uuid) {
        for (Book book : this.bookCache) {
            if (book.getUuid().equals(uuid)) {
                return book;
            }
        }
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/fetch-bookdata/", WebRequest.RequestType.GET)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addHeader("uuid", uuid);
            Optional<String> response = web.execute().get();
            if (response.isPresent()) {
                Book book = Book.deserialize(Json.fromJson(response.get()));
                if (!book.isValid())
                    return null;
                this.bookCache.add(book);
                return book;
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
    public Map<String, Object> fetchBookdata(String uuid) {
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/fetch-bookdata/", WebRequest.RequestType.GET)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addHeader("uuid", uuid);
            Optional<String> response = web.execute().get();
            if (response.isPresent())
                return Json.fromJson(response.get());
        } catch (IOException ex) { Log.error(ex); }
        return null;
    }

    /**
     * Pushes a book's data to the database.
     *
     * @param uuid the books uuid.
     * @param bookdata the result of {@link Book#serialize()}, or a copy.
     * @return true if successful.
     */
    @SneakyThrows
    public boolean pushBookdata(String uuid, Map<String, Object> bookdata) {
        try {
            Map<String, Object> parent = new LinkedHashMap<>();
            parent.put(uuid, bookdata);
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/push-bookdata", WebRequest.RequestType.POST)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addParameter(parent);
            CompletableFuture<Optional<String>> future = web.execute();
            while (!future.isDone()) {}
            Optional<String> response = future.get();
            if (response.isPresent()) {
                Log.debug(response.get());
                return true;
            }
            return true;
        } catch (IOException ex) { Log.error(ex); }
        return false;
    }

    //</editor-fold>

    //<editor-fold desc="Miscellaneous">

    @Nullable
    @SneakyThrows
    public String fetchUuid(String username) {
        try {
            WebRequest web = new WebRequest("https://voya-backend-cfb21ea1f03f.herokuapp.com/get-uuid-from-username", WebRequest.RequestType.GET)
                    .addHeader(Pair.create("Content-Type", "application/json"))
                    .addHeader(Pair.create("authorization", this.voyaToken))
                    .addHeader(Pair.create("username", username));
            CompletableFuture<Optional<String>> future = web.execute();
            while (!future.isDone()) {}
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
     * Gets the desired image from Dropbox.
     *
     * @param folderName the name of the folder.
     *                   For accurate results, use the {@link IOHandler#PAGE_IMAGE}
     *                   or {@link IOHandler#PROFILE_IMAGE} field.
     * @param imageName  the name of the image including extension.
     * @return the data of the image. If any errors are logged, will return nothing.
     */
    public byte[] getImage(String folderName, String imageName) {
        // TODO FIX
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
        while (!future.isDone()) {}
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            Log.error(ex);
            return new byte[0];
        }
    }

    /**
     * Used to tell the user the problem without hindering obtaining the result.
     */
    public enum Response {

        /**
         * Successful response.
         */
        SUCCESS,
        /**
         * An unexpected error arisen.
         */
        ERROR,
        /**
         * There was no internet.
         */
        NO_INTERNET,
        /**
         * The server didn't respond.
         */
        NO_RESPONSE,
        /**
         * An exception occurred.
         */
        EXCEPTION;

    }

    //</editor-fold>

}
