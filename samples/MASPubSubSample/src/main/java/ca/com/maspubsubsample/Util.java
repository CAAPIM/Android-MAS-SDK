package ca.com.maspubsubsample;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

public class Util {

    public static void showSnackbar(Context context, String message, CoordinatorLayout coordinatorLayout){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Welcome to AndroidHive", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
