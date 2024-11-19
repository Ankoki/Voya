package us.byeol.voya.misc.popup;

import android.annotation.SuppressLint;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackBarImpl implements PopUp {

    @SuppressLint("WrongConstant")
    @Override
    public void showText(View view, String message, PopUp.Length duration) {
        // TODO add the ability to specify options, such as background colour and actions.
        Snackbar.make(view, message, duration.getCode()).show();
    }

}
