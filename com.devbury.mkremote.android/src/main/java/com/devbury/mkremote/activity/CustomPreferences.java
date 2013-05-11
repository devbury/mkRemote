/*
 * Copyright (c) 2009-2013 devBury LLC
 *
 *   This file is part of mkRemote.
 *
 *   mkRemote is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License Version 3
 *   as published by the Free Software Foundation.
 *
 *   mkRemote is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with mkRemote.  If not, see <http://www.gnu.org/licenses/gpl.txt/>.
 */

package com.devbury.mkremote.activity;

import com.devbury.logging.Logger;
import com.devbury.mkremote.MkRemoteApplication;
import com.devbury.mkremote.MkRemotePreferences;
import com.devbury.mkremote.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class CustomPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private int[] listPreferences = {R.string.c_dpad_center_key, R.string.c_dpad_down_key, R.string.c_dpad_left_key,
            R.string.c_dpad_right_key, R.string.c_dpad_up_key, R.string.c_default_orientation,
            R.string.c_trackball_key, R.string.c_call_key, R.string.c_back_key, R.string.c_camera_key,
            R.string.c_volume_up_key, R.string.c_volume_down_key, R.string.c_mouse_sensitivity, R.string.c_search_key,
            R.string.c_camera_focus_key};
    private MkRemoteApplication app;

    protected String s(int r) {
        return MkRemotePreferences.cs(r);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(s(R.string.c_notification_bar_on))) {
            app.windowAttributeHelper.processNotificationBar(getWindow(), sharedPreferences.getBoolean(key, false));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.debug("Unregistering");
        app.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MkRemoteApplication) getApplication();
        onSharedPreferenceChanged(app.sharedPreferences, s(R.string.c_notification_bar_on));
        app.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.preferences);

        Preference p = findPreference(s(R.string.c_bluetooth_details));
        Preference bluetooth_on = findPreference(s(R.string.c_bluetooth_on));
        bluetooth_on.setEnabled(false);
        if (app.isLite()) {
            ((CheckBoxPreference) bluetooth_on).setChecked(false);
            bluetooth_on.setSummary(R.string.only_in_full_version);
        }
        switch (app.bluetoothType) {
            case MkRemoteApplication.BLUETOOTH_NATIVE:
                p.setTitle(R.string.bluetooth_native);
                p.setSummary(R.string.bluetooth_native_summary);
                if (!app.isLite()) {
                    bluetooth_on.setEnabled(true);
                }
                break;
            case MkRemoteApplication.BLUETOOTH_EXPERIMENTAL:
                p.setTitle(R.string.bluetooth_experimental);
                p.setSummary(R.string.bluetooth_experimental_summary);
                if (!app.isLite()) {
                    bluetooth_on.setEnabled(true);
                }
                break;
            default:
                p.setTitle(R.string.bluetooth_none);
                p.setSummary(R.string.bluetooth_none_summary);
        }

        p = findPreference(s(R.string.c_boxee_mode_on));
        if (app.isLite()) {
            p.setEnabled(false);
            ((CheckBoxPreference) p).setChecked(false);
            p.setSummary(R.string.only_in_full_version);
        }

        initListPreferenceSummaries();

        deviceSpecificSettings();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
                && newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES
                && newConfig.orientation != app.getDefaultOrientation()) {
            setRequestedOrientation(app.getDefaultScreenOrientation());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateOrientation();
    }

    protected void updateOrientation() {
        MkRemoteApplication a = (MkRemoteApplication) getApplication();
        if (getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES
                && getResources().getConfiguration().orientation != a.getDefaultOrientation()) {
            setRequestedOrientation(a.getDefaultScreenOrientation());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey() != null) {
            if (preference.getKey().equals(s(R.string.c_trackball_on))) {
                CheckBoxPreference box = (CheckBoxPreference) preference;
                if (box.isChecked()) {
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_wheel_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_scroll_up_down_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_scroll_left_right_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                }
            } else if (preference.getKey().equals(s(R.string.c_trackball_scroll_up_down_on))) {
                CheckBoxPreference box = (CheckBoxPreference) preference;
                if (box.isChecked()) {
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_wheel_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                }
            } else if (preference.getKey().equals(s(R.string.c_trackball_scroll_left_right_on))) {
                CheckBoxPreference box = (CheckBoxPreference) preference;
                box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_wheel_on));
                if (box.isChecked()) {
                    box.setChecked(false);
                }
                if (box.isChecked()) {
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                }
            } else if (preference.getKey().equals(s(R.string.c_trackball_wheel_on))) {
                CheckBoxPreference box = (CheckBoxPreference) preference;
                if (box.isChecked()) {
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_scroll_up_down_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                    box = (CheckBoxPreference) findPreference(s(R.string.c_trackball_scroll_left_right_on));
                    if (box.isChecked()) {
                        box.setChecked(false);
                    }
                }
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    protected void initListPreferenceSummaries() {
        Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference lp = (ListPreference) preference;
                CharSequence[] values = lp.getEntryValues();
                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(newValue)) {
                        lp.setSummary(lp.getEntries()[i]);
                        if (lp.getKey().equals(s(R.string.c_default_orientation))) {
                            String s = (String) newValue;
                            int o;
                            int os;
                            if (s.equals(s(R.string.c_portrait))) {
                                o = Configuration.ORIENTATION_PORTRAIT;
                                os = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            } else {
                                o = Configuration.ORIENTATION_LANDSCAPE;
                                os = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            }
                            if (getResources().getConfiguration().orientation != o) {
                                setRequestedOrientation(os);
                            }
                        }
                        return true;
                    }
                }
                return true;
            }
        };

        for (int id : listPreferences) {
            ListPreference p = (ListPreference) findPreference(s(id));
            if (p != null) {
                p.setSummary(p.getEntry());
                p.setOnPreferenceChangeListener(listener);
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        updateOrientation();
    }

    public void deviceSpecificSettings() {
        boolean remove_dpad = false;
        boolean remove_trackball = false;

        int nav = getResources().getConfiguration().navigation;
        if (nav == Configuration.NAVIGATION_DPAD) {
            remove_trackball = true;
        } else if (nav == Configuration.NAVIGATION_TRACKBALL) {
            remove_dpad = true;
        }

        PreferenceScreen ps = (PreferenceScreen) findPreference(s(R.string.c_preference_screen_key));
        if (remove_dpad) {
            ps.removePreference(findPreference(s(R.string.dpad_settings_category)));
        }
        if (remove_trackball) {
            ps.removePreference(findPreference(s(R.string.trackball_settings_category)));
        }
    }
}
