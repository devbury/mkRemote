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

import android.view.MotionEvent;


public class ScrollTrackballHandler implements MotionEventHandler {

    private ServerConnection serverConnection;
    private DataPacket scrollUpPacket = new DataPacket(DataPacket.UP_ARROW);
    private DataPacket scrollDownPacket = new DataPacket(DataPacket.DOWN_ARROW);
    private DataPacket scrollRightPacket = new DataPacket(DataPacket.RIGHT_ARROW);
    private DataPacket scrollLeftPacket = new DataPacket(DataPacket.LEFT_ARROW);
    private boolean upDown = false;
    private boolean leftRight = false;

    @Override
    public boolean processEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int hsize = event.getHistorySize();
            for (int i = 0; i < hsize; i++) {
                process(event.getHistoricalX(i), event.getHistoricalY(i));
            }
            process(event.getX(), event.getY());
        }
        return true;
    }

    protected void process(float x, float y) {
        if (upDown) {
            if (y < 0) {
                serverConnection.writeObject(scrollUpPacket);
            } else if (y > 0) {
                serverConnection.writeObject(scrollDownPacket);
            }
        }
        if (leftRight) {
            if (x < 0) {
                serverConnection.writeObject(scrollLeftPacket);
            } else if (x > 0) {
                serverConnection.writeObject(scrollRightPacket);
            }
        }
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public boolean isUpDown() {
        return upDown;
    }

    public void setUpDown(boolean upDown) {
        this.upDown = upDown;
    }

    public boolean isLeftRight() {
        return leftRight;
    }

    public void setLeftRight(boolean leftRight) {
        this.leftRight = leftRight;
    }
}
