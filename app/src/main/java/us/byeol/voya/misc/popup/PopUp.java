package us.byeol.voya.misc.popup;

import android.os.Build;
import android.view.View;

public interface PopUp {

    // TODO Find out if 34 supports Toast.
    PopUp instance = Build.VERSION.SDK_INT > 34 ? new SnackBarImpl() : new ToastImpl();

    /**
     * Shows the given message on the given view.
     *
     * @param view the view to show it on.
     * @param message the message to show.
     * @param duration the duration. Please note if {@link Length#LENGTH_INDEFINITE} is used on an
     *                 API that doesn't support Snackbar, {@link Length#LENGTH_LONG} will be used.
     */
    void showText(View view, String message, Length duration);

    enum Length {

        LENGTH_INDEFINITE(-2),
        LENGTH_SHORT(-1),
        LENGTH_LONG(0);

        private final int code;

        Length(int code) {
            this.code = code;
        }

        /**
         * Gets the code associated with this popup.
         *
         * @return the code.
         */
        public int getCode() {
            return code;
        }

    }

}
