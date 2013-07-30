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

package com.devbury.mkremote.api;

import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

public class Bluetooth {
    public static final String MOUSEMOVESERVICE = "4B3D08B2AE8D3EB2D2C07ECA9D386E97";
    public static final String MAINSERVICE = "4B3D66FFAD8E4030CEFD2C7364AE3BD7";
    public static final UUID MAINSERVICE_UUID = new UUID(0x4B3D66FFAD8E4030L, 0xCEFD2C7364AE3BD7L);
    public static final UUID MOUSEMOVESERVICE_UUID = new UUID(0x4B3D08B2AE8D3EB2L, 0xD2C07ECA9D386E97L);

    public static String formatAddress(String a) {
        StringReader r = new StringReader(a.replace(":", "").toUpperCase());
        StringBuilder newAddress = new StringBuilder();
        int c;
        int counter = 0;
        try {
            while ((c = r.read()) != -1) {
                if (counter == 2) {
                    newAddress.append(":");
                    counter = 0;
                }
                newAddress.append((char) c);

                counter++;
            }
        } catch (IOException e) {
            return a;
        }
        return newAddress.toString();
    }
}
