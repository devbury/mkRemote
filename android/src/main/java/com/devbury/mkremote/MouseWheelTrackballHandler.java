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

public class MouseWheelTrackballHandler implements MotionEventHandler {

    private ServerConnection serverConnection;
    private DataPacket scrollUpPacket = new DataPacket(DataPacket.SCROLL_UP);
    private DataPacket scrollDownPacket = new DataPacket(DataPacket.SCROLL_DOWN);

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
        if (y < 0) {
            serverConnection.writeObject(scrollUpPacket);
        } else if (y > 0) {
            serverConnection.writeObject(scrollDownPacket);
        }
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }
}
