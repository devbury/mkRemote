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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesOptionsStorageService implements OptionsStorageService {

    public static final String NETWORK_PORT = "networkPort";
    public static final String NETWORK_INTERFACE = "networkInterface";
    public static final String QUICK_LAUNCH_DIR = "quickLaunchDir";
    public static final String LINUX_DESKTOP = "linuxDesktop";
    public static final String BOXEE_PORT = "boxeePort";
    public static final String BOXEE_USERID = "boxeeUserid";
    public static final String BOXEE_PASSWORD = "boxeePassword";
    private Logger logger = LoggerFactory
            .getLogger(PreferencesOptionsStorageService.class);

    public String getBoxeePassword() {
        return findPreferences().get(BOXEE_PASSWORD, null);
    }

    public String getBoxeePort() {
        return findPreferences().get(BOXEE_PORT, "8800");
    }

    public String getBoxeeUserid() {
        return findPreferences().get(BOXEE_USERID, null);
    }

    public void saveBoxeePassword(String password) {
        findPreferences().put(BOXEE_PASSWORD, password);
        flush();
    }

    public void saveBoxeePort(String port) {
        findPreferences().put(BOXEE_PORT, port);
        flush();
    }

    public void saveBoxeeUserid(String userid) {
        findPreferences().put(BOXEE_USERID, userid);
        flush();
    }

    public String getNetworkInterface() {
        return findPreferences().get(NETWORK_INTERFACE, null);
    }

    public void saveNetworkInterface(String key) {
        findPreferences().put(NETWORK_INTERFACE, key);
        flush();
    }

    public String getPassword(String key) {
        return findPreferences().get(key, "");
    }

    public void savePassword(String key, String password) {
        findPreferences().put(key, password);
        flush();
    }

    public String getQuickLaunchDir() {
        return findPreferences().get(QUICK_LAUNCH_DIR, getUserHome());
    }

    public void saveQuickLaunchDir(String dir) {
        findPreferences().put(QUICK_LAUNCH_DIR, dir);
        flush();
    }

    public String getLinuxDesktop() {
        return findPreferences().get(LINUX_DESKTOP, "gnome");
    }

    public void saveLinuxDesktop(String desktop) {
        findPreferences().put(LINUX_DESKTOP, desktop);
        flush();
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

    protected String getUserHome() {
        return System.getProperty("user.home");
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
