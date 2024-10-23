package us.byeol.voya.misc.popup;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Toast;

public class ToastImpl implements PopUp {

    @SuppressLint("WrongConstant")
    @Override
    public void showText(View view, String message, PopUp.Length duration) {
        view.setElevation(10f);
        Toast.makeText(view.getContext(), message, duration.getCode() == -2 ? 0 : duration.getCode()).show();
    }

}
