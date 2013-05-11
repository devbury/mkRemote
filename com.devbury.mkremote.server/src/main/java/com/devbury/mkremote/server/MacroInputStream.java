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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacroInputStream extends InputStream {
    private Logger logger = LoggerFactory.getLogger(MacroInputStream.class);
    private ArrayList<String> macro;
    private Iterator<String> iterator;

    public MacroInputStream(String macroString, String seperator) {
        macro = new ArrayList<String>();
        for (String m : macroString.split(seperator)) {
            macro.add(m);
            logger.debug("Added {}", m);
        }
        iterator = macro.iterator();
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (iterator.hasNext()) {
            byte[] v = iterator.next().getBytes();
            for (int i = 0; i < v.length; i++) {
                b[off + i] = v[i];
            }
            logger.debug(new String(b, off, v.length));
            return v.length;
        } else {
            logger.debug("No more strings left");
            return -1;
        }
    }
}