package com.Mohammad.ac.test3g;

import android.app.Activity;
import android.content.pm.ActivityInfo;

/**
 * Created by mohammad.haider on 12/20/2014.
 */
public class myUtility {
    /** Static methods related to device orientation. */
    public static class OrientationUtils {
        private OrientationUtils() {}

        /** Locks the device window in landscape mode. */
        public static void lockOrientationLandscape(Activity activity) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        /** Locks the device window in portrait mode. */
        public static void lockOrientationPortrait(Activity activity) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        /** Allows user to freely use portrait or landscape mode. */
        public static void unlockOrientation(Activity activity) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

    }
}
