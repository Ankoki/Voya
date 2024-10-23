package us.byeol.voya.misc;

import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;

public class Misc {

    /**
     * Sets the status bar colour of the given window.
     *
     * @param window the window to change the status bar colour of.
     * @param colour the colour to change the status bar to.
     */
    public static void setStatusBarColour(Window window, @ColorInt int colour) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(colour);
    }

}
