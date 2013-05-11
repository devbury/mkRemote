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

package com.devbury.desktoplib.upgrade;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationUpgradeInfo {

    public static final String LATEST_VERSION_CODE = "latestVersionCode";
    public static final String UPGRADE_URI = "upgradeUri";
    public static final String LATEST_VERSION = "latestVersion";
    private Logger logger = LoggerFactory.getLogger(ApplicationUpgradeInfo.class);
    private String upgradeInfoUrl;
    private int currentVersionCode;
    private long lastCheck = 0;
    private long checkInterval = 1000 * 60 * 60;
    private String upgradeUri;
    private String latestVersion;
    private int latestVersionCode = 0;

    protected InputStream openInputStream() throws Throwable {
        URL u = new URL(upgradeInfoUrl);
        URLConnection con = u.openConnection();
        con.setConnectTimeout(10000);
        return new BufferedInputStream(con.getInputStream());
    }

    public void checkForUpgrade() {
        long time = currentTime();
        if ((time - lastCheck) > checkInterval) {
            lastCheck = time;
            try {
                InputStream in = openInputStream();
                Properties p = new Properties();
                p.load(in);
                in.close();
                upgradeUri = p.getProperty(UPGRADE_URI);
                latestVersion = p.getProperty(LATEST_VERSION);
                latestVersionCode = Integer.parseInt(p.getProperty(LATEST_VERSION_CODE));
                logger.debug("Received upgrade info.  latestVersionCode = {}, latestVersion = {}, upgradeUri = {}",
                        new Object[]{latestVersionCode, latestVersion, upgradeUri});
            } catch (Throwable t) {
                logger.error("Could not get upgrade info from {} {}", upgradeInfoUrl, t);
            }
        }
    }

    public boolean isUpgradeAvailable() {
        return (latestVersionCode > currentVersionCode);
    }

    protected long currentTime() {
        return System.currentTimeMillis();
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getUpgradeInfoUrl() {
        return upgradeInfoUrl;
    }

    public void setUpgradeInfoUrl(String upgradeInfoUrl) {
        this.upgradeInfoUrl = upgradeInfoUrl;
    }

    public int getCurrentVersionCode() {
        return currentVersionCode;
    }

    public void setCurrentVersionCode(int currentVersionCode) {
        this.currentVersionCode = currentVersionCode;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(long lastCheck) {
        this.lastCheck = lastCheck;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public String getUpgradeUri() {
        return upgradeUri;
    }

    public void setUpgradeUri(String upgradeUri) {
        this.upgradeUri = upgradeUri;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public int getLatestVersionCode() {
        return latestVersionCode;
    }

    public void setLatestVersionCode(int latestVersionCode) {
        this.latestVersionCode = latestVersionCode;
    }
}
