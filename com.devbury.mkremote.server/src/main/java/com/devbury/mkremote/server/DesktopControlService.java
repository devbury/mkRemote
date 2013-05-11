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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.BoxeePercentageResponsePacket;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.JsonService;
import com.devbury.mkremote.api.LoginResponsePacket;
import com.devbury.mkremote.api.MacroListResponsePacket;
import com.devbury.mkremote.api.QuickLaunchListResponsePacket;
import com.devbury.mkremote.api.ServiceInfo;
import com.devbury.mkremote.server.boxee.IBoxeeService;

public class DesktopControlService implements Service {

    private Logger logger = LoggerFactory
            .getLogger(DesktopControlService.class);
    private JsonService jsonService;
    private MouseAndKeyService mouseAndKeyService;
    private UnicodeToKeyCodeSequenceConverter unicodeConverter;
    private PasswordService passwordService;
    private QuickLaunchService quickLaunchService;
    private ServiceInfo mouseMoveServiceInfo;
    private MacroService macroService;
    private BluetoothServer bluetoothServer;
    private BluetoothMouseMoveServer bluetoothMouseMoveServer;
    private IBoxeeService boxeeService;

    protected void delay(long delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }

    public void performService(InputStream in, OutputStream out) {
        performService(in, out, 0);
    }

