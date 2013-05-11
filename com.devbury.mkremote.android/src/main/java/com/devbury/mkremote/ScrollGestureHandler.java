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

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class ScrollGestureHandler extends SimpleOnGestureListener {

    private ServerConnection serverConnection;
    private boolean scrollActive = false;
    private DataPacket up = new DataPacket(DataPacket.SCROLL_UP);
    private DataPacket down = new DataPacket(DataPacket.SCROLL_DOWN);

    @Override
    public void onLongPress(MotionEvent e) {
        scrollActive = true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (scrollActive) {
            if (distanceY < 0) {
                serverConnection.writeObject(up);
            } else {
                serverConnection.writeObject(down);
            }
            if (e2.getAction() == MotionEvent.ACTION_UP) {
                setScrollActive(false);
            }
            return true;
        }
        return false;
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public boolean isScrollActive() {
        return scrollActive;
    }

    public void setScrollActive(boolean scrollActive) {
        this.scrollActive = scrollActive;
    }
}
