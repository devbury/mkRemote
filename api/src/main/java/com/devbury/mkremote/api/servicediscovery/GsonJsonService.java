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

package com.devbury.mkremote.api.servicediscovery;

import java.io.Reader;
import java.io.Writer;

import com.devbury.mkremote.api.JsonService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonJsonService implements JsonService {

    private Gson gson;

    public GsonJsonService() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    public GsonJsonService(Gson g) {
        gson = g;
    }

    @Override
    public <T> T fromJson(String s, Class<T> clazz) {
        return gson.fromJson(s, clazz);
    }

    @Override
    public <T> T fromJson(Reader r, Class<T> clazz) {
        return gson.fromJson(r, clazz);
    }

    @Override
    public String toJson(Object o) {
        return gson.toJson(o);
    }

    @Override
    public void toJson(Object o, Writer w) {
        gson.toJson(o, w);
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
