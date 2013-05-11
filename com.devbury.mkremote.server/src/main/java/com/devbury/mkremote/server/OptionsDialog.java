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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.desktoplib.swing.DialogLayout;
import com.devbury.desktoplib.swing.Tools;
import com.devbury.mkremote.server.boxee.BoxeeService;


public class OptionsDialog extends JDialog {

    private static final long serialVersionUID = 4227747087738662375L;
    private Logger logger = LoggerFactory.getLogger(OptionsDialog.class);
    private OptionsStorageService optionsStorageService;
    private String imageName = "com/devbury/mkremote/server/grey_mouse.png";
    private JComboBox selectedInterface;
    private JTextField password;
    private JTextField quickLaunchFolder;
    private JTextField boxeePort;
    private JRadioButton gnome;
    private JRadioButton kde;
    private ButtonGroup linuxDesktopGroup;
    private SocketServer socketServer;
    private MouseMoveServer mouseMoveServer;
    private MulticastServiceAnnouncer multicastServiceAnnouncer;
    private QuickLaunchService quickLaunchService;
    private NetworkAddressService networkAddressService;
    private String preferredInterface;
    private AboutDialog aboutDialog;
    private Collection<String> interfaces;
    private boolean initialized = false;
    private BoxeeService boxeeService;

    protected Image getImage(String name) {
        return Tools.getImageFromClasspath(name);
    }

    protected Point centerComponent(Component c) {
        return Tools.centerComponent(c);
    }

    public void initValues() {
        if (preferredInterface == null && interfaces.size() > 1) {
            selectedInterface.setSelectedItem(networkAddressService
                    .getNetworkName());
        }
        if (isLinux()) {
            if (optionsStorageService.getLinuxDesktop().equals("gnome")) {
                linuxDesktopGroup.setSelected(gnome.getModel(), true);
            } else {
                linuxDesktopGroup.setSelected(kde.getModel(), true);
            }
        }
        password.setText(optionsStorageService.getPassword("password"));
        if (quickLaunchService.isSupported()) {
            quickLaunchFolder
                    .setText(optionsStorageService.getQuickLaunchDir());
        }
        boxeePort.setText(String.valueOf(optionsStorageService.getBoxeePort()));
        pack();
    }

