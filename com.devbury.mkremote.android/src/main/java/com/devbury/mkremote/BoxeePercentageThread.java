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

import com.devbury.logging.Logger;
import com.devbury.mkremote.api.BoxeePercentageResponsePacket;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.connections.ServerConnection;

import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;

public class BoxeePercentageThread extends Thread {
    public boolean stopped = false;
    private DataPacket dp = new DataPacket(DataPacket.BOXEE_PLAYED_PERCENTAGE);
    private SeekBar sb;
    private ServerConnection serverConnection;
    private Handler uiHandler;
    private int percent;

    public BoxeePercentageThread(ServerConnection sc, SeekBar s, Handler h) {
        serverConnection = sc;
        sb = s;
        uiHandler = h;
    }

    @Override
    public void run() {
        Logger.debug("Boxee thread starting");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (percent <= 0 || percent >= 100) {
                    sb.setVisibility(View.GONE);
                } else {
                    sb.setProgress(percent);
                    sb.setVisibility(View.VISIBLE);
                }
            }
        };
        while (!stopped) {
            try {
                Thread.sleep(3000);
                if (!stopped) {
                    if (serverConnection.isConnected()) {
                        percent = serverConnection.writeObject(dp, BoxeePercentageResponsePacket.class).getPercentage();
                        uiHandler.post(r);
                    } else {
                        Logger.debug("Not connected to a server.  Shutting down Boxee thread");
                        stopped = true;
                    }
                }
            } catch (Throwable t) {
                Logger.debug(t.toString());
            }
        }
        Logger.debug("Boxee thread stopping");
    }
}
