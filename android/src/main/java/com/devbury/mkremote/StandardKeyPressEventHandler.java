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

package com.devbury.mkremote;

import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.VirtualKeyCode;
import com.devbury.mkremote.connections.ServerConnection;

import android.view.KeyEvent;


public class StandardKeyPressEventHandler implements KeyEventHandler {

    private ServerConnection serverConnection;
    private DataPacket keyPressPacket = new DataPacket(DataPacket.KEY_PRESS);
    private DataPacket virtualKeyPressPacket = new DataPacket(DataPacket.VIRTUAL_KEY_PRESS);
    private boolean altModifier = false;
    private boolean shiftModifier = false;

    @Override
    public boolean processEvent(int keyCode, KeyEvent event) {
        if (event instanceof VirtualKeyEvent) {
            VirtualKeyEvent v = (VirtualKeyEvent) event;
            virtualKeyPressPacket.setVirtualKeyCode(keyCode);
            int modifiers = 0;
            modifiers |= v.isVirtualAltPressed() ? VirtualKeyCode.ALT_MASK : 0;
            modifiers |= v.isVirtualCtrlPressed() ? VirtualKeyCode.CTRL_MASK : 0;
            modifiers |= v.isVirtualWinPressed() ? VirtualKeyCode.WIN_MASK : 0;
            modifiers |= v.isVirtualCommandPressed() ? VirtualKeyCode.MAC_COMMAND_MASK : 0;
            virtualKeyPressPacket.setVirtualModifiers(modifiers);
            serverConnection.writeObject(virtualKeyPressPacket);
            return true;
        }
        int meta_state = event.getMetaState();
        if (altModifier) {
            meta_state |= KeyEvent.META_ALT_ON;
        }
        if (shiftModifier) {
            meta_state |= KeyEvent.META_SHIFT_ON;
        }
        int code = event.getUnicodeChar(meta_state);
        if (code != 0) {
            altModifier = false;
            shiftModifier = false;
            keyPressPacket.setUnicode(code);
            serverConnection.writeObject(keyPressPacket);
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                altModifier = !altModifier;
                return true;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                shiftModifier = !shiftModifier;
                return true;
            case KeyEvent.KEYCODE_DEL:
                if (((meta_state & KeyEvent.META_ALT_ON) == KeyEvent.META_ALT_ON)
                        || ((meta_state & KeyEvent.META_SHIFT_ON) == KeyEvent.META_SHIFT_ON)) {
                    altModifier = false;
                    shiftModifier = false;
                    keyPressPacket.setType(DataPacket.DEL_PRESS);
                    serverConnection.writeObject(keyPressPacket);
                    keyPressPacket.setType(DataPacket.KEY_PRESS);
                } else {
                    keyPressPacket.setUnicode('\b');
                    serverConnection.writeObject(keyPressPacket);
                }
                return true;
        }
        return false;
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }
}
