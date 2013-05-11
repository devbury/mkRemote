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

import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesMacroDataStore implements MacroDataStore {
    private static final String MACRO_PREFIX = "macro_";
    private Logger logger = LoggerFactory
            .getLogger(PreferencesMacroDataStore.class);

    public String loadMacro(String name) {
        return findPreferences().get(MACRO_PREFIX + name, "");
    }

    public boolean saveMacro(String name, String data) {
        String m_name = MACRO_PREFIX + name;
        findPreferences().put(m_name, data);
        flush();
        logger.debug("saved {} = {}", m_name, data);
        return true;
    }

    public ArrayList<String> listMacros() {
        ArrayList<String> names = new ArrayList<String>();
        try {
            for (String key : findPreferences().keys()) {
                if (key.startsWith(MACRO_PREFIX)) {
                    String name = key.substring(MACRO_PREFIX.length());
                    names.add(name);
                    logger.debug("Added macro {}", name);
                }
            }
        } catch (BackingStoreException e) {
            logger.debug("Error listing macro names {}", e.toString());
        }
        return names;
    }

    public void delete(String name) {
        findPreferences().remove(MACRO_PREFIX + name);
    }

    protected void flush() {
        try {
            findPreferences().flush();
        } catch (BackingStoreException e) {
            logger.warn("Could not flush {}", e.toString());
        }
    }

    protected Preferences findPreferences() {
        return Preferences.userNodeForPackage(getClass());
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
