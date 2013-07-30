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
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaximizeServiceImpl implements MaximizeService {

    private Logger logger = LoggerFactory.getLogger(MaximizeServiceImpl.class);
    private OptionsStorageService optionsStorageService;

    public int[] getMaximizeSequence() {
        return getDefaultSequence();
    }

    public int[] getDefaultSequence() {
        ArrayList<Integer> ms = new ArrayList<Integer>();
        String name = getOsName().toLowerCase();
        if (name.startsWith("windows")) {
            ms.add(KeyEvent.VK_ALT);
            ms.add(KeyEvent.VK_SPACE);
            ms.add(KeyEvent.VK_X);
        } else if (name.startsWith("linux")) {
            if (optionsStorageService.getLinuxDesktop().equals("gnome")) {
                ms.add(KeyEvent.VK_ALT);
                ms.add(KeyEvent.VK_F10);
            } else {
                ms.add(KeyEvent.VK_WINDOWS);
                ms.add(KeyEvent.VK_PLUS);
            }
        } else if (name.startsWith("mac")) {
            logger.warn("Mac does not support screen maximize");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Maximize sequence for {} is {}", getOsName(),
                    toString(ms));
        }
        return toArray(ms);
    }

    protected String toString(List<Integer> l) {
        StringBuffer sb = new StringBuffer();
        Iterator<Integer> it = l.iterator();
        while (it.hasNext()) {
            sb.append(KeyEvent.getKeyText(it.next()));
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    protected String getOsName() {
        return System.getProperty("os.name");
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
}
