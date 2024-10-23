package us.byeol.voya.misc;

/**
 * Utility class to make debugging and logging errors easier and more readable.
 */
public class Log {

    /**
     * Sends a debug message to console.
     *
     * @param message the debug message.
     */
    public static void debug(String message) {
        System.out.println("[DEBUG] " + message);
    }

    /**
     * Sends an error message to console.
     *
     * @param message the error message.
     */
    public static void error(String message) {
        System.out.println("[ERROR] " + message);
    }

    /**
     * Sends an error message to console.
     *
     * @param ex the exception to get the message from.
     */
    public static void error(Exception ex) {
        Log.error(ex.getMessage());
    }

}
