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

import java.awt.Desktop;
import java.net.URI;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.desktoplib.systemtray.ApplicationShutdownService;
import com.devbury.desktoplib.upgrade.ApplicationUpgradeInfo;

public class MkRemoteUpgradeService extends Thread {

    public static final String LAST_UPDATE_CHECK = "lastUpdateCheck";
    private Logger logger = LoggerFactory
            .getLogger(MkRemoteUpgradeService.class);
    private String message;
    private String title;
    private String noDesktopMessage;
    private String noDesktopTitle;
    private VersionInfo version;
    private ApplicationShutdownService applicationShutdownService;

    public MkRemoteUpgradeService() {
        message = "Version %s of the mkRemote server is available.  Would you like to download it now?";
        title = "mkRemote Server Upgrade Available";
        noDesktopTitle = "Can Not Launch Web Browser";
        noDesktopMessage = "I can not launch a browser window on your system.  "
                + "Please navigate to %s to download version %s of the mkRemote server.";
    }

    @Override
    public void run() {
        ApplicationUpgradeInfo info = newApplicationUpgradeInfo();
        Preferences p = findPreferences();
        info.setCurrentVersionCode(version.getVersionCode());
        long last_check = p.getLong(LAST_UPDATE_CHECK, 0);
        info.setLastCheck(last_check);
        info.setUpgradeInfoUrl("http://upgrades.devbury.com/mkRemote/mkRemoteServer.properties");
        info.checkForUpgrade();
        if (last_check != info.getLastCheck()) {
            p.putLong(LAST_UPDATE_CHECK, info.getLastCheck());
            if (info.isUpgradeAvailable() && info.getUpgradeUri() != null
                    && !info.getUpgradeUri().equals("")) {
                int ret = JOptionPane
                        .showConfirmDialog(
                                null,
                                String.format(message, info.getLatestVersion()),
                                title, JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                if (ret == JOptionPane.YES_OPTION) {
                    logger.debug("go to {}", info.getUpgradeUri());
                    if (isDesktopSupported()) {
                        Desktop d = newDesktop();
                        try {
                            d.browse(new URI(info.getUpgradeUri()));
                        } catch (Throwable t) {
                            desktopErrorDialog(info);
                        }
                    } else {
                        desktopErrorDialog(info);
                    }
                    applicationShutdownService.shutdown();
                } else {
                    logger.info("Waiting until next check to download");
                }
            } else {
                logger.info("Upgrade is not available");
            }
        } else {
            logger.info("Not time to check for an upgrade");
        }
    }

    protected void desktopErrorDialog(ApplicationUpgradeInfo info) {
        JOptionPane.showMessageDialog(
                null,
                String.format(noDesktopMessage, info.getUpgradeUri(),
                        info.getLatestVersion()), noDesktopTitle,
                JOptionPane.ERROR_MESSAGE);
    }

    protected boolean isDesktopSupported() {
        return Desktop.isDesktopSupported();
    }

    protected Desktop newDesktop() {
        return Desktop.getDesktop();
    }

    protected ApplicationUpgradeInfo newApplicationUpgradeInfo() {
        return new ApplicationUpgradeInfo();
    }

    protected Preferences findPreferences() {
        return Preferences.userNodeForPackage(getClass());
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public VersionInfo getVersion() {
        return version;
    }

    public void setVersion(VersionInfo version) {
        this.version = version;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public ApplicationShutdownService getApplicationShutdownService() {
        return applicationShutdownService;
    }

    public void setApplicationShutdownService(
            ApplicationShutdownService applicationShutdownService) {
        this.applicationShutdownService = applicationShutdownService;
    }
}
