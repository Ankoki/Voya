package us.byeol.voya.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.web.IOHandler;

/**
 * This class controls all userdata that may be modified or needed to access books.
 */
public class User extends MongoMappable {

    /**
     * Deserializes a User from a map.
     *
     * @param map the map containing the userdata.
     * @return the User. May be invalid if any required fields were not found.
     */
    @NonNull
    public static User deserialize(Map<String, Object> map) {
        User user = new User();
        user.valid = user.mapResponse(map);
        return user;
    }

    private boolean valid;
    private String username,
            pfpPath,
            fullName,
            uuid,
            bio;
    private byte[] profilePicture;
    private final List<String> friends = new ArrayList<>(),
            friendRequests = new ArrayList<>();
    private final List<Book> books = new ArrayList<>(),
            bookInvites = new ArrayList<>();

    /**
     * To ensure we can only create new users internally.
     */
    private User() {
        super(user -> IOHandler.getInstance().fetchUser(((User) user).getUuid()).isValid(),
                user -> IOHandler.getInstance().pushUserdata(((User) user).getUsername(), user.serialize()));
    }

    /**
     * Whether or not this user exists/can be loaded again [may have malformed data].
     *
     * @return true if valid.
     */
    public boolean isValid() {
        return this.valid;
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
     * Gets the username of this user.
     *
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the name of the users profile picture.
     *
     * @return the name of the profile picture.
     */
    public String getProfilePictureName() {
        return this.pfpPath;
    }

    /**
     * Gets the profile picture of the user as a Bitmap.
     *
     * @return the profile picture.
     */
    public Bitmap getProfilePicture() {
        return BitmapFactory.decodeByteArray(this.profilePicture, 0, this.profilePicture.length);
    }

    /**
     * Get this users full name.
     *
     * @return the full name.
     */
    public String getFullName() {
        return this.fullName;
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
        this.fetchUpdates();
        this.bio = bio;
        this.pushChanges();
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
     * Gets all the friends this user has.
     *
     * @return the friends.
     */
    public User[] getFriends() {
        this.fetchUpdates();
        return this.friends.toArray(new User[0]);
    }

    /**
     * Adds a friend to this user.
     *
     * @param user the friend.
     */
    public void addFriend(User user) {
        this.fetchUpdates();
        this.friends.add(user.getUuid());
        this.pushChanges();
    }

    /**
     * Checks if this user has a friend request with the given uuid.
     *
     * @param uuid the uuid.
     * @return true if has a friend request.
     */
    public boolean hasFriendRequest(String uuid) {
        this.fetchUpdates();
        for (String user : this.friendRequests)
            if (user.equals(uuid))
                return true;
        return false;
    }

    /**
     * Adds a friend request the user will be able to accept.
     *
     * @param friendRequests the friend requests to add.
     */
    public void addFriendRequest(@Nullable String... friendRequests) {
        if (friendRequests == null)
            return;
        this.fetchUpdates();
        this.friendRequests.addAll(Arrays.asList(friendRequests));
        this.pushChanges();
    }

    /**
     * Gets all the books this user has access too.
     *
     * @return the books.
     */
    public Book[] getBooks() {
        this.fetchUpdates();
        return this.books.toArray(new Book[0]);
    }

    /**
     * Adds a book to this user.
     *
     * @param book the book.
     */
    public void addBook(Book book) {
        this.fetchUpdates();
        this.books.add(book);
        this.pushChanges();
    }

    /**
     * Checks if this user has a book invite with the given uuid.
     *
     * @param uuid the uuid.
     * @return true if has a book invite.
     */
    public boolean hasBookInvite(String uuid) {
        this.fetchUpdates();
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
        if (bookInvites == null || bookInvites.length == 0)
            return;
        this.fetchUpdates();
        this.bookInvites.addAll(Arrays.asList(bookInvites));
        this.pushChanges();
    }

    @Override
    protected boolean mapResponse(Map<String, Object> map) {
        if (map == null ||
                !map.containsKey("username") ||
                !map.containsKey("uuid") ||
                !map.containsKey("profile-picture") ||
                !map.containsKey("full-name") ||
                !map.containsKey("bio"))
            return false;
        this.friends.clear();
        this.friendRequests.clear();
        this.books.clear();
        this.bookInvites.clear();
        this.username = Misc.castKey(map, "username", String.class);
        this.uuid = Misc.castKey(map, "uuid", String.class);
        this.pfpPath = Misc.castKey(map, "profile-picture", String.class);
        this.fullName = Misc.castKey(map, "full-name", String.class);
        this.bio = Misc.castKey(map, "bio", String.class);
        if (map.containsKey("friends")) {
            String[] friends = Misc.castKey(map, "friends", String[].class);
            if (friends != null) {
                for (String friend : friends) {
                    User user = IOHandler.getInstance().fetchUser(friend);
                    if (user.isValid())
                        this.addFriend(user);
                }
            }
        }
        if (map.containsKey("friend-requests")) {
            List<String> friendRequests = Misc.castKey(map, "friend-requests", List.class);
            this.friendRequests.addAll(friendRequests);
        }
        if (map.containsKey("books")) {
            List<String> books = Misc.castKey(map, "books", List.class);
            if (books != null) {
                for (String uuid : books) {
                    Book book = IOHandler.getInstance().fetchBook(uuid);
                    if (book.isValid())
                        this.books.add(book);
                }
            }
        }
        if (map.containsKey("book-invites")) {
            String[] bookInvites = Misc.castKey(map, "book-invites", String[].class);
            if (bookInvites != null) {
                for (String invite : bookInvites) {
                    Book book = IOHandler.getInstance().fetchBook(invite);
                    if (book.isValid())
                        this.addBookInvite(book);
                }
            }
        }
        this.profilePicture = IOHandler.getInstance().getImage(IOHandler.PROFILE_IMAGE, this.pfpPath);
        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("username", this.username);
        map.put("uuid", this.uuid);
        map.put("profile-picture", this.pfpPath);
        map.put("full-name", this.fullName);
        map.put("bio", this.bio);
        map.put("friends", this.friends);
        map.put("friend-requests", this.friendRequests);
        String[] bookInvites = new String[this.bookInvites.size()];
        int i = 0;
        for (Book book : this.bookInvites) {
            bookInvites[i] = book.getUuid();
            i++;
        }
        map.put("book-invites", Arrays.asList(bookInvites));
        String[] books = new String[this.books.size()];
        i = 0;
        for (Book book : this.books) {
            books[i] = book.getUuid();
            i++;
        }
        map.put("books", Arrays.asList(books));
        return map;
    }

    @NonNull
    @Override
    public String toString() {
        return "User[username=" + this.username + ",\n" +
                    "uuid=" + this.uuid + ",\n" +
                    "pfpPath=" + this.pfpPath + ",\n" +
                    "fullName=" + this.fullName + ",\n" +
                    "bio=" + this.bio + ",\n" +
                    "friends=[" + String.join(", ", this.friends) + "],\n" +
                    "friendRequests=[" + String.join(", ", this.friendRequests) + "],\n" +
                    "books=[" + this.books.stream().map(Book::toString).collect(Collectors.joining(",\n")) + "],\n" +
                    "bookInvites=[" + this.bookInvites.stream().map(Book::toString).collect(Collectors.joining(",\n")) + "]\n" +
                    "]";
    }

}
