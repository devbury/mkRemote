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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;

import com.devbury.mkremote.api.IP;
import com.devbury.mkremote.api.JsonService;
import com.devbury.mkremote.api.ServiceInfo;

public class MulticastDiscoveryService implements DiscoveryService {

    private static final int SECONDS_TO_MILLISECONDS = 1000;
    private int port = IP.DEFAULT_PORT;
    private String group = IP.DEFAULT_GROUP;
    private int secondsToWait = 3;
    private JsonService jsonService;

    @Override
    public ArrayList<ServiceInfo> find(String serviceName) {
        HashSet<ServiceInfo> services = new HashSet<ServiceInfo>();
        MulticastSocket socket = null;
        try {
            socket = newMulticastSocket(port);
            InetAddress group_address = InetAddress.getByName(group);
            socket.joinGroup(group_address);
            DatagramPacket out = new DatagramPacket(serviceName.getBytes(), serviceName.length(), group_address, port);
            // send out request
            socket.send(out);

            // read responses
            boolean done = false;
            socket.setSoTimeout(secondsToWait * SECONDS_TO_MILLISECONDS);
            while (!done) {
                byte[] buffer = new byte[IP.DEFAULT_BUFFER_SIZE];
                DatagramPacket in = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(in);
                    ServiceInfo si = processResponse(new String(in.getData(), in.getOffset(), in.getLength()),
                            serviceName);
                    if (si != null) {
                        services.add(si);
                    }
                } catch (SocketTimeoutException e) {
                    done = true;
                }
            }
        } catch (Throwable t) {
        }
        if (socket != null) {
            socket.close();
        }
        return new ArrayList<ServiceInfo>(services);
    }

    protected ServiceInfo processResponse(String data, String serviceName) {
        try {
            ServiceInfo si = jsonService.fromJson(data, ServiceInfo.class);
            if (si.getName().equals(serviceName)) {
                return si;
            }
        } catch (Throwable t) {
        }
        return null;
    }

    protected MulticastSocket newMulticastSocket(int port) throws IOException {
        return new MulticastSocket(port);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getSecondsToWait() {
        return secondsToWait;
    }

    public void setSecondsToWait(int secondsToWait) {
        this.secondsToWait = secondsToWait;
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }
}
