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

import java.awt.Point;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.JsonService;

public class MacroRecordJsonService implements JsonService {

    private Logger logger = LoggerFactory
            .getLogger(MacroRecordJsonService.class);
    private JsonService jsonService;
    private MacroService macroService;
    private MouseAndKeyService mouseAndKeyService;
    private ArrayList<Integer> ignoreDataPackets;

    public MacroRecordJsonService() {
        ignoreDataPackets = new ArrayList<Integer>();
        ignoreDataPackets.add(DataPacket.MACRO_START);
        ignoreDataPackets.add(DataPacket.MACRO_SAVE);
        ignoreDataPackets.add(DataPacket.MACRO_ABORT);
        ignoreDataPackets.add(DataPacket.MACRO_LIST);
        ignoreDataPackets.add(DataPacket.MACRO_DELETE);
        ignoreDataPackets.add(DataPacket.MACRO_END);
        ignoreDataPackets.add(DataPacket.MOUSE_MOVE_SERVICE_INFO);
        ignoreDataPackets.add(DataPacket.QUICK_LAUNCH_LIST);
        ignoreDataPackets.add(DataPacket.LOGIN);
    }

    protected void record(Object o) {
        if (macroService.isRecording()) {
            if (o instanceof DataPacket) {
                DataPacket dp = (DataPacket) o;
                if (!ignoreDataPackets.contains(dp.getType())) {
                    if (dp.getType() == DataPacket.MACRO_DELAY_RECORD) {
                        DataPacket delay = new DataPacket(
                                DataPacket.MACRO_DELAY);
                        delay.setX(dp.getX());
                        macroService.record(jsonService.toJson(delay));
                    } else if (dp.getType() == DataPacket.LEFT_BUTTON_DOWN
                            || dp.getType() == DataPacket.LEFT_BUTTON_UP
                            || dp.getType() == DataPacket.MIDDLE_BUTTON_DOWN
                            || dp.getType() == DataPacket.MIDDLE_BUTTON_UP
                            || dp.getType() == DataPacket.RIGHT_BUTTON_DOWN
                            || dp.getType() == DataPacket.RIGHT_BUTTON_UP
                            || dp.getType() == DataPacket.DOUBLE_CLICK) {
                        Point p = mouseAndKeyService.currentMouseLocation();
                        DataPacket move = new DataPacket(
                                DataPacket.MOUSE_MOVE_TO);
                        move.setX(p.x);
                        move.setY(p.y);
                        macroService.record(jsonService.toJson(move));
                        macroService.record(jsonService.toJson(dp));
                    } else {
                        macroService.record(jsonService.toJson(o));
                    }
                } else {
                    logger.debug("skipping {}", dp.getType());
                }
            } else {
                macroService.record(jsonService.toJson(o));
            }
        }
    }

    public <T> T fromJson(String s, Class<T> clazz) {
        T o = jsonService.fromJson(s, clazz);
        record(o);
        return o;
    }

    public <T> T fromJson(Reader r, Class<T> clazz) {
        T hold = jsonService.fromJson(r, clazz);
        record(hold);
        return hold;
    }

    public String toJson(Object o) {
        return jsonService.toJson(o);
    }

    public void toJson(Object o, Writer w) {
        jsonService.toJson(o, w);
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    public MacroService getMacroService() {
        return macroService;
    }

    public void setMacroService(MacroService macroService) {
        this.macroService = macroService;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public MouseAndKeyService getMouseAndKeyService() {
        return mouseAndKeyService;
    }

    public void setMouseAndKeyService(MouseAndKeyService mouseAndKeyService) {
        this.mouseAndKeyService = mouseAndKeyService;
    }

}
