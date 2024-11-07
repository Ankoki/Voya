package us.byeol.voya.misc;

/**
 * Utility class to make debugging and logging errors easier and more readable.
 */
public class Log {

    /**
     * Sends an informative message to console.
     *
     * @param message the informative message.
     */
    public static void info(String message) {
        System.out.println(message);
    }

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
        System.err.println("# " + ex.getClass().getName());
        System.err.println("# " + ex.getMessage());
        System.err.println("# ");
        for (StackTraceElement element : ex.getStackTrace())
            System.err.println("# " + element.toString());
    }

}
