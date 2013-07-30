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

import android.content.SharedPreferences;
import android.os.Vibrator;

public class VibrateClickFeedbackService implements ClickFeedbackService {
    public static final long VIBRATE_TIME = 20;
    private Vibrator vibrator;
    private SharedPreferences sharedPreferences;

    @Override
    public void provideFeedback() {
        if (sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_click_vibrate_on),
                MkRemotePreferences.DEFAULT_CLICK_VIBRATE_ON)) {
            vibrator.vibrate(VIBRATE_TIME);
        }
    }

    public Vibrator getVibrator() {
        return vibrator;
    }

    public void setVibrator(Vibrator vibrator) {
        this.vibrator = vibrator;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }
}
