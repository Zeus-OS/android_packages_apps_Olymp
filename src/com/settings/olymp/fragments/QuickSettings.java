/*
 *  Copyright (C) 2020 Zeus-OS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.settings.olymp.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.content.ContentResolver;
import android.net.Uri;

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
import android.content.Context;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

import static com.settings.olymp.utils.Utils.handleOverlays;
import com.zeus.support.preferences.SystemSettingListPreference;
import com.zeus.support.colorpicker.ColorPickerPreference;
import com.zeus.support.preferences.CustomSeekBarPreference;
import com.zeus.support.preferences.SystemSettingSwitchPreference;
import com.android.internal.util.zeus.ZeusUtils;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String BRIGHTNESS_SLIDER_STYLE = "brightness_slider_style";
    private static final String PREF_TILE_STYLE = "qs_tile_style";
    private static final String QS_MEDIA_DIVIDER = "qs_media_divider";
    private static final String QS_MEDIA_DIVIDER_COLOR_MODE = "qs_media_divider_color_mode";
    private static final String QS_MEDIA_DIVIDER_RANDOM_COLOR_INTERVAL = "qs_media_divider_random_color_interval";
    private static final String BRIGHTNESS_SLIDER_THUMB = "brightness_slider_thumb";
    private static final String QS_SYSTEM_INFO_ICON = "qs_system_info_icon";
    private static final String QS_SYSTEM_INFO = "qs_system_info";

    private IOverlayManager mOverlayManager;
    private ListPreference mBrightnessSliderStyle;
    private ListPreference mQsTileStyle;
    private SystemSettingListPreference mQsColorMode;
    private SystemSettingSwitchPreference mQsColorDivider;
    private CustomSeekBarPreference mMediaDividerRandomColorInterval;
    private ListPreference mBrightnessThumbStyle;
    private SystemSettingSwitchPreference mShowSystemInfoIcon;
    private SystemSettingListPreference mShowSystemInfo;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.olymp_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mOverlayManager = IOverlayManager.Stub.asInterface(
            ServiceManager.getService(Context.OVERLAY_SERVICE));

        mBrightnessSliderStyle = (ListPreference) findPreference(BRIGHTNESS_SLIDER_STYLE);
        int BrightnessSliderStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BRIGHTNESS_SLIDER_STYLE, 0);
        int BrightnessSliderStyleValue = getOverlayPosition(ThemesUtils.BRIGHTNESS_SLIDER_THEMES);
        if (BrightnessSliderStyleValue != 0) {
            mBrightnessSliderStyle.setValue(String.valueOf(BrightnessSliderStyle));
        }
        mBrightnessSliderStyle.setSummary(mBrightnessSliderStyle.getEntry());
        mBrightnessSliderStyle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference == mBrightnessSliderStyle) {
                    String value = (String) newValue;
                    Settings.System.putInt(getActivity().getContentResolver(), Settings.System.BRIGHTNESS_SLIDER_STYLE, Integer.valueOf(value));
                    int valueIndex = mBrightnessSliderStyle.findIndexOfValue(value);
                    mBrightnessSliderStyle.setSummary(mBrightnessSliderStyle.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.BRIGHTNESS_SLIDER_THEMES);
                    if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                    }
                    if (valueIndex > 0) {
                        handleOverlays(ThemesUtils.BRIGHTNESS_SLIDER_THEMES[valueIndex],
                                true, mOverlayManager);
                    }
                    return true;
                }
                return false;
            }});

        mShowSystemInfoIcon = (SystemSettingSwitchPreference) findPreference(QS_SYSTEM_INFO_ICON);
        mShowSystemInfoIcon.setOnPreferenceChangeListener(this);
        updateSystemIconView();

        mBrightnessThumbStyle = (ListPreference) findPreference(BRIGHTNESS_SLIDER_THUMB);
        int BrightnessThumbStyle = Settings.System.getInt(resolver,
                Settings.System.BRIGHTNESS_SLIDER_THUMB, 0);
        int BrightnessThumbStyleValue = getOverlayPosition(ThemesUtils.BRIGHTNESS_SLIDER_THUMB);
        if (BrightnessThumbStyleValue != 0) {
            mBrightnessThumbStyle.setValue(String.valueOf(BrightnessThumbStyle));
        }
        mBrightnessThumbStyle.setSummary(mBrightnessThumbStyle.getEntry());
        mBrightnessThumbStyle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference == mBrightnessThumbStyle) {
                    String value = (String) newValue;
                    Settings.System.putInt(resolver, Settings.System.BRIGHTNESS_SLIDER_THUMB, Integer.valueOf(value));
                    int valueIndex = mBrightnessThumbStyle.findIndexOfValue(value);
                    mBrightnessThumbStyle.setSummary(mBrightnessThumbStyle.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.BRIGHTNESS_SLIDER_THUMB);
                    if (overlayName != null) {
                        handleOverlays(overlayName, false, mOverlayManager);
                    }               
                    if (valueIndex > 0) {
                        handleOverlays(ThemesUtils.BRIGHTNESS_SLIDER_THUMB[valueIndex],
                                true, mOverlayManager);
                    }
                    return true;
                }
                return false;
            }});

            mQsTileStyle = (ListPreference) findPreference(PREF_TILE_STYLE);
        int qsTileStyle = Settings.System.getInt(resolver,
                Settings.System.QS_TILE_STYLE, 0);
        int qsTileStyleValue = getOverlayPosition(ThemesUtils.QS_TILE_THEMES);
        if (qsTileStyleValue != 0) {
            mQsTileStyle.setValue(String.valueOf(qsTileStyle));
        }
        mQsTileStyle.setSummary(mQsTileStyle.getEntry());
        mQsTileStyle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference == mQsTileStyle) {
                    String value = (String) newValue;
                    Settings.System.putInt(resolver, Settings.System.QS_TILE_STYLE, Integer.valueOf(value));
                    int valueIndex = mQsTileStyle.findIndexOfValue(value);
                    mQsTileStyle.setSummary(mQsTileStyle.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.QS_TILE_THEMES);
                    if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                    }
                    if (valueIndex > 0) {
                        handleOverlays(ThemesUtils.QS_TILE_THEMES[valueIndex],
                                true, mOverlayManager);
                    }
                    return true;
                }
                return false;
            }
       });

        mQsColorDivider = (SystemSettingSwitchPreference) findPreference(QS_MEDIA_DIVIDER);
        mQsColorDivider.setOnPreferenceChangeListener(this);

        mQsColorMode = (SystemSettingListPreference) findPreference(QS_MEDIA_DIVIDER_COLOR_MODE);
        mQsColorMode.setOnPreferenceChangeListener(this);

        mShowSystemInfo = (SystemSettingListPreference) findPreference(QS_SYSTEM_INFO);
        mShowSystemInfo.setOnPreferenceChangeListener(this);

        mMediaDividerRandomColorInterval = (CustomSeekBarPreference) findPreference(QS_MEDIA_DIVIDER_RANDOM_COLOR_INTERVAL);
        int time = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_MEDIA_DIVIDER_RANDOM_COLOR_INTERVAL, 3);
        mMediaDividerRandomColorInterval.setValue(time);
        mMediaDividerRandomColorInterval.setOnPreferenceChangeListener(this);

        handleMediaDividerVisibilty();

    }

    private int getQsSystemInfoMode() {
        return Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.QS_SYSTEM_INFO, 0);
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQsColorMode) {
                int val = Integer.parseInt((String) newValue);
                int index = mQsColorMode.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_MEDIA_DIVIDER_COLOR_MODE, val);
                mQsColorMode.setSummary(mQsColorMode.getEntries()[index]);
                handleMediaDividerVisibilty();
                return true;
        } else if (preference == mMediaDividerRandomColorInterval) {
            int time = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_MEDIA_DIVIDER_RANDOM_COLOR_INTERVAL, time);
            return true;
       } else if (preference == mQsColorDivider) {
            Boolean value = (Boolean) newValue;
            mQsColorDivider.setChecked(value);
            handleMediaDividerVisibilty();
            return true;
       } else if (preference == mShowSystemInfo) {
                int val1 = Integer.parseInt((String) newValue);
                int i = mShowSystemInfo.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_SYSTEM_INFO, val1);
                    mShowSystemInfo.setSummary(mShowSystemInfo.getEntries()[i]);
            updateSystemIconView();
            return true;
        }
        return false;
    }

    private int getMediaDividerColorMode() {
        return Settings.System.getInt(getContentResolver(),
                Settings.System.QS_MEDIA_DIVIDER_COLOR_MODE, 2);
    }

    private boolean getMediaDivider() {
        return Settings.System.getInt(getContentResolver(),
        Settings.System.QS_MEDIA_DIVIDER, 1) == 1;
    }

    private void updateSystemIconView() {
        if(getQsSystemInfoMode() > 0) {
            mShowSystemInfoIcon.setVisible(true);
         } else {
            mShowSystemInfoIcon.setVisible(false);
         }
    }

    private void handleMediaDividerVisibilty() {
        if(!getMediaDivider()) {
            mMediaDividerRandomColorInterval.setVisible(false);
            mQsColorMode.setVisible(false);
        } else {
            mQsColorMode.setVisible(true);
            switch (getMediaDividerColorMode()) {
                case 0:
                    mMediaDividerRandomColorInterval.setVisible(false);
                    break;
                case 1:
                    mMediaDividerRandomColorInterval.setVisible(false);
                    break;
                case 2:
                    mMediaDividerRandomColorInterval.setVisible(true);
                    break;
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ZEUS_SETTINGS;
    }

}
