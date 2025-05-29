package com.gabriele.nextstop;


import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    private final Activity activity;
    private final String permission;
    private final int requestCode;
    private PermissionCallback callback;

    public PermissionHelper(Activity activity, String permission, int requestCode) {
        this.activity = activity;
        this.permission = permission;
        this.requestCode = requestCode;
    }

    public void checkAndRequestPermission(PermissionCallback callback) {
        this.callback = callback;

        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            // Permesso già concesso
            callback.onPermissionGranted();
        } else {
            // Richiedi permesso
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == this.requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (callback != null) callback.onPermissionGranted();
            } else {
                if (callback != null) callback.onPermissionDenied();
            }
        }
    }
}
