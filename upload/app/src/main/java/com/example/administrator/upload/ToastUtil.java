package com.example.administrator.upload;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * Created by zq on 2017/2/10.
 */
public class ToastUtil {
    public static Toast sToast;

    /**
     *
     * @param context
     * @param resId  资源文件-->string
     */
    public static void showToast(@NonNull Context context, int resId) {
        if (sToast == null) {
            sToast = Toast.makeText(context, "", Toast.LENGTH_LONG);
        }
        sToast.setText(resId);
        sToast.show();
    }


    public static void showToast(@NonNull Context context, String msg) {
        if (sToast == null) {
            sToast = Toast.makeText(context, "", Toast.LENGTH_LONG);
        }
        sToast.setText(msg);
        sToast.show();
    }
}
