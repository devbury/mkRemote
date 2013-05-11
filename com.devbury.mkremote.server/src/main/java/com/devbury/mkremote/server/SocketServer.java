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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.ServiceInfo;

public class SocketServer {
    public static final int DEFAULT_PORT = 5555;
    private Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private int port = DEFAULT_PORT;
    private NetworkAddressService networkAddressService;
    private SocketServerThread thread;
    private ServerSocket serverSocket;
    private Socket socket;
    private ServiceInfo serviceInfo;
    private Service service;
    private int backlog = 0;

    public void start() {
        try {
            InetAddress ip = networkAddressService.getNetworkAddress();
            String id = ip.getHostName();
            if (id == null || id.equals("")) {
                id = ip.getCanonicalHostName();
            }

            if (id == null || id.equals("") || id.equals(ip.getHostAddress())) {
                id = System.getProperty("user.name") + " "
                        + System.getProperty("os.name") + "-"
                        + System.getProperty("os.version");
            }

            serverSocket = new ServerSocket(port, backlog, ip);
            serviceInfo.setPort(serverSocket.getLocalPort());
            serviceInfo.setAddress(serverSocket.getInetAddress()
                    .getHostAddress());
            serviceInfo.setId(id);
            thread = new SocketServerThread();
            thread.setName("SocketServer");
            thread.start();
        } catch (Throwable e) {
            logger.warn("Could not start up socket server", e);
        }
    }

    public void shutdown() {
        logger.debug("Shutting down");
        if (thread != null) {
            thread.setStop(true);
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.debug("Could not close socket server");
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Could not close socket");
            }
        }
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public NetworkAddressService getNetworkAddressService() {
        return networkAddressService;
    }

    public void setNetworkAddressService(
            NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private class SocketServerThread extends Thread {

        private boolean stop = false;

        public void run() {

            logger.debug("Accepting connections on " + serviceInfo.getAddress()
                    + ':' + serviceInfo.getPort());
            while (!stop) {
                try {
                    socket = serverSocket.accept();
                    logger.debug("Accepted connection");
                    service.performService(socket.getInputStream(),
                            socket.getOutputStream());
                    logger.debug("Service finished");
                    socket.close();
                    socket = null;
                } catch (Throwable t) {
                    if (!stop) {
                        logger.error("error.  Listening for a new connection",
                                t);
                    }
                }
            }
            logger.debug("Stopped");
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }
    }
}
