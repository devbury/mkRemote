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
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.Bluetooth;
import com.intel.bluetooth.BlueCoveImpl;

public class BluetoothServer {

    private Logger logger = LoggerFactory.getLogger(BluetoothServer.class);
    private BluetoothServerThread thread = new BluetoothServerThread();
    private LocalDevice localDevice;
    private Service service;
    private boolean connected = false;
    private int port;
    private String address;
    private boolean bluetoothSupported = false;

    private class BluetoothServerThread extends Thread {

        private boolean stopped = false;

        @Override
        public void run() {
            logger.debug("Thread started");
            UUID uuid = new UUID(Bluetooth.MAINSERVICE, false);

            String connectionString = "btspp://localhost:" + uuid
                    + ";name=mkRemote";
            StreamConnectionNotifier serviceNotifier = null;
            ServiceRecord record = null;
            try {
                serviceNotifier = (StreamConnectionNotifier) Connector.open(
                        connectionString, Connector.READ_WRITE);
                record = localDevice.getRecord(serviceNotifier);
                DataElement de = new DataElement(DataElement.DATSEQ);
                de.addElement(new DataElement(DataElement.UUID, new UUID(
                        "1002", true)));
                record.setAttributeValue(0x005, de);
                localDevice.updateRecord(record);
            } catch (Throwable t) {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                logger.debug("Bluetooth not supported due to {}", sw);
                bluetoothSupported = false;
            }

            if (bluetoothSupported) {
                {
                    String url = record.getConnectionURL(
                            ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    String hold = url.substring(0, url.indexOf(';'));
                    hold = hold.substring(hold.lastIndexOf(':') + 1,
                            hold.length());
                    port = Integer.parseInt(hold);
                    logger.debug("Running on port {}", port);
                }
                while (!stopped) {
                    try {
                        StreamConnection con = serviceNotifier.acceptAndOpen();
                        logger.debug("Bluetooth Connection");
                        connected = true;
                        service.performService(con.openInputStream(),
                                con.openOutputStream());
                        logger.debug("Service Finished");
                        con.close();
                        connected = false;
                    } catch (IOException e) {
                        if (!(e instanceof InterruptedIOException)) {
                            logger.debug("Error While processing request {}",
                                    e.toString());
                        }
                    }
                }
            }
            try {
                serviceNotifier.close();
            } catch (IOException e) {
            }
            BlueCoveImpl.shutdown();
            logger.debug("Thread stopped");
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }
    }

    public void setStack(String stack) {
        System.setProperty("bluecove.stack", stack);
        logger.debug("Setting Bluetooth stack to {}", stack);
    }

    public void start() {
        try {
            localDevice = LocalDevice.getLocalDevice();
            address = Bluetooth
                    .formatAddress(localDevice.getBluetoothAddress());
            logger.debug("Bluetooth stack type is {}",
                    LocalDevice.getProperty("bluecove.stack"));
            logger.debug("Bluetooth name {}", localDevice.getFriendlyName());
            logger.debug("Bluetooth address {}", address);
            logger.debug("Bluetooth API Version {}",
                    LocalDevice.getProperty("bluetooth.api.version"));
            try {
                if (!localDevice.setDiscoverable(DiscoveryAgent.GIAC)) {
                    logger.debug("Could not set discoverable to GIAC");
                }
            } catch (Throwable sd) {
                logger.debug("Could not set discoverable to GIAC", sd);
            }

            thread = new BluetoothServerThread();
            thread.setName("BluetoothServer");
            thread.start();
            bluetoothSupported = true;
        } catch (Throwable t) {
            logger.debug("Bluetooth not supported due to {}", t.toString());
        }
    }

    public void shutdown() {
        if (bluetoothSupported) {
            logger.debug("Shutting down");
            thread.setStopped(true);
            thread.interrupt();
        }
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isBluetoothSupported() {
        return bluetoothSupported;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}
