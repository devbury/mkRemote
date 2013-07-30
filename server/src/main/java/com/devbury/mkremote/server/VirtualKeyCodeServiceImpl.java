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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.VirtualKeyCode;

public class VirtualKeyCodeServiceImpl implements VirtualKeyCodeService {

    private Logger logger = LoggerFactory
            .getLogger(VirtualKeyCodeServiceImpl.class);
    private UnicodeToKeyCodeSequenceConverter converter;

    @Override
    public int[] getSequence(DataPacket vp) {
        ArrayList<Integer> ms = new ArrayList<Integer>();
        if ((vp.getVirtualModifiers() & VirtualKeyCode.ALT_MASK) != 0) {
            ms.add(KeyEvent.VK_ALT);
        }
        if ((vp.getVirtualModifiers() & VirtualKeyCode.CTRL_MASK) != 0) {
            ms.add(KeyEvent.VK_CONTROL);
        }
        if ((vp.getVirtualModifiers() & VirtualKeyCode.WIN_MASK) != 0) {
            ms.add(KeyEvent.VK_WINDOWS);
        }
        if ((vp.getVirtualModifiers() & VirtualKeyCode.MAC_COMMAND_MASK) != 0) {
            ms.add(KeyEvent.VK_META);
        }
        if (vp.getUnicode() > 0) {
            // it is a unicode char.
            // convert to a KeyEvent array
            for (int key : converter.convert(vp.getUnicode())) {
                ms.add(key);
            }

        } else {
            switch (vp.getVirtualKeyCode()) {
                case VirtualKeyCode.ESC:
                    ms.add(KeyEvent.VK_ESCAPE);
                    break;
                case VirtualKeyCode.F1:
                    ms.add(KeyEvent.VK_F1);
                    break;
                case VirtualKeyCode.F2:
                    ms.add(KeyEvent.VK_F2);
                    break;
                case VirtualKeyCode.F3:
                    ms.add(KeyEvent.VK_F3);
                    break;
                case VirtualKeyCode.F4:
                    ms.add(KeyEvent.VK_F4);
                    break;
                case VirtualKeyCode.F5:
                    ms.add(KeyEvent.VK_F5);
                    break;
                case VirtualKeyCode.F6:
                    ms.add(KeyEvent.VK_F6);
                    break;
                case VirtualKeyCode.F7:
                    ms.add(KeyEvent.VK_F7);
                    break;
                case VirtualKeyCode.F8:
                    ms.add(KeyEvent.VK_F8);
                    break;
                case VirtualKeyCode.F9:
                    ms.add(KeyEvent.VK_F9);
                    break;
                case VirtualKeyCode.F10:
                    ms.add(KeyEvent.VK_F10);
                    break;
                case VirtualKeyCode.F11:
                    ms.add(KeyEvent.VK_F11);
                    break;
                case VirtualKeyCode.F12:
                    ms.add(KeyEvent.VK_F12);
                    break;
                case VirtualKeyCode.BSPACE:
                    ms.add(KeyEvent.VK_BACK_SPACE);
                    break;
                case VirtualKeyCode.TAB:
                    ms.add(KeyEvent.VK_TAB);
                    break;
                case VirtualKeyCode.ENTER:
                    ms.add(KeyEvent.VK_ENTER);
                    break;
                case VirtualKeyCode.DEL:
                    ms.add(KeyEvent.VK_DELETE);
                    break;
                case VirtualKeyCode.DOWN_ARROW:
                    ms.add(KeyEvent.VK_DOWN);
                    break;
                case VirtualKeyCode.UP_ARROW:
                    ms.add(KeyEvent.VK_UP);
                    break;
                case VirtualKeyCode.LEFT_ARROW:
                    ms.add(KeyEvent.VK_LEFT);
                    break;
                case VirtualKeyCode.RIGHT_ARROW:
                    ms.add(KeyEvent.VK_RIGHT);
                    break;
                case VirtualKeyCode.PAGE_UP:
                    ms.add(KeyEvent.VK_PAGE_UP);
                    break;
                case VirtualKeyCode.PAGE_DOWN:
                    ms.add(KeyEvent.VK_PAGE_DOWN);
                    break;
            }
        }
        return toArray(ms);
    }

    protected int[] toArray(List<Integer> l) {
        if (l.size() == 0) {
            return new int[0];
        } else {
            int[] values = new int[l.size()];
            int i = 0;
            for (int value : l) {
                values[i] = value;
                i++;
            }
            return values;
        }
    }

    public UnicodeToKeyCodeSequenceConverter getConverter() {
        return converter;
    }

    public void setConverter(UnicodeToKeyCodeSequenceConverter converter) {
        this.converter = converter;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
