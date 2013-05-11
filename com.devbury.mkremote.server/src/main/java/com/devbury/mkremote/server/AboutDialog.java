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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.devbury.desktoplib.swing.Tools;
import com.devbury.mkremote.api.ServiceInfo;

public class AboutDialog extends JDialog {

    private static final long serialVersionUID = -4042532353005884301L;
    private String imageName = "com/devbury/mkremote/server/splash.png";
    private String iconName = "com/devbury/mkremote/server/grey_mouse.png";
    private VersionInfo version;
    private ServiceInfo serviceInfo;
    private JLabel listeningLabel;
    private JLabel bluetoothListeningLabel;
    private JLabel versionLabel;
    private JLabel applicationNameLabel;
    private BluetoothServer bluetoothServer;
    private boolean initialized = false;

    public void init() {
        if (!initialized) {
            initialized = true;
            setTitle("About");
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;

            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            Image image = getImage(imageName);
            JLabel logo = new JLabel(new ImageIcon(image.getScaledInstance(250, -1, Image.SCALE_SMOOTH)));
            p.add(logo);
            c.gridy = 0;
            add(p, c);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            applicationNameLabel = new JLabel();
            p.add(applicationNameLabel);
            c.gridy = 1;
            add(p, c);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            versionLabel = new JLabel();
            p.add(versionLabel);
            c.gridy = 2;
            add(p, c);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            JLabel label = new JLabel("Copyright (c) 2009-2013 devBury LLC.  Released Under GPLv3");
            p.add(label);
            c.gridy = 3;
            add(p, c);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            label = new JLabel("www.devbury.com");
            p.add(label);
            c.gridy = 4;
            add(p, c);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            listeningLabel = new JLabel();
            p.add(listeningLabel);
            c.gridy = 5;
            add(p, c);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            bluetoothListeningLabel = new JLabel();
            p.add(bluetoothListeningLabel);
            c.gridy = 6;
            add(p, c);

            setDefaultCloseOperation(HIDE_ON_CLOSE);
            setResizable(false);
            setIconImage(getImage(iconName));
        }
        initValues();
        setLocation(centerComponent(this));
    }

    public void initValues() {
        if (initialized) {
            String t = "Listening for IP connections on " + serviceInfo.getAddress() + " port " + serviceInfo.getPort();
            listeningLabel.setText(t);
            if (bluetoothServer.isBluetoothSupported()) {
                t = "Listening for Bluetooth connections on " + bluetoothServer.getAddress() + " port "
                        + bluetoothServer.getPort();
            } else {
                t = "Bluetooth is not supported on this computer";
            }
            bluetoothListeningLabel.setText(t);
            versionLabel.setText(version.getVersion());
            applicationNameLabel.setText(version.getApplicationName());
            validate();
            pack();
            repaint();
        }
    }

    protected Image getImage(String i) {
        return Tools.getImageFromClasspath(i);
    }

    protected Point centerComponent(Component c) {
        return Tools.centerComponent(c);
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public VersionInfo getVersion() {
        return version;
    }

    public void setVersion(VersionInfo version) {
        this.version = version;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public BluetoothServer getBluetoothServer() {
        return bluetoothServer;
    }

    public void setBluetoothServer(BluetoothServer bluetoothServer) {
        this.bluetoothServer = bluetoothServer;
    }
}
