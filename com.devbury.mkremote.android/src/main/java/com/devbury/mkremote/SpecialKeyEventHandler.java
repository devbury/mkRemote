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
import android.view.KeyEvent;

public class SpecialKeyEventHandler implements KeyEventHandler {

    private KeyEventHandler handlerDelegate;
    private SharedPreferences sharedPreferences;
    private KeyProcessor keyProcessor;

    @Override
    public boolean processEvent(int keyCode, KeyEvent event) {
        if (!(event instanceof VirtualKeyEvent)) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences.cs(R.string.c_back_key),
                        null));
            }
            if (keyCode == KeyEvent.KEYCODE_CALL) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences.cs(R.string.c_call_key),
                        null));
            }
            if (keyCode == KeyEvent.KEYCODE_CAMERA) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                        .cs(R.string.c_camera_key), null));
            }
            if (keyCode == KeyEvent.KEYCODE_FOCUS) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                        .cs(R.string.c_camera_focus_key), null));
            }
            if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                        .cs(R.string.c_search_key), null));
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_boxee_mode_on), false)) {
                    return keyProcessor.processKey(MkRemotePreferences.cs(R.string.c_boxee_volume_up));
                } else {
                    return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                            .cs(R.string.c_volume_up_key), null));
                }
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_boxee_mode_on), false)) {
                    return keyProcessor.processKey(MkRemotePreferences.cs(R.string.c_boxee_volume_down));
                } else {
                    return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                            .cs(R.string.c_volume_down_key), null));
                }
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                        .cs(R.string.c_dpad_down_key), null));
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                        .cs(R.string.c_dpad_up_key), null));
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                        .cs(R.string.c_dpad_left_key), null));
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                        .cs(R.string.c_dpad_right_key), null));
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                return keyProcessor.processKey(sharedPreferences.getString(MkRemotePreferences
                        .cs(R.string.c_dpad_center_key), null));
            }
        }
        return handlerDelegate.processEvent(keyCode, event);
    }

    public KeyEventHandler getHandlerDelegate() {
        return handlerDelegate;
    }

    public void setHandlerDelegate(KeyEventHandler handlerDelegate) {
        this.handlerDelegate = handlerDelegate;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public KeyProcessor getKeyProcessor() {
        return keyProcessor;
    }

    public void setKeyProcessor(KeyProcessor keyProcessor) {
        this.keyProcessor = keyProcessor;
    }
}
