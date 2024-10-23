package us.byeol.voya.misc.popup;

import android.annotation.SuppressLint;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackBarImpl implements PopUp {

    @SuppressLint("WrongConstant")
    @Override
    public void showText(View view, String message, PopUp.Length duration) {
        view.setElevation(10f);
        Snackbar.make(view, message, duration.getCode()).show();
    }

}
