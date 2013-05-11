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

import com.devbury.logging.Logger;
import com.devbury.mkremote.MkRemotePreferences;
import com.devbury.mkremote.R;
import com.devbury.mkremote.api.Bluetooth;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.JsonService;
import com.devbury.mkremote.api.ServiceInfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;

public class NativeBluetoothDiscoveryService implements DiscoveryService {

    private SharedPreferences sharedPreferences;
    private JsonService jsonService;

    public NativeBluetoothDiscoveryService() {
        // force load to make sure phone has Native bluetooth support
        BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public ArrayList<ServiceInfo> find(String serviceName) {
        ArrayList<ServiceInfo> services = new ArrayList<ServiceInfo>();
        if (sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_bluetooth_on), false)) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                for (BluetoothDevice d : adapter.getBondedDevices()) {
                    try {
                        BluetoothSocket bs = d.createRfcommSocketToServiceRecord(Bluetooth.MAINSERVICE_UUID);
                        bs.connect();
                        NativeBluetoothServerConnection con = new NativeBluetoothServerConnection(bs);
                        con.setJsonService(jsonService);
                        ServiceInfo si = con.writeObject(new DataPacket(DataPacket.SERVICE_PING), ServiceInfo.class);
                        con.close();
                        si.setAddress(d.getAddress());
                        si.setId(d.getName());
                        si.setName(si.getId());
                        services.add(si);
                    } catch (Throwable t) {
                        Logger.debug("Not running on " + d.getName() + " " + d.getAddress() + " Reason " + t);
                    }
                }
            }
        }
        return services;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }
}
