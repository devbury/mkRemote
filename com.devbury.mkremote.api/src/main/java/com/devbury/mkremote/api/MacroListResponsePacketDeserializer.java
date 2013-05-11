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

package com.devbury.mkremote.api;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class MacroListResponsePacketDeserializer implements JsonDeserializer<MacroListResponsePacket> {
    @Override
    public MacroListResponsePacket deserialize(JsonElement json, Type t, JsonDeserializationContext context)
            throws JsonParseException {
        MacroListResponsePacket r = new MacroListResponsePacket();
        ArrayList<String> names = new ArrayList<String>();
        r.setMacroNames(names);
        Iterator<JsonElement> it = json.getAsJsonObject().get("macroNames").getAsJsonArray().iterator();
        while (it.hasNext()) {
            names.add(it.next().getAsString());
        }
        return r;
    }
}