    public void performService(InputStream in, OutputStream out, long delay) {
        BufferedInputStream bis = new BufferedInputStream(in);
        Reader reader = new InputStreamReader(bis);
        Writer writer = new OutputStreamWriter(out);
        // validate password
        DataPacket lp = jsonService.fromJson(reader, DataPacket.class);
        if (lp.getType() == DataPacket.SERVICE_PING) {
            ServiceInfo si = mouseMoveServiceInfo.clone();
            jsonService.toJson(si, writer);
            flush(writer);
            return;
        }
        LoginResponsePacket lrp = new LoginResponsePacket();
        if (passwordService.isValidPassword(lp.getS())) {
            lrp.setStatus(LoginResponsePacket.GOOD_PASSWORD);
            lrp.setMacroMode(macroService.isRecording());
            jsonService.toJson(lrp, writer);
            flush(writer);
        } else {
            logger.debug("Bad password");
            lrp.setStatus(LoginResponsePacket.BAD_PASSWORD);
            jsonService.toJson(lrp, writer);
            flush(writer);
            return;
        }
        boolean stop = false;
        while (!stop) {
            try {
                flush(writer);
                DataPacket p = jsonService.fromJson(reader, DataPacket.class);
                switch (p.getType()) {
                    case DataPacket.LEFT_BUTTON_DOWN:
                        mouseAndKeyService.leftButtonDown();
                        break;
                    case DataPacket.LEFT_BUTTON_UP:
                        mouseAndKeyService.leftButtonUp();
                        break;
                    case DataPacket.DOUBLE_CLICK:
                        mouseAndKeyService.leftButtonDown();
                        mouseAndKeyService.leftButtonUp();
                        mouseAndKeyService.leftButtonDown();
                        mouseAndKeyService.leftButtonUp();
                        break;
                    case DataPacket.MIDDLE_BUTTON_DOWN:
                        mouseAndKeyService.middleButtonDown();
                        break;
                    case DataPacket.MIDDLE_BUTTON_UP:
                        mouseAndKeyService.middleButtonUp();
                        break;
                    case DataPacket.RIGHT_BUTTON_DOWN:
                        mouseAndKeyService.rightButtonDown();
                        break;
                    case DataPacket.RIGHT_BUTTON_UP:
                        mouseAndKeyService.rightButtonUp();
                        break;
                    case DataPacket.KEY_PRESS:
                        mouseAndKeyService.keySequence(unicodeConverter.convert(p
                                .getUnicode()));
                        break;
                    case DataPacket.VIRTUAL_KEY_PRESS:
                        mouseAndKeyService.keySequence(p);
                        break;
                    case DataPacket.PAGE_UP:
                        mouseAndKeyService.keyPressed(KeyEvent.VK_PAGE_UP);
                        mouseAndKeyService.keyReleased(KeyEvent.VK_PAGE_UP);
                        break;
                    case DataPacket.PAGE_DOWN:
                        mouseAndKeyService.keyPressed(KeyEvent.VK_PAGE_DOWN);
                        mouseAndKeyService.keyReleased(KeyEvent.VK_PAGE_DOWN);
                        break;
                    case DataPacket.SCROLL_UP:
                        mouseAndKeyService.scrollUp();
                        break;
                    case DataPacket.SCROLL_DOWN:
                        mouseAndKeyService.scrollDown();
                        break;
                    case DataPacket.UP_ARROW:
                        mouseAndKeyService.keyPressed(KeyEvent.VK_UP);
                        mouseAndKeyService.keyReleased(KeyEvent.VK_UP);
                        break;
                    case DataPacket.DOWN_ARROW:
                        mouseAndKeyService.keyPressed(KeyEvent.VK_DOWN);
                        mouseAndKeyService.keyReleased(KeyEvent.VK_DOWN);
                        break;
                    case DataPacket.LEFT_ARROW:
                        mouseAndKeyService.keyPressed(KeyEvent.VK_LEFT);
                        mouseAndKeyService.keyReleased(KeyEvent.VK_LEFT);
                        break;
                    case DataPacket.RIGHT_ARROW:
                        mouseAndKeyService.keyPressed(KeyEvent.VK_RIGHT);
                        mouseAndKeyService.keyReleased(KeyEvent.VK_RIGHT);
                        break;
                    case DataPacket.DEL_PRESS:
                        mouseAndKeyService.keyPressed(KeyEvent.VK_DELETE);
                        mouseAndKeyService.keyReleased(KeyEvent.VK_DELETE);
                        break;
                    case DataPacket.QUICK_LAUNCH_LIST:
                        logger.debug("quick launch list");
                        QuickLaunchListResponsePacket qr = new QuickLaunchListResponsePacket();
                        qr.setItems(quickLaunchService.list(p.getS()));
                        jsonService.toJson(qr, writer);
                        flush(writer);
                        break;
                    case DataPacket.QUICK_LAUNCH:
                        logger.debug("quick launch");
                        quickLaunchService.execute(p.getS());
                        break;
                    case DataPacket.MAXIMIZE:
                        logger.debug("maximize");
                        mouseAndKeyService.maximize();
                        break;
                    case DataPacket.MOUSE_MOVE_SERVICE_INFO:
                        logger.debug("MouseMoveServiceInfo request");
                        ServiceInfo si = mouseMoveServiceInfo.clone();
                        if (bluetoothServer.isConnected()) {
                            si.setPort(bluetoothMouseMoveServer.getPort());
                            si.setAddress(bluetoothMouseMoveServer.getAddress());
                        }
                        jsonService.toJson(si, writer);
                        flush(writer);
                        logger.debug("responded with {}", si);
                        break;
                    case DataPacket.MACRO_START:
                        logger.debug("Macro start");
                        macroService.startRecord();
                        break;
                    case DataPacket.MACRO_SAVE:
                        logger.debug("Macro save");
                        macroService.saveMacro(p.getS());
                        break;
                    case DataPacket.MACRO_ABORT:
                        logger.debug("Macro abort");
                        macroService.abortRecord();
                        break;
                    case DataPacket.MACRO_LIST:
                        logger.debug("Macro list");
                        MacroListResponsePacket r = new MacroListResponsePacket();
                        r.setMacroNames(macroService.listMacros());
                        jsonService.toJson(r, writer);
                        flush(writer);
                        break;
                    case DataPacket.MACRO_END:
                        logger.debug("Macro end");
                        stop = true;
                        break;
                    case DataPacket.MACRO_EXECUTE:
                        logger.debug("Macro execute");
                        macroService.executeMacro(p.getS());
                        break;
                    case DataPacket.MACRO_DELETE:
                        logger.debug("Macro delete");
                        macroService.deleteMacro(p.getS());
                        break;
                    case DataPacket.MOUSE_MOVE_TO:
                        logger.debug("Mouse move to {}, {}", p.getX(), p.getY());
                        mouseAndKeyService.mouseTo(p.getX(), p.getY());
                        break;
                    case DataPacket.MACRO_DELAY:
                        logger.debug("delay {} seconds", p.getX());
                        try {
                            Thread.sleep(1000 * p.getX());
                        } catch (InterruptedException ie) {
                        }
                        break;
                    case DataPacket.BOXEE_UP:
                        boxeeService.up();
                        break;
                    case DataPacket.BOXEE_DOWN:
                        boxeeService.down();
                        break;
                    case DataPacket.BOXEE_LEFT:
                        boxeeService.left();
                        break;
                    case DataPacket.BOXEE_RIGHT:
                        boxeeService.right();
                        break;
                    case DataPacket.BOXEE_BACK:
                        boxeeService.back();
                        break;
                    case DataPacket.BOXEE_PLAY_NEXT:
                        boxeeService.playNext();
                        break;
                    case DataPacket.BOXEE_PAUSE:
                        boxeeService.pause();
                        break;
                    case DataPacket.BOXEE_MUTE:
                        boxeeService.mute();
                        break;
                    case DataPacket.BOXEE_SELECT:
                        boxeeService.select();
                        break;
                    case DataPacket.BOXEE_STOP:
                        boxeeService.stop();
                        break;
                    case DataPacket.BOXEE_VOLUME_UP:
                        jsonService.toJson(adjustBoxeeVolume(5), writer);
                        flush(writer);
                        break;
                    case DataPacket.BOXEE_VOLUME_DOWN:
                        jsonService.toJson(adjustBoxeeVolume(-5), writer);
                        flush(writer);
                        break;
                    case DataPacket.BOXEE_PLAYED_PERCENTAGE:
                        jsonService.toJson(boxeePercentagePlayed(), writer);
                        flush(writer);
                        break;
                    case DataPacket.BOXEE_SET_PLAYED_PERCENTAGE:
                        boxeeService.seekPercentage(p.getPercentage());
                        break;
                }
                delay(delay);
            } catch (Throwable t) {
                logger.trace("Problem processing packet.  Ending service {}",
                        t.toString());
                stop = true;
            }
        }
    }

