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

package com.devbury.mkremote;

import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.connections.ServerConnection;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BoxeeBarInitializer {

    private Activity activity;
    private ServerConnection serverConnection;

    public BoxeeBarInitializer(Activity a, ServerConnection sc) {
        activity = a;
        serverConnection = sc;
    }

    protected void initButton(int id, final int drawable, final DataPacket dp) {
        final View layout = activity.findViewById(R.id.boxee_dpad);
        View v = activity.findViewById(id);
        v.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    layout.setBackgroundResource(drawable);
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    layout.setBackgroundResource(R.drawable.boxeepad);
                    return false;
                }
                return false;
            }
        });
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        if (serverConnection.isConnected()) {
                            serverConnection.writeObject(dp);
                        }
                    }
                }.start();
            }
        });
    }

    public void init() {
        initButton(R.id.boxee_dpad_up, R.drawable.boxeepad_up, new DataPacket(DataPacket.BOXEE_UP));
        initButton(R.id.boxee_dpad_down, R.drawable.boxeepad_down, new DataPacket(DataPacket.BOXEE_DOWN));
        initButton(R.id.boxee_dpad_right, R.drawable.boxeepad_right, new DataPacket(DataPacket.BOXEE_RIGHT));
        initButton(R.id.boxee_dpad_left, R.drawable.boxeepad_left, new DataPacket(DataPacket.BOXEE_LEFT));
        initButton(R.id.boxee_dpad_center, R.drawable.boxeepad_center, new DataPacket(DataPacket.BOXEE_SELECT));
        initButton(R.id.boxee_dpad_back, R.drawable.boxeepad_back, new DataPacket(DataPacket.BOXEE_BACK));
        initButton(R.id.boxee_dpad_mute, R.drawable.boxeepad_mute, new DataPacket(DataPacket.BOXEE_MUTE));
        initButton(R.id.boxee_dpad_pause, R.drawable.boxeepad_pause, new DataPacket(DataPacket.BOXEE_PAUSE));
        initButton(R.id.boxee_dpad_stop, R.drawable.boxeepad_stop, new DataPacket(DataPacket.BOXEE_STOP));

        SeekBar sb = (SeekBar) activity.findViewById(R.id.boxee_seek);
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int arg1, boolean arg2) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar s) {
                if (serverConnection.isConnected()) {
                    new Thread() {
                        @Override
                        public void run() {
                            DataPacket dp = new DataPacket(DataPacket.BOXEE_SET_PLAYED_PERCENTAGE);
                            dp.setPercentage(s.getProgress());
                            serverConnection.writeObject(dp);
                        }
                    }.start();
                } else {
                    s.setProgress(0);
                }
            }
        });
    }
}
