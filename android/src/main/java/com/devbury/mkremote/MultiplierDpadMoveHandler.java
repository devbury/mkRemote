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

import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.connections.ServerConnection;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.KeyEvent;

public class MultiplierDpadMoveHandler implements DpadMoveHandler, OnSharedPreferenceChangeListener {

    private int multiplier = 1;
    private DataPacket dp = new DataPacket(DataPacket.MOUSE_MOVE);
    private ServerConnection serverConnection;
    private long lastMove = now();
    private String mouseSensitivityKey = MkRemotePreferences.cs(R.string.c_mouse_sensitivity);
    private String dpadOnKey = MkRemotePreferences.cs(R.string.c_dpad_mouse_on);
    private boolean dpadOn = false;

    public MultiplierDpadMoveHandler(SharedPreferences sp) {
        sp.registerOnSharedPreferenceChangeListener(this);
        init(sp);
    }

    public void init(SharedPreferences sp) {
        onSharedPreferenceChanged(sp, mouseSensitivityKey);
        onSharedPreferenceChanged(sp, dpadOnKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(mouseSensitivityKey)) {
            multiplier = Integer.valueOf(sp.getString(key, "1"));
            return;
        }
        if (key.equals(dpadOnKey)) {
            dpadOn = sp.getBoolean(key, false);
            return;
        }
    }

    @Override
    public boolean move(int keyCode) {
        if (!dpadOn) {
            return false;
        }
        long current = now();
        if (current - lastMove < 100) {
            if (multiplier < 20) {
                multiplier += 1;
            }
        } else {
            multiplier = 1;
        }
        lastMove = current;
        dp.setDx(0);
        dp.setDy(0);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                dp.setDy(1 * multiplier);
                serverConnection.writeObject(dp);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                dp.setDy(-1 * multiplier);
                serverConnection.writeObject(dp);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                dp.setDx(-1 * multiplier);
                serverConnection.writeObject(dp);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                dp.setDx(1 * multiplier);
                serverConnection.writeObject(dp);
                return true;
        }
        return false;
    }

    private long now() {
        return System.currentTimeMillis();
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }
}
