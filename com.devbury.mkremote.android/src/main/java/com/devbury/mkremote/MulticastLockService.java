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

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

public class MulticastLockService implements IMulticastLockService {
    private MulticastLock lock;
    private Context context;

    public MulticastLockService(Context context) {
        this.context = context;

    }

    @Override
    public void acquireLock() {
        if (lock == null || !lock.isHeld()) {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            lock = wm.createMulticastLock("mkRemote");
            lock.acquire();
        }
    }

    @Override
    public void releaseLock() {
        if (lock != null && lock.isHeld()) {
            lock.release();
            lock = null;
        }
    }
}
