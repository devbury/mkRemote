/*
 * Copyright (c) 2009-2013 devBury LLC
 * This file is part of mkRemote.
 *
 *     mkRemote is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License Version 3
 *     as published by the Free Software Foundation.
 *
 *     mkRemote is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with mkRemote.  If not, see <http://www.gnu.org/licenses/gpl.txt/>.
 */

package com.devbury.mkremote.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.ServiceInfo;

public class MouseMoveServer {

    private Logger logger = LoggerFactory.getLogger(MouseMoveServer.class);
    private int port = 0;
    private ServiceInfo serviceInfo;
    private DatagramSocket socket;
    private MouseMoveThread mouseMoveThread;
    private MouseAndKeyService mouseAndKeyService;
    private NetworkAddressService networkAddressService;

    public void start() {
        try {
            InetAddress ia = networkAddressService.getNetworkAddress();
            socket = newDatagramSocket(ia, port);
            serviceInfo.setAddress(ia.getHostAddress());
            serviceInfo.setPort(socket.getLocalPort());
            String id = ia.getHostName();
            if (id == null || id.equals("")) {
                id = ia.getCanonicalHostName();
            }
            if (id == null || id.equals("") || id.equals(ia.getHostAddress())) {
                id = System.getProperty("user.name") + " "
                        + System.getProperty("os.name") + "-"
                        + System.getProperty("os.version");
            }
            serviceInfo.setId(id);
            mouseMoveThread = new MouseMoveThread();
            mouseMoveThread.setName("MouseMoveThread");
            mouseMoveThread.start();
        } catch (Throwable t) {
            logger.warn("Could not start mouseMove thread", t);
        }
    }

    public void shutdown() {
        if (mouseMoveThread != null) {
            mouseMoveThread.setStopped(true);
            mouseMoveThread.interrupt();
        }
        if (socket != null) {
            socket.close();
        }
    }

    protected DatagramSocket newDatagramSocket(InetAddress a, int port) {
        try {
            return new DatagramSocket(new InetSocketAddress(a, port));
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public MouseAndKeyService getMouseAndKeyService() {
        return mouseAndKeyService;
    }

    public void setMouseAndKeyService(MouseAndKeyService mouseAndKeyService) {
        this.mouseAndKeyService = mouseAndKeyService;
    }

    public NetworkAddressService getNetworkAddressService() {
        return networkAddressService;
    }

    public void setNetworkAddressService(
            NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private class MouseMoveThread extends Thread {

        private boolean stopped = false;

        @Override
        public void run() {
            logger.debug("Accepting connections on {}:{}",
                    serviceInfo.getAddress(), serviceInfo.getPort());
            byte[] buffer = new byte[2];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            while (!stopped) {
                try {
                    socket.receive(dp);
                    mouseAndKeyService.mouseDelta(buffer[0], buffer[1]);
                } catch (IOException e) {
                    if (!stopped) {
                        logger.error("Error receiving packet. {}", e.toString());
                    }
                }
            }
            logger.debug("MouseMoveThread stopped");
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }
    }
}