    protected BoxeePercentageResponsePacket boxeePercentagePlayed() {
        BoxeePercentageResponsePacket p = new BoxeePercentageResponsePacket();
        p.setPercentage(boxeeService.getPercentage());
        return p;
    }

    protected BoxeePercentageResponsePacket adjustBoxeeVolume(int diff) {
        BoxeePercentageResponsePacket vp = new BoxeePercentageResponsePacket();
        vp.setPercentage(boxeeService.getVolume() + diff);
        if (vp.getPercentage() < 0) {
            vp.setPercentage(0);
        } else if (vp.getPercentage() > 100) {
            vp.setPercentage(100);
        }

        boxeeService.setVolume(vp.getPercentage());
        return vp;
    }

    protected void flush(Writer w) {
        try {
            w.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    public MouseAndKeyService getMouseAndKeyService() {
        return mouseAndKeyService;
    }

    public void setMouseAndKeyService(MouseAndKeyService mouseAndKeyService) {
        this.mouseAndKeyService = mouseAndKeyService;
    }

    public UnicodeToKeyCodeSequenceConverter getUnicodeConverter() {
        return unicodeConverter;
    }

    public void setUnicodeConverter(
            UnicodeToKeyCodeSequenceConverter unicodeConverter) {
        this.unicodeConverter = unicodeConverter;
    }

    public PasswordService getPasswordService() {
        return passwordService;
    }

    public void setPasswordService(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public QuickLaunchService getQuickLaunchService() {
        return quickLaunchService;
    }

    public void setQuickLaunchService(QuickLaunchService quickLaunchService) {
        this.quickLaunchService = quickLaunchService;
    }

    public ServiceInfo getMouseMoveServiceInfo() {
        return mouseMoveServiceInfo;
    }

    public void setMouseMoveServiceInfo(ServiceInfo mouseMoveServiceInfo) {
        this.mouseMoveServiceInfo = mouseMoveServiceInfo;
    }

    public MacroService getMacroService() {
        return macroService;
    }

    public void setMacroService(MacroService macroService) {
        this.macroService = macroService;
    }

    public BluetoothServer getBluetoothServer() {
        return bluetoothServer;
    }

    public void setBluetoothServer(BluetoothServer bluetoothServer) {
        this.bluetoothServer = bluetoothServer;
    }

    public BluetoothMouseMoveServer getBluetoothMouseMoveServer() {
        return bluetoothMouseMoveServer;
    }

    public void setBluetoothMouseMoveServer(
            BluetoothMouseMoveServer bluetoothMouseMoveServer) {
        this.bluetoothMouseMoveServer = bluetoothMouseMoveServer;
    }

    public IBoxeeService getBoxeeService() {
        return boxeeService;
    }

    public void setBoxeeService(IBoxeeService boxeeService) {
        this.boxeeService = boxeeService;
    }
}
