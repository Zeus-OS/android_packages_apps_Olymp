/*
 * Copyright (C) 2020 Zeus-OS
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
package com.settings.olymp.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.UserHandle;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.Context;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.internal.util.zeus.ThemesUtils;
import android.content.om.IOverlayManager;
import android.content.SharedPreferences;
import android.os.ServiceManager;
import android.content.om.OverlayInfo;
import android.graphics.Color;
import android.os.SystemProperties;
import android.os.RemoteException;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

import static com.settings.olymp.utils.Utils.handleOverlays;
import com.zeus.support.preferences.SystemSettingListPreference;
import com.zeus.support.colorpicker.ColorPickerPreference;
import com.android.internal.util.zeus.ZeusUtils;
import com.zeus.support.preferences.CustomSeekBarPreference;

import lineageos.hardware.LineageHardwareManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;

public class UserInterfaceSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String DUAL_STATUSBAR_ROW_MODE = "dual_statusbar_row_mode";
    private static final String DUAL_ROW_DATAUSAGE = "dual_row_datausage";
    private static final String STATUS_BAR_HEIGHT = "status_bar_height";

    private static final String DISPLAY_CUTOUT_MODE = "display_cutout_mode";
    private static final String STOCK_STATUSBAR = "stock_statusbar_in_hide";

    private IOverlayManager mOverlayManager;
    private SharedPreferences mSharedPreferences;
    private SystemSettingListPreference mStatusbarDualRowMode;
    private SystemSettingListPreference mDualRowDataUsageMode;
    private ListPreference mStatusbarHeight;

    private Preference mStockStatusbar;
    private ListPreference mImmersiveMode;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.olymp_userinterface);
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));

        boolean mIsDualStatusbarDefaultEnabled = getContext().getResources().getBoolean(
            com.android.internal.R.bool.config_default_dual_status_bar);
        int mDualStatusbarDefaultMode = getContext().getResources().getInteger(
            com.android.internal.R.integer.config_default_dual_status_bar_mode);

        if(mIsDualStatusbarDefaultEnabled) {
            mStatusbarDualRowMode = (SystemSettingListPreference) findPreference(DUAL_STATUSBAR_ROW_MODE);
            mStatusbarDualRowMode.setValue(String.valueOf(mDualStatusbarDefaultMode));
            mStatusbarDualRowMode.setSummary(mStatusbarDualRowMode.getEntry());
            mStatusbarDualRowMode.setOnPreferenceChangeListener(this);
            mIsDualStatusbarDefaultEnabled = false;
        } else {
            mStatusbarDualRowMode = (SystemSettingListPreference) findPreference(DUAL_STATUSBAR_ROW_MODE);
            int statusbarDualRowMode = Settings.System.getIntForUser(getActivity().getContentResolver(),
                    Settings.System.DUAL_STATUSBAR_ROW_MODE, 0, UserHandle.USER_CURRENT);
            mStatusbarDualRowMode.setValue(String.valueOf(statusbarDualRowMode));
            mStatusbarDualRowMode.setSummary(mStatusbarDualRowMode.getEntry());
            mStatusbarDualRowMode.setOnPreferenceChangeListener(this);
        }
        mStatusbarHeight = (ListPreference) findPreference(STATUS_BAR_HEIGHT);
        int StatusbarHeight = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_HEIGHT, 0);
        int StatusbarHeightValue = getOverlayPosition(ThemesUtils.STATUSBAR_HEIGHT);
        if (StatusbarHeightValue != 0) {
            mStatusbarHeight.setValue(String.valueOf(StatusbarHeight));
        }
        mStatusbarHeight.setValue(String.valueOf(StatusbarHeight));
        mStatusbarHeight.setSummary(mStatusbarHeight.getEntry());
        mStatusbarHeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference == mStatusbarHeight) {
                    String value = (String) newValue;
                    Settings.System.putInt(getActivity().getContentResolver(), Settings.System.STATUS_BAR_HEIGHT, Integer.valueOf(value));
                    int valueIndex = mStatusbarHeight.findIndexOfValue(value);
                    mStatusbarHeight.setSummary(mStatusbarHeight.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.STATUSBAR_HEIGHT);
                    if (overlayName != null) {
                        handleOverlays(overlayName, false, mOverlayManager);
                    }
                    if (valueIndex != 0) {
                        handleOverlays(ThemesUtils.STATUSBAR_HEIGHT[valueIndex],
                                true, mOverlayManager);
                    }
                    return true;
                }
                return false;
            }
       });

       final PreferenceScreen prefScreen = getPreferenceScreen();

       mImmersiveMode = (ListPreference) prefScreen.findPreference(DISPLAY_CUTOUT_MODE);
       mImmersiveMode.setOnPreferenceChangeListener(this);

        int immersiveMode = Settings.System.getInt(getContentResolver(),
                Settings.System.DISPLAY_CUTOUT_MODE, 0);

        mStockStatusbar = (Preference) prefScreen.findPreference(STOCK_STATUSBAR);
        mStockStatusbar.setEnabled(immersiveMode == 2);

        handleDataUsePreferences();

    }

    private void handleDataUsePreferences() {

        int dualRowMode = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.DUAL_STATUSBAR_ROW_MODE, 0);

        if(dualRowMode == 3) {
            mDualRowDataUsageMode = (SystemSettingListPreference) findPreference(DUAL_ROW_DATAUSAGE);
            int dualRowDataUsageMode = Settings.System.getIntForUser(getActivity().getContentResolver(),
                    Settings.System.DUAL_ROW_DATAUSAGE, 0, UserHandle.USER_CURRENT);
            mDualRowDataUsageMode.setValue(String.valueOf(dualRowDataUsageMode));
            mDualRowDataUsageMode.setSummary(mDualRowDataUsageMode.getEntry());
            mDualRowDataUsageMode.setOnPreferenceChangeListener(this);
            mDualRowDataUsageMode.setVisible(true);
        } else {
            mDualRowDataUsageMode = (SystemSettingListPreference) findPreference(DUAL_ROW_DATAUSAGE);
            if(mDualRowDataUsageMode != null) {
                mDualRowDataUsageMode.setVisible(false);
            }
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusbarDualRowMode) {
            int statusbarDualRowMode = Integer.parseInt((String) newValue);
            int statusbarDualRowModeIndex = mStatusbarDualRowMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DUAL_STATUSBAR_ROW_MODE, statusbarDualRowMode);
            mStatusbarDualRowMode.setSummary(mStatusbarDualRowMode.getEntries()[statusbarDualRowModeIndex]);
            ZeusUtils.showSystemUiRestartDialog(getContext());
            handleDataUsePreferences();
            return true;
        } else if (preference == mDualRowDataUsageMode) {
            int dualRowDataUsageMode = Integer.parseInt((String) newValue);
            int dualRowDataUsageModeIndex = mDualRowDataUsageMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DUAL_ROW_DATAUSAGE, dualRowDataUsageMode);
            mDualRowDataUsageMode.setSummary(mDualRowDataUsageMode.getEntries()[dualRowDataUsageModeIndex]);
            return true;
        } else if (preference == mImmersiveMode) {
            int value = Integer.valueOf((String) newValue);
            mStockStatusbar.setEnabled(value == 2);
            return true;
        }
        return false;
    }


    private String getOverlayName(String[] overlays) {
            String overlayName = null;
            for (int i = 0; i < overlays.length; i++) {
                String overlay = overlays[i];
                if (ZeusUtils.isThemeEnabled(overlay)) {
                    overlayName = overlay;
                }
            }
            return overlayName;
        }

    private int getOverlayPosition(String[] overlays) {
            int position = -1;
            for (int i = 0; i < overlays.length; i++) {
                String overlay = overlays[i];
                if (ZeusUtils.isThemeEnabled(overlay)) {
                    position = i;
                }
            }
        return position;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ZEUS_SETTINGS;
    }
}