    public void init() {
        if (!initialized) {
            initialized = true;
            EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
            setTitle("mkRemote Options");
            JPanel p = new JPanel();
            p.setLayout(new DialogLayout());
            interfaces = networkAddressService.findPossibleNetworkAddresses();
            if (preferredInterface == null && interfaces.size() > 1) {
                JLabel l = new JLabel("Interface:");
                l.setBorder(eb);
                p.add(l);
                selectedInterface = new JComboBox();
                for (String s : interfaces) {
                    selectedInterface.addItem(s);
                }
                selectedInterface.setEditable(false);
                p.add(selectedInterface);
            }

            if (isLinux()) {
                JPanel desktop = new JPanel();
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0;
                c.gridy = 0;
                desktop.setLayout(new GridBagLayout());
                JLabel l = new JLabel("Linux Desktop:");
                l.setBorder(eb);
                desktop.add(l, c);
                linuxDesktopGroup = new ButtonGroup();
                gnome = new JRadioButton();
                gnome.setText("Gnome");
                linuxDesktopGroup.add(gnome);
                c.gridx = 1;
                desktop.add(gnome);
                kde = new JRadioButton();
                kde.setText("KDE");
                linuxDesktopGroup.add(kde);
                c.gridx = 2;
                desktop.add(kde);
                p.add(desktop);
            }

            JLabel l = new JLabel("Password:");
            l.setBorder(eb);
            p.add(l);
            password = new JTextField(15);
            p.add(password);

            if (quickLaunchService.isSupported()) {
                JPanel chooser = new JPanel();
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0;
                c.gridy = 0;
                chooser.setLayout(new GridBagLayout());
                l = new JLabel("Quick Launch Folder:");
                l.setBorder(eb);
                chooser.add(l, c);
                quickLaunchFolder = new JTextField(20);
                quickLaunchFolder.setEditable(false);
                c.gridy = 1;
                chooser.add(quickLaunchFolder, c);
                JButton browse = new JButton("Browse");
                browse.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser c = new JFileChooser(quickLaunchFolder
                                .getText());
                        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int returnVal = c.showDialog(null, "Select Directory");
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            quickLaunchFolder.setText(c.getSelectedFile()
                                    .getAbsolutePath());
                        }
                    }
                });
                c.gridx = 1;
                chooser.add(browse, c);
                JButton open = new JButton("Open");
                open.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        quickLaunchService.executeAbsolute(quickLaunchFolder
                                .getText());
                    }
                });
                c.gridx = 2;
                chooser.add(open, c);
                p.add(chooser);
            } else {
                p.add(new JLabel("Quick Launch :"));
                JTextField not_supported = new JTextField();
                not_supported.setText(" Not Supported On Your Platform");
                not_supported.setEditable(false);
                p.add(not_supported);
            }

            JLabel boxee_port_l = new JLabel("Boxee Port :");
            boxee_port_l.setBorder(eb);
            p.add(boxee_port_l);
            boxeePort = new JTextField(5);
            p.add(boxeePort);
            JButton update = new JButton("Update");
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    boolean changed = false;
                    if (preferredInterface == null && interfaces.size() > 1) {
                        String n = ((String) selectedInterface
                                .getSelectedItem()).split(":")[0];
                        if (!n.equals(optionsStorageService
                                .getNetworkInterface())) {
                            changed = true;
                            optionsStorageService.saveNetworkInterface(n);
                        }
                    }
                    if (!password.getText().equals(
                            optionsStorageService.getPassword("password"))) {
                        changed = true;
                        optionsStorageService.savePassword("password",
                                password.getText());
                    }
                    optionsStorageService.saveQuickLaunchDir(quickLaunchFolder
                            .getText());
                    if (isLinux()) {
                        if (gnome.isSelected()) {
                            logger.debug("Saving desktop as gnome");
                            optionsStorageService.saveLinuxDesktop("gnome");
                        } else {
                            logger.debug("Saving desktop as kde");
                            optionsStorageService.saveLinuxDesktop("kde");
                        }
                    }
                    optionsStorageService.saveBoxeePort(boxeePort.getText());
                    boxeeService.init();
                    setVisible(false);
                    if (changed) {
                        multicastServiceAnnouncer.shutdown();
                        socketServer.shutdown();
                        mouseMoveServer.shutdown();
                        mouseMoveServer.start();
                        socketServer.start();
                        multicastServiceAnnouncer.start();
                        aboutDialog.initValues();
                    }
                }
            });
            p.add(update);
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setVisible(false);
                }
            });
            p.add(cancel);
            getContentPane().add(p);
            setDefaultCloseOperation(HIDE_ON_CLOSE);
            setResizable(false);
            setIconImage(getImage(imageName));
        }
        initValues();
        setLocation(centerComponent(this));
    }

    protected boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public OptionsStorageService getOptionsStorageService() {
        return optionsStorageService;
    }

    public void setOptionsStorageService(
            OptionsStorageService optionsStorageService) {
        this.optionsStorageService = optionsStorageService;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public SocketServer getSocketServer() {
        return socketServer;
    }

    public void setSocketServer(SocketServer socketServer) {
        this.socketServer = socketServer;
    }

    public MulticastServiceAnnouncer getMulticastServiceAnnouncer() {
        return multicastServiceAnnouncer;
    }

    public void setMulticastServiceAnnouncer(
            MulticastServiceAnnouncer multicastServiceAnnouncer) {
        this.multicastServiceAnnouncer = multicastServiceAnnouncer;
    }

    public QuickLaunchService getQuickLaunchService() {
        return quickLaunchService;
    }

    public void setQuickLaunchService(QuickLaunchService quickLaunchService) {
        this.quickLaunchService = quickLaunchService;
    }

    public MouseMoveServer getMouseMoveServer() {
        return mouseMoveServer;
    }

    public void setMouseMoveServer(MouseMoveServer mouseMoveServer) {
        this.mouseMoveServer = mouseMoveServer;
    }

    public NetworkAddressService getNetworkAddressService() {
        return networkAddressService;
    }

    public void setNetworkAddressService(
            NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    public String getPreferredInterface() {
        return preferredInterface;
    }

    public void setPreferredInterface(String preferredInterface) {
        this.preferredInterface = preferredInterface;
    }

    public AboutDialog getAboutDialog() {
        return aboutDialog;
    }

    public void setAboutDialog(AboutDialog aboutDialog) {
        this.aboutDialog = aboutDialog;
    }

    public BoxeeService getBoxeeService() {
        return boxeeService;
    }

    public void setBoxeeService(BoxeeService boxeeService) {
        this.boxeeService = boxeeService;
    }
}
