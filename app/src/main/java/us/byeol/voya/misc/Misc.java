package us.byeol.voya.misc;

import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

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

    /**
     * Casts a key from a map to the given type.
     *
     * @param map the map to retrieve it from.
     * @param key the key.
     * @param clazz the class you want to cast the value too.
     * @return the casted value. May be null.
     * @param <Type> The type to cast to.
     */
    @Nullable
    public static <Type> Type castKey(Map<String, Object> map, String key, Class<? extends Type> clazz) {
        return castKey(map, key, null, clazz);
    }

    /**
     * Casts a key from a map to the given type.
     *
     * @param map the map to retrieve it from.
     * @param key the key.
     * @param backup the default response.
     * @param clazz the class you want to cast the value too.
     * @return the casted value. May be null.
     * @param <Type> The type to cast to.
     */
    @Nullable
    public static <Type> Type castKey(Map<String, Object> map, String key, Type backup, Class<? extends Type> clazz) {
        if (!map.containsKey(key))
            return null;
        Object rawValue = map.getOrDefault(key, backup);
        if (rawValue == null)
            return null;
        if (clazz.isInstance(rawValue))
            return (Type) rawValue;
        return null;
    }

}
