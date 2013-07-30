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
import java.awt.Desktop.Action;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.LaunchItem;

public class QuickLaunchServiceImpl implements QuickLaunchService {

    private Logger logger = LoggerFactory
            .getLogger(QuickLaunchServiceImpl.class);
    private OptionsStorageService optionsStorageService;

    public ArrayList<LaunchItem> list(String dir) {
        Base64 base64 = new Base64();
        JFileChooser chooser = new JFileChooser();
        File new_dir = newFileDir(dir);
        logger.debug("Looking for files in {}", new_dir.getAbsolutePath());
        ArrayList<LaunchItem> items = new ArrayList<LaunchItem>();
        if (isSupported()) {
            if (new_dir.isDirectory()) {
                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return !name.startsWith(".");
                    }
                };
                for (File f : new_dir.listFiles(filter)) {
                    if (!f.isHidden()) {
                        LaunchItem item = new LaunchItem();
                        item.setName(f.getName());
                        item.setPath(dir);
                        if (f.isDirectory()) {
                            if (isMac() && f.getName().endsWith(".app")) {
                                item.setType(LaunchItem.FILE_TYPE);
                                item.setName(f.getName().substring(0,
                                        f.getName().length() - 4));
                            } else {
                                item.setType(LaunchItem.DIR_TYPE);
                            }
                        } else {
                            item.setType(LaunchItem.FILE_TYPE);
                        }
                        Icon icon = chooser.getIcon(f);
                        BufferedImage bi = new BufferedImage(
                                icon.getIconWidth(), icon.getIconHeight(),
                                BufferedImage.TYPE_INT_RGB);
                        icon.paintIcon(null, bi.createGraphics(), 0, 0);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(bi, "png", os);
                            item.setIcon(base64.encodeToString(os.toByteArray()));
                        } catch (IOException e) {
                            logger.debug("could not write image {}", e);
                            item.setIcon(null);
                        }
                        logger.debug("Adding LaunchItem : {}", item);
                        items.add(item);
                    } else {
                        logger.debug("Skipping hidden file {}", f.getName());
                    }
                }
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "We are sorry but quick launch is not supported on your platform",
                                    "Quick Launch Not Supported",
                                    JOptionPane.ERROR_MESSAGE);
                }
            }.start();
        }
        return items;
    }

    public void executeAbsolute(String file) {
        final File f = newFile(file);
        if (isSupported()) {
            logger.debug("Opening {}", file);
            open(f);
        } else {
            logger.debug("Desktop is not supported");
            new Thread() {
                @Override
                public void run() {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "We are sorry but mkRemote is unable to start applications on your platform",
                                    "Could Not Launch " + f.getAbsolutePath(),
                                    JOptionPane.ERROR_MESSAGE);
                }
            }.start();
        }
    }

    public void execute(String file) {
        executeAbsolute(newFileDir(file).getAbsolutePath());
    }

    public boolean isSupported() {
        boolean supported = false;
        supported = Desktop.isDesktopSupported();
        logger.debug("isDesktopSupported {}", supported);
        if (supported) {
            supported = Desktop.getDesktop().isSupported(Action.OPEN);
            logger.debug("isOpenSupported {}", supported);
        }
        return supported;
    }

    protected void open(final File file) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "File name {} exists {}, read {}, write {}, execute {}",
                    new Object[]{file.getAbsolutePath(), file.exists(),
                            file.canRead(), file.canWrite(), file.canExecute()});
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (Throwable t) {
            if (isMac()) {
                File file_app = newFile(file.getAbsolutePath() + ".app");
                try {
                    Desktop.getDesktop().open(file_app);
                } catch (Throwable tt) {
                    handleOpenError(file, t);
                }
            } else {
                handleOpenError(file, t);
            }
        }
    }

    protected void handleOpenError(final File file, final Throwable t) {
        boolean error = true;
        logger.debug(t.toString());
        if (isWindows()) {
            try {
                runOnWindows(file);
                error = false;
            } catch (Throwable w) {
                logger.debug(w.toString());
            }
        }
        if (error) {
            new Thread() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, t.getMessage(),
                            "Error Launching " + file.getAbsolutePath(),
                            JOptionPane.ERROR_MESSAGE);
                }
            }.start();
        }
    }

    protected void runOnWindows(File f) throws Throwable {
        logger.debug("Windows launch failed.  Trying work arounds");
        String extention = f.getName().substring(f.getName().lastIndexOf("."));
        ProcessBuilder pb = new ProcessBuilder("cmd", "/C", "assoc", extention);
        Process proc = pb.start();
        Properties p = new Properties();
        p.load(new BackslashFilterInputStream(proc.getInputStream()));
        String type = p.getProperty(extention);
        pb = new ProcessBuilder("cmd", "/C", "ftype", type);
        proc = pb.start();
        p = new Properties();
        p.load(new BackslashFilterInputStream(proc.getInputStream()));
        String exe = p.getProperty(type);
        logger.debug("exe is {}", exe);
        // see if windows media player is supposed to run this
        if (exe.toLowerCase().indexOf("wmplayer") != -1) {
            String program_files = System.getenv("ProgramFiles");
            if (program_files == null) {
                program_files = "C:\\Program Files";
            }
            logger.debug("Using custom windows media player launch");
            pb = new ProcessBuilder(program_files
                    + "\\Windows Media Player\\wmplayer.exe",
                    f.getCanonicalPath());
            pb.start();
        } else {
            pb = new ProcessBuilder("cmd", "/C", f.getName());
            pb.directory(f.getParentFile());
            pb.start();
        }
    }

    protected Preferences findPreferences() {
        return Preferences.userNodeForPackage(getClass());
    }

    protected File newFile(String f) {
        return new File(f);
    }

    protected boolean isWindows() {
        return System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
    }

    protected boolean isMac() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac");
    }

    protected File newFileDir(String dir) {
        return new File(optionsStorageService.getQuickLaunchDir(), dir);
    }

    protected FileSystemView getFileSystemView() {
        return FileSystemView.getFileSystemView();
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
