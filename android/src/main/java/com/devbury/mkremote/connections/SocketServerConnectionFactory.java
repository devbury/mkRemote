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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.devbury.mkremote.InvalidPasswordException;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.JsonService;
import com.devbury.mkremote.api.LoginResponsePacket;
import com.devbury.mkremote.api.ServiceInfo;

public class SocketServerConnectionFactory implements ServerConnectionFactory {

    private JsonService jsonService;
    private SocketServerConnection lastServerConnection;
    private ServiceInfo lastServiceInfo;
    private ConnectionCloseThread connectionCloseThread;
    private Object monitor = new Object();

    @Override
    public boolean close(ServerConnection serverConnection) {
        if (!(serverConnection instanceof SocketServerConnection)) {
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
    public void init() {
        connectionCloseThread = new ConnectionCloseThread();
    }

    @Override
    public ServerConnection getConnection(ServiceInfo serviceInfo, String password) {
        if (!serviceInfo.getAddress().contains(".")) {
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
            Socket s = new Socket();
            s.connect(new InetSocketAddress(serviceInfo.getAddress(), serviceInfo.getPort()), 2000);
            s.setSoTimeout(7000);
            SocketServerConnection sc = new SocketServerConnection();
            sc.setJsonService(jsonService);
            sc.setSocket(s);
            DataPacket lp = new DataPacket(DataPacket.LOGIN);
            lp.setS(password);
            LoginResponsePacket p = sc.writeObject(lp, LoginResponsePacket.class);
            if (p.getStatus() == LoginResponsePacket.BAD_PASSWORD) {
                sc.close();
                throw new InvalidPasswordException();
            }
            sc.setMacroMode(p.isMacroMode());
            ServiceInfo si = sc.writeObject(new DataPacket(DataPacket.MOUSE_MOVE_SERVICE_INFO), ServiceInfo.class);
            sc.setId(si.getId());
            serviceInfo.setId(si.getId());
            // This is needed because multicast is not working in Android.
            // we would have the attributes field published during autodiscovery
            // but since we don't we will
            // populate it here with the attributes in the MOUSE_MOVE
            // ServiceInfo
            // TODO remove this code when multicast works again
            if (serviceInfo.getAttributes() == null || serviceInfo.getAttributes().isEmpty()) {
                serviceInfo.setAttributes(si.getAttributes());
            }
            DatagramSocket ds = new DatagramSocket();
            byte[] buffer = new byte[2];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(si.getAddress()), si
                    .getPort());
            sc.setDatagramSocket(ds);
            sc.setDatagramPacket(dp);
            sc.setBuffer(buffer);
            lastServerConnection = sc;
            lastServiceInfo = serviceInfo;
            return sc;
        } catch (Throwable t) {
            if (t instanceof ServerConnectionException) {
                throw (ServerConnectionException) t;
            }
            throw new ServerConnectionException(t);
        }
    }

    public JsonService getJsonService() {
        return jsonService;
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
