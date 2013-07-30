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

package com.devbury.mkremote;

import java.util.HashMap;
import java.util.Map;

import com.devbury.android.ResourceConstants;

public class MkRemotePreferences extends ResourceConstants {

    public static final boolean DEFAULT_TOUCHSCREEN_ON = true;
    public static final boolean DEFAULT_TOUCH_CLICK_ON = true;
    public static final boolean DEFAULT_TRACKBALL_ON = false;
    public static final boolean DEFAULT_TRACKBALL_SCROLL_ON = false;
    public static final boolean DEFAULT_BUTTONBAR_ONE_ON = true;
    public static final boolean DEFAULT_BUTTONBAR_TWO_ON = false;
    public static final boolean DEFAULT_CLICK_VIBRATE_ON = true;
    public static final boolean DEFAULT_DPAD_MOUSE_ON = false;
    public static final boolean DEFAULT_AUTO_CONNECT_ON = true;
    public static Map<String, ButtonBarStatus> bbv = new HashMap<String, ButtonBarStatus>();

    public static void initDefaults() {
        bbv.put(cs(R.string.c_buttonbar1_lefthold_on), new ButtonBarStatus(R.id.buttonbar1_lefthold, true));
        bbv.put(cs(R.string.c_buttonbar1_left_on), new ButtonBarStatus(R.id.buttonbar1_left, false));
        bbv.put(cs(R.string.c_buttonbar1_middle_on), new ButtonBarStatus(R.id.buttonbar1_middle, false));
        bbv.put(cs(R.string.c_buttonbar1_middlehold_on), new ButtonBarStatus(R.id.buttonbar1_middlehold, false));
        bbv.put(cs(R.string.c_buttonbar1_right_on), new ButtonBarStatus(R.id.buttonbar1_right, true));
        bbv.put(cs(R.string.c_buttonbar1_righthold_on), new ButtonBarStatus(R.id.buttonbar1_righthold, false));
        bbv.put(cs(R.string.c_buttonbar1_esc_on), new ButtonBarStatus(R.id.buttonbar1_esc, false));
        bbv.put(cs(R.string.c_buttonbar1_f5_on), new ButtonBarStatus(R.id.buttonbar1_f5, false));
        bbv.put(cs(R.string.c_buttonbar1_pageup_on), new ButtonBarStatus(R.id.buttonbar1_pageup, false));
        bbv.put(cs(R.string.c_buttonbar1_pagedown_on), new ButtonBarStatus(R.id.buttonbar1_pagedown, false));
        bbv.put(cs(R.string.c_buttonbar1_enter_on), new ButtonBarStatus(R.id.buttonbar1_enter, false));
        bbv.put(cs(R.string.c_buttonbar1_maximize_on), new ButtonBarStatus(R.id.buttonbar1_maximize, false));
        bbv.put(cs(R.string.c_buttonbar1_show_virtual_keyboard_on), new ButtonBarStatus(
                R.id.buttonbar1_show_virtual_keyboard, false));

        bbv.put(cs(R.string.c_buttonbar2_lefthold_on), new ButtonBarStatus(R.id.buttonbar2_lefthold, false));
        bbv.put(cs(R.string.c_buttonbar2_left_on), new ButtonBarStatus(R.id.buttonbar2_left, false));
        bbv.put(cs(R.string.c_buttonbar2_middle_on), new ButtonBarStatus(R.id.buttonbar2_middle, false));
        bbv.put(cs(R.string.c_buttonbar2_middlehold_on), new ButtonBarStatus(R.id.buttonbar2_middlehold, false));
        bbv.put(cs(R.string.c_buttonbar2_right_on), new ButtonBarStatus(R.id.buttonbar2_right, false));
        bbv.put(cs(R.string.c_buttonbar2_righthold_on), new ButtonBarStatus(R.id.buttonbar2_righthold, false));
        bbv.put(cs(R.string.c_buttonbar2_esc_on), new ButtonBarStatus(R.id.buttonbar2_esc, false));
        bbv.put(cs(R.string.c_buttonbar2_f5_on), new ButtonBarStatus(R.id.buttonbar2_f5, false));
        bbv.put(cs(R.string.c_buttonbar2_pageup_on), new ButtonBarStatus(R.id.buttonbar2_pageup, false));
        bbv.put(cs(R.string.c_buttonbar2_pagedown_on), new ButtonBarStatus(R.id.buttonbar2_pagedown, false));
        bbv.put(cs(R.string.c_buttonbar2_enter_on), new ButtonBarStatus(R.id.buttonbar2_enter, false));
        bbv.put(cs(R.string.c_buttonbar2_maximize_on), new ButtonBarStatus(R.id.buttonbar2_maximize, false));
        bbv.put(cs(R.string.c_buttonbar2_show_virtual_keyboard_on), new ButtonBarStatus(
                R.id.buttonbar2_show_virtual_keyboard, false));
        bbv.put(cs(R.string.c_buttonbar1_on), new ButtonBarStatus(R.id.buttonbar1, true));
        bbv.put(cs(R.string.c_buttonbar2_on), new ButtonBarStatus(R.id.buttonbar2, false));

        setDefault(R.string.c_touchscreen_on, DEFAULT_TOUCHSCREEN_ON);
        setDefault(R.string.c_touch_click_on, DEFAULT_TOUCH_CLICK_ON);
        setDefault(R.string.c_trackball_on, DEFAULT_TRACKBALL_ON);
        setDefault(R.string.c_trackball_scroll_up_down_on, DEFAULT_TRACKBALL_SCROLL_ON);
        setDefault(R.string.c_trackball_scroll_left_right_on, DEFAULT_TRACKBALL_SCROLL_ON);
        setDefault(R.string.c_trackball_key, cs(R.string.c_default));
        setDefault(R.string.c_call_key, cs(R.string.c_default));
        setDefault(R.string.c_back_key, cs(R.string.c_default));
        setDefault(R.string.c_camera_key, cs(R.string.c_toggle_boxee_mode));
        setDefault(R.string.c_camera_focus_key, cs(R.string.c_default));
        setDefault(R.string.c_search_key, cs(R.string.c_toggle_boxee_mode));
        setDefault(R.string.c_volume_up_key, cs(R.string.c_default));
        setDefault(R.string.c_volume_down_key, cs(R.string.c_default));
        setDefault(R.string.c_click_vibrate_on, DEFAULT_CLICK_VIBRATE_ON);
        setDefault(R.string.c_default_orientation, cs(R.string.c_portrait));

        setDefault(R.string.c_dpad_center_key, cs(R.string.c_left_mouse_click));
        setDefault(R.string.c_dpad_up_key, cs(R.string.c_up_arrow));
        setDefault(R.string.c_dpad_down_key, cs(R.string.c_down_arrow));
        setDefault(R.string.c_dpad_left_key, cs(R.string.c_left_arrow));
        setDefault(R.string.c_dpad_right_key, cs(R.string.c_right_arrow));
        setDefault(R.string.c_dpad_mouse_on, DEFAULT_DPAD_MOUSE_ON);

        setDefault(R.string.c_auto_connect_on, DEFAULT_AUTO_CONNECT_ON);
        setDefault(R.string.c_bluetooth_on, false);
        setDefault(R.string.c_wake_lock_on, false);
        setDefault(R.string.c_mouse_sensitivity, "1");
        setDefault(R.string.c_boxee_mode_on, false);
        setDefault(R.string.c_long_press_scroll_on, true);
        setDefault(R.string.c_wallpaper_on, true);
        setDefault(R.string.c_notification_bar_on, false);
        setDefault(R.string.c_lock_on, false);
    }
}
