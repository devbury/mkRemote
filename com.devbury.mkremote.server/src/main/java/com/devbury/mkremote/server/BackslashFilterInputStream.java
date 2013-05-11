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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BackslashFilterInputStream extends FilterInputStream {

    public BackslashFilterInputStream(InputStream is) {
        super(is);
    }

    protected int processRead(int v) {
        return 1;
    }

    @Override
    public int read() throws IOException {
        int hold = super.read();
        return hold;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = super.read(b, off, len);
        byte[] hold = new byte[len];
        int index = 0;
        int new_count = count;
        for (int i = off; i < (count + off); i++) {
            hold[index] = b[i];
            index++;
            if (b[i] == 92) {
                hold[index] = 92;
                index++;
                new_count++;
            }
        }
        index = off;
        for (int i = 0; i < new_count; i++) {
            b[index] = hold[i];
            index++;
        }
        return new_count;
    }
}
