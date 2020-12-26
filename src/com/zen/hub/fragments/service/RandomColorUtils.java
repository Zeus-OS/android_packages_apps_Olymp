/*
 * Copyright (C) 2021 ZenX-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zen.hub.fragments.subs.service;

import static android.os.UserHandle.USER_SYSTEM;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.WindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.util.Log;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.R;


public class RandomColorUtils {

    private static final String TAG = "RandomColorServiceUtils";
    private static final boolean DEBUG = false;
    public static boolean mServiceEnabled = false;

    public static void enableService(Context context) {
        if (isRandomAccentColorIsActive(context) && !mServiceEnabled) {
            startService(context);
        } else if (!isRandomAccentColorIsActive(context) && mServiceEnabled) {
            stopService(context);
        }
    }

    private static void startService(Context context) {
        if (DEBUG) Log.d(TAG, "Starting service");
        context.startServiceAsUser(new Intent(context, RandomColorService.class),
                UserHandle.CURRENT);
        mServiceEnabled = true;
    }

    public static void restartService(Context context) {
       stopService(context);
       startService(context);
    }

    private static void stopService(Context context) {
        if (DEBUG) Log.d(TAG, "Stopping service");
        mServiceEnabled = false;
        context.stopServiceAsUser(new Intent(context, RandomColorService.class),
                UserHandle.CURRENT);
    }

    public static boolean isRandomAccentColorIsActive(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                Settings.System.ACCENT_RANDOM_COLOR, 0, UserHandle.USER_CURRENT) == 1;
    }
}