package com.termux.api.apis;

import static android.content.Context.WINDOW_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.RequiresPermission;

import com.termux.api.TermuxApiReceiver;
import com.termux.api.util.ResultReturner;
import com.termux.shared.logger.Logger;

import java.io.PrintWriter;
import java.lang.reflect.Method;

public class WindowManagerAPI {

    private static final String LOG_TAG = "WindowManagerAPI";

    @RequiresPermission(Manifest.permission.WRITE_SETTINGS)
    public static void onReceive(TermuxApiReceiver apiReceiver, Context context, final Intent intent) {
        Logger.logDebug(LOG_TAG, "onReceive");
        // adb shell wm size reset|width-x-height
        // adb shell wm density reset|number

        // https://developer.android.com/guide/topics/resources/runtime-changes
        // ^^^ no, that's more for changing settings on the current app, I want changes for the whole system


        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.WithStringInput() {
            @RequiresPermission(allOf = { Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS })
            @Override
            public void writeResult(PrintWriter out) {
                Logger.logError(LOG_TAG, "CRAIG: start returnData(), context="+context);
                //DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                //Logger.logDebug(LOG_TAG, "metrics="+metrics);
                WindowManager wm = (WindowManager)context.getSystemService(WINDOW_SERVICE);
                Logger.logDebug(LOG_TAG, "wm="+wm);

                int displayId = wm.getDefaultDisplay().getDisplayId();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    WindowMetrics metrics = wm.getCurrentWindowMetrics();
                    // no way to set anything on windowmanager :(
                }
                Display display = wm.getDefaultDisplay();
                // no way to set anything on display :(

                try {
                    Class wmShellCommand = Class.forName("com.android.server.wm.WindowManagerShellCommand");
                    Logger.logDebug(LOG_TAG, "got wm shell command:" + wmShellCommand);
                } catch( Exception e) {
                    e.printStackTrace();
                }
                try {
                    Class wmInterfaceClz = Class.forName("android.view.IWindowManager");
                    Method[] delcaredMethods = wmInterfaceClz.getDeclaredMethods();
                    Method setSizeMethod = wmInterfaceClz.getDeclaredMethod("setForcedDisplaySize", Integer.TYPE, Integer.TYPE, Integer.TYPE);
                    setSizeMethod.invoke(wm, displayId, 1920, 1080);
                    Logger.logDebug(LOG_TAG, "CRAIG YEE HAW");
                } catch ( Exception e ) {
                    Logger.logDebug(LOG_TAG, "couldn't do class for name IWindowManager");
                    e.printStackTrace();
                }
                // sucky, api 28 blocks this reflection :( why can't we just control the damn device!? :(
                // I don't consider Android open source at all :(
/*
                @SuppressLint("SoonBlockedPrivateApi") Method setForcedDensityMethod = wmInterfaceClz.getDeclaredMethod("setForcedDisplayDensityForUser",
                        Integer.TYPE,
                        Integer.TYPE,
                        Integer.TYPE
                        );
*/
                //wm.getDefaultDisplay().getMetrics(metrics);
                //Logger.logDebug(LOG_TAG, "before, metrics="+metrics);
                //metrics.heightPixels = 1080;
                //metrics.widthPixels = 1920;
                //metrics.densityDpi = 200;
                //Logger.logDebug(LOG_TAG, "after, metrics="+metrics);
// compatScreenHeightDp, compatScreenWidthDp, densityDpi, windowConfiguration.mBounds
                //Configuration configuration = context.getResources().getConfiguration();
                //Logger.logDebug(LOG_TAG, "before, configuration="+configuration);
                //context.getResources().updateConfiguration(configuration, metrics);
                //Logger.logDebug(LOG_TAG, "after, configuration="+configuration);
                Logger.logError(LOG_TAG, "CRAIG: YO!");
            }
        });
    }
}
