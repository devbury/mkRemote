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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.JsonService;
import com.devbury.mkremote.api.PasswordStorageService;

public class MacroServiceImpl implements MacroService {

    public static final String SEPERATOR = "mkEnD";
    private Logger logger = LoggerFactory.getLogger(MacroServiceImpl.class);
    private MacroDataStore macroDataStore;
    private DesktopControlService desktopControlService;
    private PasswordStorageService passwordStorageService;
    private JsonService jsonService;
    private StringBuffer sb;
    private boolean recording = false;
    private MacroThread macroThread;

    public void deleteMacro(String name) {
        macroDataStore.delete(name);
    }

    public void record(Object o) {
        if (recording) {
            logger.debug("Adding {} to {}", o, sb);
            sb.append(o);
            sb.append(SEPERATOR);
        }
    }

    public void abortRecord() {
        logger.debug("abort recording");
        sb = null;
        recording = false;
    }

    public boolean isRecording() {
        return recording;
    }

    public boolean saveMacro(String name) {
        logger.debug("finished recording");
        recording = false;
        return macroDataStore.saveMacro(name, sb.toString());
    }

    public void startRecord() {
        logger.debug("Starting to record");
        sb = new StringBuffer();
        recording = true;
    }

    public ArrayList<String> listMacros() {
        ArrayList<String> hold = macroDataStore.listMacros();
        Collections.sort(hold);
        return hold;
    }

    public boolean executeMacro(String name) {
        if (macroThread != null && macroThread.isAlive()) {
            return false;
        }
        macroThread = new MacroThread();
        macroThread.setName("MacroThread");
        macroThread.setMacro(macroDataStore.loadMacro(name));
        macroThread.start();
        return true;
    }

    public MacroDataStore getMacroDataStore() {
        return macroDataStore;
    }

    public void setMacroDataStore(MacroDataStore macroDataStore) {
        this.macroDataStore = macroDataStore;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public DesktopControlService getDesktopControlService() {
        return desktopControlService;
    }

    public void setDesktopControlService(
            DesktopControlService desktopControlService) {
        this.desktopControlService = desktopControlService;
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    public PasswordStorageService getPasswordStorageService() {
        return passwordStorageService;
    }

    public void setPasswordStorageService(
            PasswordStorageService passwordStorageService) {
        this.passwordStorageService = passwordStorageService;
    }

    private class MacroThread extends Thread {

        private String macro;

        public void setMacro(String macro) {
            this.macro = macro;
        }

        @Override
        public void run() {
            StringBuffer sb = new StringBuffer();
            DataPacket lp = new DataPacket(DataPacket.LOGIN);
            lp.setS(passwordStorageService
                    .getPassword(PasswordStorageService.PASSWORD_KEY));
            sb.append(jsonService.toJson(lp));
            sb.append(SEPERATOR);
            sb.append(macro);
            sb.append(jsonService.toJson(new DataPacket(DataPacket.MACRO_END)));
            MacroInputStream in = new MacroInputStream(sb.toString(), SEPERATOR);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            logger.debug("Starting macro");
            desktopControlService.performService(in, out, 10);
            logger.debug("Macro done");
        }
    }
}
