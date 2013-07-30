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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import com.devbury.logging.Logger;
import com.devbury.mkremote.api.BoxeePercentageResponsePacket;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.JsonService;
import com.google.gson.JsonParseException;

public class SocketServerConnection implements ServerConnection {

    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private byte[] buffer;
    private JsonService jsonService;
    private boolean macroMode = false;
    private String id;
    private BoxeePercentageResponsePacket lastPacket = new BoxeePercentageResponsePacket();

    protected void close() {
        try {
            in.close();
        } catch (Throwable t) {
            // we tried
        }
        in = null;
        try {
            out.close();
        } catch (Throwable t) {
            // we tried
        }
        out = null;
        try {
            socket.close();
        } catch (Throwable t) {
            // we tried
        }
        socket = null;
        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
        datagramPacket = null;
    }

    @Override
    public void writeObject(Object object) {
        if (object instanceof DataPacket) {
            DataPacket dp = (DataPacket) object;
            if (dp.getType() == DataPacket.MOUSE_MOVE) {
                buffer[0] = (byte) dp.getDx();
                buffer[1] = (byte) dp.getDy();
                try {
                    datagramSocket.send(datagramPacket);
                } catch (IOException e) {
                    throw new ServerConnectionException("Not connected to a server");
                }
                return;
            }
        }
        errorIfNotConnected();
        try {
            Writer writer = new OutputStreamWriter(out);
            jsonService.toJson(object, writer);
            writer.flush();
        } catch (Throwable t) {
            close();
            throw new ServerConnectionException(t);
        }
    }

    public <T> T readObject(Class<T> clazz) {
        errorIfNotConnected();
        Throwable t_hold;
        try {
            Reader reader = new InputStreamReader(in);
            T hold = jsonService.fromJson(reader, clazz);
            if (clazz.equals(BoxeePercentageResponsePacket.class)) {
                lastPacket = (BoxeePercentageResponsePacket) hold;
            }
            return hold;
        } catch (JsonParseException e) {
            if (clazz.equals(BoxeePercentageResponsePacket.class)) {
                Logger.debug("Jam up.  Returning lastPercentPacket");
                return (T) lastPacket;
            }
            t_hold = e;
        } catch (Throwable t) {
            t_hold = t;
        }
        close();
        throw new ServerConnectionException(t_hold);
    }

    @Override
    public <T> T writeObject(Object object, Class<T> clazz) {
        writeObject(object);
        return readObject(clazz);
    }

    @Override
    public boolean isConnected() {
        return (socket != null && in != null && out != null);
    }

    protected void errorIfNotConnected() {
        if (!isConnected()) {
            throw new ServerConnectionException("Not connected to a server");
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }

    public void setDatagramPacket(DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean isMacroMode() {
        return macroMode;
    }

    public void setMacroMode(boolean macroMode) {
        this.macroMode = macroMode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
