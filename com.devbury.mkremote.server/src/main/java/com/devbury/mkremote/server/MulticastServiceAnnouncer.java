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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.IP;
import com.devbury.mkremote.api.JsonService;
import com.devbury.mkremote.api.ServiceInfo;

public class MulticastServiceAnnouncer {
    private Logger logger = LoggerFactory
            .getLogger(MulticastServiceAnnouncer.class);
    private MulticastSocket multicastSocket;
    private boolean stop = false;
    private Collection<ServiceInfo> serviceInfos;
    private int port = IP.DEFAULT_PORT;
    private String group = IP.DEFAULT_GROUP;
    private JsonService jsonService;
    private AnnounceThread thread;

    protected ServiceInfo respondWithService(String request) {
        for (ServiceInfo si : serviceInfos) {
            if (request.equals(si.getName())) {
                return si;
            }
        }
        return null;
    }

    protected String newString(byte[] b, int offset, int length) {
        return new String(b, offset, length);
    }

    public void start() {
        thread = new AnnounceThread();
        thread.setName("MulticastServiceAnnouncer");
        thread.start();
    }

    public void shutdown() {
        stop = true;
        multicastSocket.close();
    }

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }

    public void setMulticastSocket(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
    }

    public Collection<ServiceInfo> getServiceInfos() {
        return serviceInfos;
    }

    public void setServiceInfos(Collection<ServiceInfo> serviceInfos) {
        this.serviceInfos = serviceInfos;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        serviceInfos = new LinkedList<ServiceInfo>();
        serviceInfos.add(serviceInfo);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    private class AnnounceThread extends Thread {
        @Override
        public void run() {
            InetAddress group_address = null;
            try {
                multicastSocket = new MulticastSocket(port);
                group_address = InetAddress.getByName(group);
                multicastSocket.joinGroup(group_address);
            } catch (Exception e) {
                logger.error("{}", e);
                logger.error("Thread stopping");
                return;
            }
            if (logger.isDebugEnabled()) {
                for (ServiceInfo si : serviceInfos) {
                    logger.debug("Starting service announce for " + si);
                }
            }
            boolean break_loop = false;
            while (!break_loop) {
                byte[] buffer = new byte[IP.DEFAULT_BUFFER_SIZE];
                DatagramPacket in = new DatagramPacket(buffer, buffer.length);
                try {
                    multicastSocket.receive(in);
                    String request = newString(in.getData(), in.getOffset(),
                            in.getLength());
                    logger.debug("Request is \"" + request + "\"");
                    ServiceInfo si = respondWithService(request);
                    if (si != null) {
                        logger.debug("Responding to request with " + si);
                        String json = jsonService.toJson(si);
                        DatagramPacket out = new DatagramPacket(
                                json.getBytes(), json.length(), group_address,
                                port);
                        multicastSocket.send(out);
                    } else {
                        logger.debug("Not responding to request");
                    }
                } catch (Exception e) {
                    if (stop) {
                        break_loop = true;
                    } else {
                        logger.debug("Skipping request.  Could not understand it"
                                + e);
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                for (ServiceInfo si : serviceInfos) {
                    logger.debug("Stopped service announce for " + si);
                }
            }
        }
    }
}
