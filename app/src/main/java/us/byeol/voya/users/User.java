package us.byeol.voya.users;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User {

    public static User deserialize(Map<String, Object> map) {
        return null;
    }

    private final String username,
            fullName;
    private final List<String> friends = new ArrayList<>(),
            friendRequests = new ArrayList<>(),
            bookInvites = new ArrayList<>(),
            standardBooks = new ArrayList<>(),
            adminBooks = new ArrayList<>();

    public User(String username, String fullName) {
        this.username = username;
        this.fullName = fullName;
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
     * Get this users full name.
     *
     * @return the full name.
     */
    @NotNull
    public String getFullName() {
        return this.fullName;
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
     * Whether this user is an admin or not.
     *
     * @return true if an admin.
     */
    public boolean isAdmin(/*Book book*/) {
        return false /*book.isAdmin(this);*/;
    }

}
