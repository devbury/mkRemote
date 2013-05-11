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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.JsonService;

import android.bluetooth.BluetoothSocket;

public class NativeBluetoothServerConnection implements ServerConnection {

    private BluetoothSocket mainSocket;
    private BluetoothSocket mouseSocket;
    private OutputStream mouseOutputStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String id;
    private boolean macroMode = false;
    private JsonService jsonService;
    private byte[] buffer = new byte[2];

    public NativeBluetoothServerConnection(BluetoothSocket main) throws IOException {
        mainSocket = main;
        inputStream = mainSocket.getInputStream();
        outputStream = mainSocket.getOutputStream();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean isConnected() {
        return (mainSocket != null && inputStream != null && outputStream != null);
    }

    @Override
    public boolean isMacroMode() {
        return macroMode;
    }

    public void setMacroMode(boolean macroMode) {
        this.macroMode = macroMode;
    }

    @Override
    public <T> T writeObject(Object object, Class<T> clazz) {
        writeObject(object);
        return readObject(clazz);
    }

    public <T> T readObject(Class<T> clazz) {
        errorIfNotConnected();
        try {
            Reader reader = new InputStreamReader(inputStream);
            return jsonService.fromJson(reader, clazz);
        } catch (Throwable t) {
            close();
            throw new ServerConnectionException(t);
        }
    }

    @Override
    public void writeObject(Object object) {
        if (object instanceof DataPacket) {
            DataPacket dp = (DataPacket) object;
            if (dp.getType() == DataPacket.MOUSE_MOVE) {
                buffer[0] = (byte) dp.getDx();
                buffer[1] = (byte) dp.getDy();
                try {
                    mouseOutputStream.write(buffer);
                } catch (Throwable t) {
                    close();
                    throw new ServerConnectionException(t);
                }
                return;
            }
        }
        errorIfNotConnected();
        try {
            Writer writer = new OutputStreamWriter(outputStream);
            jsonService.toJson(object, writer);
            writer.flush();
        } catch (Throwable t) {
            close();
            throw new ServerConnectionException(t);
        }
    }

    protected void errorIfNotConnected() {
        if (!isConnected()) {
            throw new ServerConnectionException("Not connected to a server");
        }
    }

    protected void close() {
        if (mainSocket != null) {
            try {
                mainSocket.close();
            } catch (IOException e) {
            }
        }
        if (mouseSocket != null) {
            try {
                mouseSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void setMouseSocket(BluetoothSocket mouseSocket) throws IOException {
        this.mouseSocket = mouseSocket;
        mouseOutputStream = this.mouseSocket.getOutputStream();
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }
}
