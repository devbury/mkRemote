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
import android.view.MotionEvent;


public class SimpleMoveTrackballHandler implements MotionEventHandler, OnSharedPreferenceChangeListener {

    private ServerConnection serverConnection;
    private String mouseSensitivityKey = MkRemotePreferences.cs(R.string.c_mouse_sensitivity);
    private DataPacket mouseMovePacket = new DataPacket(DataPacket.MOUSE_MOVE);
    private int multiplier = 1;

    public SimpleMoveTrackballHandler(SharedPreferences sp) {
        sp.registerOnSharedPreferenceChangeListener(this);
        init(sp);
    }

    public void init(SharedPreferences sp) {
        onSharedPreferenceChanged(sp, mouseSensitivityKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(mouseSensitivityKey)) {
            multiplier = Integer.valueOf(sp.getString(key, "1"));
            return;
        }
    }

    @Override
    public boolean processEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int hsize = event.getHistorySize();
                for (int i = 0; i < hsize; i++) {
                    move((int) (event.getHistoricalX(i) * event.getXPrecision()), (int) (event.getHistoricalY(i) * event
                            .getYPrecision()));
                }
                move((int) (event.getX() * event.getXPrecision()), (int) (event.getY() * event.getYPrecision()));
                break;
        }
        return true;
    }

    private void move(int x, int y) {
        mouseMovePacket.setDx(x * (multiplier + 10));
        mouseMovePacket.setDy(y * (multiplier + 10));
        serverConnection.writeObject(mouseMovePacket);
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }
}
