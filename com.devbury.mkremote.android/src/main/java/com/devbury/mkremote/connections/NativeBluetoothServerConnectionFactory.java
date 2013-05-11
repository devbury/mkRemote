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

import com.devbury.mkremote.InvalidPasswordException;
import com.devbury.mkremote.api.Bluetooth;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.JsonService;
import com.devbury.mkremote.api.LoginResponsePacket;
import com.devbury.mkremote.api.ServiceInfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class NativeBluetoothServerConnectionFactory implements ServerConnectionFactory {

    private JsonService jsonService;
    private BluetoothAdapter adapter;
    private ConnectionCloseThread connectionCloseThread;
    private NativeBluetoothServerConnection lastServerConnection;
    private ServiceInfo lastServiceInfo;
    private Object monitor = new Object();

    public NativeBluetoothServerConnectionFactory() {
        // Force load to make sure phone has bluetooth libraries
        BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public boolean close(ServerConnection serverConnection) {
        if (!(serverConnection instanceof NativeBluetoothServerConnection)) {
            return false;
        }
        if (serverConnection != null) {
            connectionCloseThread = new ConnectionCloseThread();
            connectionCloseThread.start();
        }
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public ServerConnection getConnection(ServiceInfo serviceInfo, String password) {
        if (!serviceInfo.getAddress().contains(":")) {
            return null;
        }
        if (serviceInfo.equals(lastServiceInfo)) {
            synchronized (monitor) {
                connectionCloseThread.setConnectionReused(true);
                if (lastServiceInfo != null && lastServerConnection != null) {
                    return lastServerConnection;
                }
            }
        }
        try {
            BluetoothDevice remote = adapter.getRemoteDevice(serviceInfo.getAddress());
            BluetoothSocket socket = null;
            BluetoothSocket mouse_socket = null;

            socket = remote.createRfcommSocketToServiceRecord(Bluetooth.MAINSERVICE_UUID);
            socket.connect();
            NativeBluetoothServerConnection con = new NativeBluetoothServerConnection(socket);
            con.setJsonService(jsonService);
            DataPacket lp = new DataPacket(DataPacket.LOGIN);
            lp.setS(password);
            LoginResponsePacket p = con.writeObject(lp, LoginResponsePacket.class);
            if (p.getStatus() == LoginResponsePacket.BAD_PASSWORD) {
                con.close();
                throw new InvalidPasswordException();
            }
            con.setMacroMode(p.isMacroMode());
            ServiceInfo si = con.writeObject(new DataPacket(DataPacket.MOUSE_MOVE_SERVICE_INFO), ServiceInfo.class);
            con.setId(serviceInfo.getId());
            serviceInfo.setAttributes(si.getAttributes());
            mouse_socket = remote.createRfcommSocketToServiceRecord(Bluetooth.MOUSEMOVESERVICE_UUID);
            mouse_socket.connect();
            con.setMouseSocket(mouse_socket);
            lastServerConnection = con;
            lastServiceInfo = serviceInfo;
            return con;
        } catch (Throwable t) {
            if (t instanceof ServerConnectionException) {
                throw (ServerConnectionException) t;
            }
            throw new ServerConnectionException(t);
        }

    }

    @Override
    public void init() {
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    private class ConnectionCloseThread extends Thread {
        private boolean connectionReused = false;

        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            synchronized (monitor) {
                if (!connectionReused) {
                    if (lastServerConnection != null) {
                        lastServerConnection.close();
                    }
                    lastServerConnection = null;
                    lastServiceInfo = null;
                }
            }
        }

        public void setConnectionReused(boolean connectionReused) {
            this.connectionReused = connectionReused;
        }
    }
}
