/*
 * Copyright (c) 2009-2013 devBury LLC
 *
 *   This file is part of mkRemote.
 *
 *   mkRemote is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License Version 3
 *   as published by the Free Software Foundation.
 *
 *   mkRemote is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with mkRemote.  If not, see <http://www.gnu.org/licenses/gpl.txt/>.
 */

package com.devbury.android;

import android.content.Context;
import android.content.res.Configuration;

public class Android5ConfigurationData implements AndroidConfigurationData {

    private Context context;

    public Android5ConfigurationData(Context c) {
        context = c;
    }

    @Override
    public void appendConfigurationData(StringBuffer sb) {
        Configuration config = null;
        try {
            config = context.getResources().getConfiguration();
        } catch (Throwable t) {
        }
        try {
            sb.append("navigationHidden : ").append(config.navigationHidden).append('\n');
        } catch (Throwable t) {
        }
    }
}
