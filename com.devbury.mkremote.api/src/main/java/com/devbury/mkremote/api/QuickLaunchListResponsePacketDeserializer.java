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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * For some reason there is a different class loader that is being used and it can't find nested classes. We deserialize
 * the whole class to make sure the right class loader is used. This is a hack and I would like to remove this some day
 * after I better understand the classloader problem.
 */
public class QuickLaunchListResponsePacketDeserializer implements JsonDeserializer<QuickLaunchListResponsePacket> {

    @Override
    public QuickLaunchListResponsePacket deserialize(JsonElement json, Type t, JsonDeserializationContext context)
            throws JsonParseException {
        QuickLaunchListResponsePacket p = new QuickLaunchListResponsePacket();
        ArrayList<LaunchItem> items = new ArrayList<LaunchItem>();
        p.setItems(items);
        Iterator<JsonElement> it = json.getAsJsonObject().get("items").getAsJsonArray().iterator();
        while (it.hasNext()) {
            items.add(buildLaunchItem(it.next()));
        }

        return p;
    }

    protected LaunchItem buildLaunchItem(JsonElement json) {
        LaunchItem item = new LaunchItem();
        JsonObject o = json.getAsJsonObject();
        item.setName(o.get("name").getAsString());
        item.setPath(o.get("path").getAsString());
        item.setType(o.get("type").getAsInt());
        item.setIcon(o.get("icon").getAsString());
        return item;
    }
}
