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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkAddressServiceImpl implements NetworkAddressService {

    private Logger logger = LoggerFactory
            .getLogger(NetworkAddressServiceImpl.class);
    private OptionsStorageService optionsStorageService;
    private String preferredInterface = null;

    public Collection<String> findPossibleNetworkAddresses() {
        HashSet<String> addresses = new HashSet<String>();
        StringBuffer sb = new StringBuffer("local:");
        try {
            sb.append(InetAddress.getLocalHost().getHostAddress());
        } catch (Throwable t) {
            sb.append("127.0.0.1");
        }
        addresses.add(sb.toString()); // just to make sure at least one exists
        try {
            Enumeration<NetworkInterface> ni_enum = NetworkInterface
                    .getNetworkInterfaces();
            while (ni_enum.hasMoreElements()) {
                NetworkInterface ni = ni_enum.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    Enumeration<InetAddress> inet_enum = ni.getInetAddresses();
                    while (inet_enum.hasMoreElements()) {
                        InetAddress inet = inet_enum.nextElement();
                        if (inet instanceof Inet4Address) {
                            String address = ni.getName() + ":"
                                    + inet.getHostAddress();
                            logger.debug("Found {}", address);
                            addresses.add(address);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("Returning partial or empty network interface list", e);
        }
        return addresses;
    }

    private InetAddress buildInetAddress(String s) {
        int index = s.indexOf(":");
        String name = s.substring(0, index);
        String address = s.substring(index + 1);
        NetworkInterface ni = null;
        if (name.equals("local")) {
            try {
                logger.debug("Interface name is set to local.  Trying to get localhost");
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new RuntimeException(
                        "Could not get the localhost adapter.  Is the network even up?");
            }
        }
        try {
            ni = NetworkInterface.getByName(name);
        } catch (SocketException e) {
            throw new RuntimeException("Could not build InetAddress from " + s,
                    e);
        }
        Enumeration<InetAddress> inet_enum = ni.getInetAddresses();
        while (inet_enum.hasMoreElements()) {
            InetAddress inet = inet_enum.nextElement();
            if (address.equals(inet.getHostAddress())) {
                return inet;
            }
        }
        throw new RuntimeException("No InetAddress found for " + s);
    }

    public String getNetworkName() {
        Collection<String> addresses = findPossibleNetworkAddresses();
        String pi = preferredInterface;
        if (pi == null) {
            pi = optionsStorageService.getNetworkInterface();
            if (pi == null) {
                if (isMac()) {
                    pi = "en1";
                } else {
                    pi = "eth";
                }
            }
        }
        for (String s : addresses) {
            if (s.startsWith(pi)) {
                return s;
            }
        }

        // take the first one
        return addresses.iterator().next();
    }

    public InetAddress getNetworkAddress() {
        return buildInetAddress(getNetworkName());
    }

    protected boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public OptionsStorageService getOptionsStorageService() {
        return optionsStorageService;
    }

    public void setOptionsStorageService(
            OptionsStorageService optionsStorageService) {
        this.optionsStorageService = optionsStorageService;
    }

    public String getPreferredInterface() {
        return preferredInterface;
    }

    public void setPreferredInterface(String preferredInterface) {
        this.preferredInterface = preferredInterface;
    }
}
