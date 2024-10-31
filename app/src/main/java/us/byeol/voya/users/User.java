package us.byeol.voya.users;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import us.byeol.voya.misc.Misc;
import us.byeol.voya.storage.IOHandler;

public class User {

    /**
     * Deserializes a user from a Map.
     *
     * @param map the map containing the userdata.
     * @return the User. May be null if any required fields were not found.
     */
    @NonNull
    public static User deserialize(Map<String, Object> map) {
        User user = new User();
        user.update(map);
        return user;
    }

    private String username,
            pfpName,
            fullName,
            uuid,
            bio;
    private byte[] profilePicture;
    private final List<User> friends = new ArrayList<>(),
            friendRequests = new ArrayList<>();
    private final List<Book> bookInvites = new ArrayList<>(),
            standardBooks = new ArrayList<>(),
            adminBooks = new ArrayList<>();

    private User() {
    }

    /**
     * Gets the username of this user.
     *
     * @return the username.
     */
    @NotNull
    public String getUsername() {
        return username;
    }

    /**
     * Gets the name of the users profile picture.
     *
     * @return the name of the profile picture.
     */
    public String getProfilePictureName() {
        return this.pfpName;
    }

    /**
     * Gets the profile picture of the users profile picture.
     *
     * @return the name of the profile picture.
     */
    public byte[] getProfilePicture() {
        return profilePicture;
    }

    /**
     * Get this users full name.
     *
     * @return the full name.
     */
    @NotNull
    public String getFullName() {
        return this.fullName;
    }

    /**
     * Gets the UUID of this user.
     *
     * @return the UUID as a string.
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * Gets the bio of this user.
     *
     * @return the bio.
     */
    public String getBio() {
        return this.bio;
    }

    /**
     * Sets the bio of this user.
     *
     * @param bio the new bio.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Gets the first name of this user.
     *
     * @return the first name.
     */
    public String getFirstName() {
        return fullName.split(" ")[0];
    }

    /**
     * Gets the initials of this user.
     *
     * @return the initials.
     */
    public String getInitials() {
        return fullName.split(" ")[0].toUpperCase().split("")[0] + fullName.split(" ")[1].toUpperCase().split("")[0];
    }

    /**
     * Checks if this user has a friend request with the given uuid.
     *
     * @param uuid the uuid.
     * @return true if has a friend request.
     */
    public boolean hasFriendRequest(String uuid) {
        for (User user : this.friendRequests)
            if (user.getUuid().equals(uuid))
                return true;
        return false;
    }

    /**
     * Adds a friend request the user will be able to accept.
     *
     * @param friendRequests the friend requests to add.
     */
    public void addFriendRequest(String... friendRequests) {
        for (String uuid : friendRequests) {
            User user = IOHandler.getInstance().getCachedUser(uuid);
            if (user == null)
                continue;
            this.friendRequests.add(user);
        }
    }

    /**
     * Checks if this user has a book invite with the given uuid.
     *
     * @param uuid the uuid.
     * @return true if has a book invite.
     */
    public boolean hasBookInvite(String uuid) {
        for (Book book : this.bookInvites)
            if (book.getUuid().equals(uuid))
                return true;
        return false;
    }

    /**
     * Adds a book invite the user will be able to accept.
     *
     * @param bookInvites the books a user has been invited too.
     */
    public void addBookInvite(Book... bookInvites) {
        this.bookInvites.addAll(Arrays.asList(bookInvites));
    }

    /**
     * Checks if this user has access to the book with the given uuid.
     *
     * @param uuid the uuid to check.
     * @return true if has the book
     */
    public boolean hasBook(String uuid) {
        for (Book book : this.standardBooks)
            if (book.getUuid().equals(uuid))
                return true;
        for (Book book : this.adminBooks)
            if (book.getUuid().equals(uuid))
                return true;
        return false;
    }

    /**
     * Adds a standard book the user will be able to see.
     *
     * @param books the books.
     */
    public void addStandardBook(Book... books) {
        this.standardBooks.addAll(Arrays.asList(books));
    }

    /**
     * Adds an admin book the user will be able to see.
     *
     * @param books the books.
     */
    public void addAdminBook(Book... books) {
        this.adminBooks.addAll(Arrays.asList(books));
    }

    /**
     * Updates this users userdata with a map.
     * Please be careful using this method, it can cause breaking behaviours if keys are forgotten.
     *
     * @param map the map.
     */
    public void update(Map<String, Object> map) {
        this.username = Misc.castKey(map, "username", String.class);
        this.uuid = Misc.castKey(map, "uuid", String.class);
        this.pfpName = Misc.castKey(map, "profile-picture", String.class);
        this.fullName = Misc.castKey(map, "full-name", String.class);
        this.bio = Misc.castKey(map, "bio", String.class);
        String[] friendRequests = Misc.castKey(map, "friend-requests", String[].class);
        this.addFriendRequest(friendRequests);
        String[] bookInvites = Misc.castKey(map, "book-invites", String[].class);
        for (String invite : bookInvites)
            this.addBookInvite(IOHandler.getInstance().loadBook(invite));
        String[] standardBooks = Misc.castKey(map, "standard-books", String[].class);
        if (standardBooks == null)
            for (String book : standardBooks)
                this.addStandardBook(IOHandler.getInstance().loadBook(book));
        String[] adminBooks = Misc.castKey(map, "admin-books", String[].class);
        for (String book : adminBooks)
            this.addAdminBook(IOHandler.getInstance().loadBook(book));
        this.profilePicture = IOHandler.getInstance().getImage(IOHandler.PROFILE_IMAGE, this.pfpName);
    }

    /**
     * Updates this user with changes that are in the database. Should be called before any change is made to a user.
     */
    public void fetch() {
        this.update(IOHandler.getInstance().getUser(uuid));
    }

    /**
     * Pushes changes made to the database. Should be called every time a change is made with the user.
     */
    public void pushChanges() {
        IOHandler.getInstance().pushUser(this);
    }

    /**
     * Whether this user is an admin or not.
     *
     * @return true if an admin.
     */
    public boolean isAdmin(/*Book book*/) {
        return false /*book.isAdmin(this);*/;
    }

    /**
     * Serializes this user for database insertion.
     * <br>
     * Please remember to add the password field as soon as the request is being pushed, as the
     * password for the user should not be stored anywhere.
     *
     * @return the serialized user.
     */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("username", this.username);
        // TODO add password field to the map in any method it is used so that it doesn't get stored anywhere.
        map.put("uuid", this.uuid);
        map.put("profile-picture", this.pfpName);
        map.put("full-name", this.fullName);
        map.put("bio", this.bio);
        String[] friendRequests = new String[this.friendRequests.size()];
        int i = 0;
        for (User user : this.friendRequests) {
            friendRequests[i] = user.getUuid();
            i++;
        }
        map.put("friend-requests", friendRequests);
        String[] bookInvites = new String[this.bookInvites.size()];
        i = 0;
        for (Book book : this.bookInvites) {
            bookInvites[i] = book.getUuid();
            i++;
        }
        map.put("book-invites", bookInvites);
        String[] standardBooks = new String[this.standardBooks.size()];
        i = 0;
        for (Book book : this.standardBooks) {
            standardBooks[i] = book.getUuid();
            i++;
        }
        map.put("standard-books", standardBooks);
        String[] adminBooks = new String[this.adminBooks.size()];
        i = 0;
        for (Book book : this.adminBooks) {
            adminBooks[i] = book.getUuid();
            i++;
        }
        map.put("admin-books", adminBooks);
        return map;
    }

}
