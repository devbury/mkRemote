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

package com.devbury.mkremote.connections;

import java.util.ArrayList;
import java.util.Properties;

import com.devbury.logging.Logger;
import com.devbury.mkremote.MkRemoteApplication;
import com.devbury.mkremote.api.ServiceInfo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class HistoryDiscoveryService implements DiscoveryService {

    public static final String HISTORY_ADDRESS = "_History_Address";
    public static final String HISTORY_PORT = "_History_Port";
    public static final String HISTORY_ID = "_History_Id";
    public static final String HISTORY_OS = "_History_Os";
    private SharedPreferences sharedPreferences;
    private MkRemoteApplication app;

    @Override
    public ArrayList<ServiceInfo> find(String serviceName) {
        ArrayList<ServiceInfo> servers = new ArrayList<ServiceInfo>();
        for (int i = 1; i <= 5; i++) {
            ServiceInfo info = new ServiceInfo();
            info.setAddress(sharedPreferences.getString(i + HISTORY_ADDRESS, null));
            if (info.getAddress() != null) {
                info.setId(sharedPreferences.getString(i + HISTORY_ID, null));
                info.setName(info.getId());
                Properties p = new Properties();
                p.setProperty("os.name", sharedPreferences.getString(i + HISTORY_OS, null));
                info.setAttributes(p);
                info.setPort(sharedPreferences.getInt(i + HISTORY_PORT, 5555));
                info.setVersionCode(app.minServerVersion);
                servers.add(info);
            }
        }
        return servers;
    }

    public void addToHistory(ServiceInfo si) {
        boolean found = false;
        int max = 0;
        for (int i = 1; i <= 5; i++) {
            String address = sharedPreferences.getString(i + HistoryDiscoveryService.HISTORY_ADDRESS, "");
            String id = sharedPreferences.getString(i + HistoryDiscoveryService.HISTORY_ID, "");
            int port = sharedPreferences.getInt(i + HistoryDiscoveryService.HISTORY_PORT, 5555);
            if (!address.equals("")) {
                max = i;
            }
            if (address.equals(si.getAddress()) && id.equals(si.getId()) && port == si.getPort()) {
                found = true;
            }
        }
        if (!found) {
            Editor e = sharedPreferences.edit();
            for (int i = max; i >= 1; i--) {
                if (i < 5) {
                    Logger.debug("moving " + i + " to " + (i + 1));
                    e.putString((i + 1) + HistoryDiscoveryService.HISTORY_ADDRESS, sharedPreferences.getString(i
                            + HistoryDiscoveryService.HISTORY_ADDRESS, ""));
                    e.putString((i + 1) + HistoryDiscoveryService.HISTORY_ID, sharedPreferences.getString(i
                            + HistoryDiscoveryService.HISTORY_ID, ""));
                    e.putString((i + 1) + HistoryDiscoveryService.HISTORY_OS, sharedPreferences.getString(i
                            + HistoryDiscoveryService.HISTORY_OS, ""));
                    e.putInt((i + 1) + HistoryDiscoveryService.HISTORY_PORT, sharedPreferences.getInt(i
                            + HistoryDiscoveryService.HISTORY_PORT, 5555));
                }
            }
            Logger.debug("Adding to history");
            e.putString(1 + HistoryDiscoveryService.HISTORY_ADDRESS, si.getAddress());
            e.putString(1 + HistoryDiscoveryService.HISTORY_ID, si.getId());
            e.putString(1 + HistoryDiscoveryService.HISTORY_OS, si.getAttributes().getProperty("os.name"));
            e.putInt(1 + HistoryDiscoveryService.HISTORY_PORT, si.getPort());
            e.commit();
        }
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public MkRemoteApplication getApp() {
        return app;
    }

    public void setApp(MkRemoteApplication app) {
        this.app = app;
    }

}
