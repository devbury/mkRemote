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

package com.devbury.mkremote.activity;

import java.util.Iterator;

import com.devbury.mkremote.BoxeeBarInitializer;
import com.devbury.mkremote.BoxeePercentageThread;
import com.devbury.mkremote.ButtonBackgroundToggler;
import com.devbury.mkremote.ButtonBarStatus;
import com.devbury.mkremote.CustomKeyProcessor;
import com.devbury.mkremote.KeyEventHandler;
import com.devbury.mkremote.KeyProcessor;
import com.devbury.mkremote.LockSlider;
import com.devbury.mkremote.MkRemotePreferences;
import com.devbury.mkremote.MultiplierDpadMoveHandler;
import com.devbury.mkremote.OnKeyboardActionListenerAdapter;
import com.devbury.mkremote.R;
import com.devbury.mkremote.SimpleTouchHandler;
import com.devbury.mkremote.SpecialKeyEventHandler;
import com.devbury.mkremote.StandardKeyPressEventHandler;
import com.devbury.mkremote.TrackBallClickHandler;
import com.devbury.mkremote.VibrateClickFeedbackService;
import com.devbury.mkremote.VirtualKeyEvent;
import com.devbury.mkremote.VirtualKeyboardManager;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.VirtualKeyCode;
import com.devbury.mkremote.connections.ServerConnectionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.method.DigitsKeyListener;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;


public class Main extends ServerConnectionActivity implements OnSharedPreferenceChangeListener, VirtualKeyboardManager {

    public static final int NAG_DIALOG = 201;
    public static final int SAVE_MACRO = 203;
    public static final int DELAY = 204;
    public static final int ERROR_EMAIL_DIALOG = 205;
    public static final String FULL_VERSION_URI = "market://search?q=pname:com.devbury.mkremote";
    public static final int MAIN_VIEW = 0;
    public static final int KEYBOARD_VIEW = 1;
    public static final int BLANK_VIEW = 2;
    private TrackBallClickHandler trackBallClickHandler;
    private SimpleTouchHandler simpleTouchHandler;
    private MultiplierDpadMoveHandler multiplierDpadMoveHandler;
    private KeyEventHandler keyPressEventHandler;
    private KeyProcessor keyProcessor;
    private VibrateClickFeedbackService vcfs;
    private NagThread nagThread = null;
    private int currentView = MAIN_VIEW;
    private boolean showHideCalled = false;
    private ViewAnimator baseView;
    private BoxeePercentageThread boxeePercentageThread;

    protected void configureDependencies() {
        super.configureDependencies();
        uiHandler = new Handler();
        vcfs = new VibrateClickFeedbackService();
        vcfs.setVibrator((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
        vcfs.setSharedPreferences(sharedPreferences);
        CustomKeyProcessor kp = new CustomKeyProcessor();
        kp.setContext(this);
        kp.setServerConnection(this);
        kp.setClickFeedbackService(vcfs);
        kp.setVirtualKeyboardManager(this);
        kp.setSharedPreferences(sharedPreferences);
        kp.setApp(app);
        keyProcessor = kp;

        StandardKeyPressEventHandler skph = new StandardKeyPressEventHandler();
        skph.setServerConnection(this);

        SpecialKeyEventHandler special = new SpecialKeyEventHandler();
        special.setHandlerDelegate(skph);
        special.setSharedPreferences(sharedPreferences);
        special.setKeyProcessor(keyProcessor);
        keyPressEventHandler = special;

        simpleTouchHandler = new SimpleTouchHandler(sharedPreferences);
        simpleTouchHandler.setServerConnection(this);
        simpleTouchHandler.setKeyProcessor(keyProcessor);
        simpleTouchHandler.setMultiTouchHandler(app.multiTouchHandler);
        simpleTouchHandler.setWindowManager(getWindowManager());

        trackBallClickHandler = new TrackBallClickHandler(sharedPreferences);
        trackBallClickHandler.setKeyProcessor(keyProcessor);
        trackBallClickHandler.setServerConnection(this);

        multiplierDpadMoveHandler = new MultiplierDpadMoveHandler(sharedPreferences);
        multiplierDpadMoveHandler.setServerConnection(this);
    }

    @Override
    public void show() {
        if (currentView != KEYBOARD_VIEW) {
            currentView = KEYBOARD_VIEW;
            Configuration c = getResources().getConfiguration();
            if (c.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                configureScreenView();
            } else {
                showHideCalled = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    @Override
    public void hide() {
        if (currentView != MAIN_VIEW) {
            baseView.setDisplayedChild(BLANK_VIEW);
            currentView = MAIN_VIEW;
            Configuration c = getResources().getConfiguration();
            if (c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
                    || app.getDefaultOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                configureScreenView();
            } else {
                showHideCalled = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!showHideCalled) {
            if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
                    && newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES
                    && newConfig.orientation != app.getDefaultOrientation() && currentView != KEYBOARD_VIEW) {
                setRequestedOrientation(app.getDefaultScreenOrientation());
            } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES
                    && newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE && currentView == KEYBOARD_VIEW) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                configureScreenView();
            }
        } else {
            showHideCalled = false;
            configureScreenView();
        }
    }

    protected void configureScreenView() {
        if (currentView == MAIN_VIEW) {
            onSharedPreferenceChanged(sharedPreferences, MkRemotePreferences.cs(R.string.c_notification_bar_on));
            onSharedPreferenceChanged(sharedPreferences, MkRemotePreferences.cs(R.string.c_wallpaper_on));

            configureAllButtonBars();
            configureMacroBar();
            updateConnectedToInfo();
            updateMacroBar();
            configureBoxeeBar();

            KeyboardView keyboardView = (KeyboardView) findViewById(R.id.mini_keyboard);
            Keyboard lowercase = new Keyboard(this, R.xml.mini_lowercase);
            keyboardView.setKeyboard(lowercase);
            MiniOnKeyboardActionListener ml = new MiniOnKeyboardActionListener();
            ml.setKeyboardView(keyboardView);
            ml.setLowercaseKeyboard(lowercase);
            ml.setUppercaseKeyboard(new Keyboard(this, R.xml.mini_uppercase));
            ml.setSymbolKeyboard(new Keyboard(this, R.xml.mini_symbols));
            ml.setAltSymbolKeyboard(new Keyboard(this, R.xml.mini_alt_symbols));
            keyboardView.setOnKeyboardActionListener(ml);
            baseView.setDisplayedChild(MAIN_VIEW);
        } else {
            // turn off notification bar while in keyboard view
            app.windowAttributeHelper.processNotificationBar(getWindow(), false);
            int keyboard_resource = R.xml.pc;
            int keyboard_shift_resource = R.xml.pcshift;
            String os = app.serviceInfo.getAttributes().getProperty("os.name");
            if (os != null) {
                os = os.toLowerCase();
                if (os.contains("mac")) {
                    keyboard_resource = R.xml.mac;
                    keyboard_shift_resource = R.xml.macshift;
                } else if (os.contains("linux")) {
                    keyboard_resource = R.xml.linux;
                    keyboard_shift_resource = R.xml.linuxshift;
                }
            }
            CustomOnKeyboardActionListener kl = new CustomOnKeyboardActionListener();
            KeyboardView keyboardView = (KeyboardView) findViewById(R.id.keyboard);
            Keyboard k = new Keyboard(this, keyboard_resource);
            keyboardView.setKeyboard(k);
            keyboardView.setOnKeyboardActionListener(kl);
            kl.setKeyboardView(keyboardView);
            kl.setToggleKeyboard(new Keyboard(this, keyboard_shift_resource));

            baseView.setDisplayedChild(KEYBOARD_VIEW);
        }
    }

    protected ViewAnimator buildView() {
        LayoutInflater li = getLayoutInflater();
        ViewAnimator va = (ViewAnimator) li.inflate(R.layout.base, null);
        View v = li.inflate(R.layout.main, null);
        if (app.isLite()) {
            ((ImageView) v.findViewById(R.id.logo)).setImageResource(R.drawable.logo_lite);
        }
        View touch = v.findViewById(R.id.touch_screen);
        touch.setOnTouchListener(simpleTouchHandler);
        LockSlider ls = (LockSlider) v.findViewById(R.id.drawer);
        if (sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_lock_on), false)) {
            ls.lock();
        } else {
            ls.unlock();
        }
        va.addView(v, MAIN_VIEW);
        va.addView(li.inflate(R.layout.keyboard, null), KEYBOARD_VIEW);
        va.addView(new View(this), BLANK_VIEW);
        va.setDisplayedChild(currentView);
        return va;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseView = buildView();
        setContentView(baseView);
        if (app.hasError()) {
            freshShowDialog(ERROR_EMAIL_DIALOG);
        } else if (app.isLite()) {
            freshShowDialog(NAG_DIALOG);
            nagThread = new NagThread();
            nagThread.start();
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        initBoxeeBar();
    }

    @Override
    protected void onPause() {
        stopBoxeeThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getResources().getConfiguration().orientation != app.getDefaultOrientation()) {
            setRequestedOrientation(app.getDefaultScreenOrientation());
        } else {
            configureScreenView();
        }
        updateMacroBar();
    }

    @Override
    protected void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        if (nagThread != null) {
            nagThread.stopped = true;
            nagThread.interrupt();
        }
        super.onDestroy();
    }

    protected void updateConnectedToInfo() {
        TextView server_info = (TextView) findViewById(R.id.server_info);
        if (isConnected()) {
            server_info.setText(app.serviceInfo.getId());
        } else {
            server_info.setText(getString(R.string.not_connected));
        }
    }

    @Override
    protected void connectToServer() {
        super.connectToServer();
        updateConnectedToInfo();
    }

    @Override
    public void serverConnectionErrorHandler() {
        hide();
        TextView server_info = (TextView) findViewById(R.id.server_info);
        server_info.setText(getString(R.string.not_connected));
        super.serverConnectionErrorHandler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi = menu.findItem(R.id.quick_launch);
        MenuItem start_macro = menu.findItem(R.id.macro_manager);
        MenuItem show_keyboard = menu.findItem(R.id.show_keyboard);
        if (isConnected()) {
            mi.setEnabled(true);
            if (currentView != KEYBOARD_VIEW) {
                show_keyboard.setEnabled(true);
            } else {
                show_keyboard.setEnabled(false);
            }
            if (isMacroMode()) {
                start_macro.setEnabled(false);
            } else {
                start_macro.setEnabled(true);
            }
        } else {
            mi.setEnabled(false);
            start_macro.setEnabled(false);
            show_keyboard.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_server:
                Intent server_select = new Intent(this, MkRemoteServerSelection.class);
                startActivity(server_select);
                break;
            case R.id.settings:
                Intent settings = new Intent(this, CustomPreferences.class);
                startActivity(settings);
                break;
            case R.id.quick_launch:
                Intent quick_launch = new Intent(this, QuickLaunchActivity.class);
                startActivity(quick_launch);
                break;
            case R.id.about:
                Intent help = new Intent(this, Help.class);
                startActivity(help);
                break;
            case R.id.macro_manager:
                Intent macro_manager = new Intent(this, MacroManager.class);
                startActivity(macro_manager);
                break;
            case R.id.show_keyboard:
                show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = super.onCreateDialog(id);
        if (id == ERROR_EMAIL_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.email_error));
            builder.setMessage(getString(R.string.email_error_message));
            final Context context = this;
            builder.setPositiveButton(R.string.send_email, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    app.sendErrorEmail(context);
                    if (app.isLite()) {
                        freshShowDialog(NAG_DIALOG);
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.maybe_later), new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    app.clearError();
                    if (app.isLite()) {
                        freshShowDialog(NAG_DIALOG);
                    }
                }
            });
            d = builder.create();
        } else if (id == NAG_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.full_version));
            builder.setMessage(getString(R.string.full_version_message));
            final Context context = this;
            builder.setPositiveButton(R.string.download, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(FULL_VERSION_URI));
                    dialog.dismiss();
                    context.startActivity(i);
                }
            });
            builder.setNegativeButton(getString(R.string.maybe_later), new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            d = builder.create();
        } else if (id == SAVE_MACRO) {
            final EditText macro_name = new EditText(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.save_macro);
            builder.setView(macro_name);
            builder.setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DataPacket dp = new DataPacket(DataPacket.MACRO_SAVE);
                    dp.setS(macro_name.getText().toString());
                    writeObject(dp);
                    setMacroMode(false);
                    updateMacroBar();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.abort, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DataPacket dp = new DataPacket(DataPacket.MACRO_ABORT);
                    writeObject(dp);
                    setMacroMode(false);
                    updateMacroBar();
                    dialog.dismiss();
                }
            });
            d = builder.create();
        } else if (id == DELAY) {
            final EditText delay = new EditText(this);
            delay.setKeyListener(new DigitsKeyListener());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delay_title);
            builder.setView(delay);
            builder.setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DataPacket dp = new DataPacket(DataPacket.MACRO_DELAY_RECORD);
                    dp.setX(Integer.valueOf(delay.getText().toString()));
                    writeObject(dp);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            d = builder.create();
        }
        return d;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (multiplierDpadMoveHandler.move(event.getKeyCode())) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isConnected()) {
            try {
                if (keyPressEventHandler.processEvent(keyCode, event)) {
                    return true;
                }
            } catch (ServerConnectionException e) {
                serverConnectionErrorHandler();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isConnected() && keyCode != KeyEvent.KEYCODE_MENU) {
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (ServerConnectionException e) {
            serverConnectionErrorHandler();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isConnected()) {
            try {
                return trackBallClickHandler.processEvent(ev);
            } catch (ServerConnectionException e) {
                serverConnectionErrorHandler();
            }
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (MkRemotePreferences.cs(R.string.c_boxee_mode_on).equals(key)) {
            configureBoxeeBar();
            configureAllButtonBars();
        } else if (MkRemotePreferences.cs(R.string.c_wallpaper_on).equals(key)) {
            app.windowAttributeHelper.processWallpaper(getWindow(), sharedPreferences.getBoolean(key, false));
        } else if (MkRemotePreferences.cs(R.string.c_notification_bar_on).equals(key)) {
            app.windowAttributeHelper.processNotificationBar(getWindow(), sharedPreferences.getBoolean(key, false));
        } else {
            // this must be last we are going to assume it is a button bar
            // setting
            configureButtonBar(key);
        }
    }

    protected void configureMacroBar() {
        Button stop = (Button) findViewById(R.id.macrobar_end_macro);
        stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                freshShowDialog(SAVE_MACRO);
            }
        });
        stop.setOnTouchListener(new ButtonBackgroundToggler());
        Button delay = (Button) findViewById(R.id.macrobar_delay);
        delay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                freshShowDialog(DELAY);
            }
        });
        delay.setOnTouchListener(new ButtonBackgroundToggler());
    }

    protected void updateMacroBar() {
        View macro_bar = findViewById(R.id.macrobar);
        if (macro_bar != null) {
            if (isMacroMode()) {
                macro_bar.setVisibility(View.VISIBLE);
            } else if (macro_bar != null) {
                macro_bar.setVisibility(View.GONE);
            }
        }
        configureBoxeeBar();
    }

    protected void configureButtonBar(String key) {
        ButtonBarStatus status = MkRemotePreferences.bbv.get(key);
        if (status != null) {
            View bb = findViewById(status.getViewId());
            if (bb != null) {
                if (bb instanceof ImageButton) {
                    final Main main = this;
                    bb.setOnTouchListener(new ButtonBackgroundToggler());
                    bb.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (main.isConnected()) {
                                if (v.getTag() instanceof String) {
                                    String type = (String) v.getTag();
                                    main.keyProcessor.processKey(type);
                                }
                            }
                        }
                    });
                }

                if (sharedPreferences.getBoolean(key, status.isDefaultVisibility())) {
                    boolean boxee_mode = sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_boxee_mode_on),
                            false);
                    if (!boxee_mode) {
                        bb.setVisibility(View.VISIBLE);
                    } else {
                        bb.setVisibility(View.GONE);
                    }
                } else {
                    bb.setVisibility(View.GONE);
                }
            }
        }
    }

    protected void initButtonBarDefaults(String key) {
        if (!sharedPreferences.contains(key)) {
            ButtonBarStatus status = MkRemotePreferences.bbv.get(key);
            boolean active = sharedPreferences.getBoolean(key, status.isDefaultVisibility());
            sharedPreferences.edit().putBoolean(key, active).commit();
        }
    }

    protected void configureAllButtonBars() {
        for (String key : MkRemotePreferences.bbv.keySet()) {
            initButtonBarDefaults(key);
            configureButtonBar(key);
        }
    }

    protected void stopBoxeeThread() {
        if (boxeePercentageThread != null) {
            boxeePercentageThread.stopped = true;
            boxeePercentageThread.interrupt();
        }
    }

    protected void startBoxeeThread() {
        stopBoxeeThread();
        boxeePercentageThread = new BoxeePercentageThread(this, (SeekBar) findViewById(R.id.boxee_seek), uiHandler);
        boxeePercentageThread.start();
    }

    protected void configureBoxeeBar() {
        View v = findViewById(R.id.boxee_remote);
        View touch = findViewById(R.id.touch_screen);
        if (sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_boxee_mode_on), false)) {
            startBoxeeThread();
            v.setVisibility(View.VISIBLE);
            touch.setVisibility(View.GONE);
        } else {
            stopBoxeeThread();
            touch.setVisibility(View.VISIBLE);
            v.setVisibility(View.GONE);
        }
        Display d = getWindowManager().getDefaultDisplay();
        int dim = d.getHeight();
        if (d.getWidth() < dim) {
            dim = d.getWidth();
        }

        int used_space = 10;
        if (sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_notification_bar_on), false)) {
            used_space += 10;
        }

        v = findViewById(R.id.statusbar);
        int sb = v.getHeight();
        if (sb == 0) { // sometimes the status bar says it is 0.  As of 1.14 it is 34
            sb = 34;
        }
        used_space += sb;

        if (isMacroMode()) {
            sb = findViewById(R.id.macrobar).getHeight();
            if (sb == 0) {
                sb = 68;
            }
            used_space += sb;
        }
        if (d.getHeight() < (dim + used_space)) {
            dim = dim - used_space;
        }

        v = findViewById(R.id.boxee_dpad);
        LayoutParams lp = v.getLayoutParams();
        lp.height = dim;
        lp.width = dim;

        int direction_width = Math.round((float) (0.415625 * dim));
        int direction_height = Math.round((float) (0.290635 * dim));

        v = findViewById(R.id.boxee_dpad_up);
        lp = v.getLayoutParams();
        lp.height = direction_height;
        lp.width = direction_width;

        v = findViewById(R.id.boxee_dpad_down);
        lp = v.getLayoutParams();
        lp.height = direction_height;
        lp.width = direction_width;

        v = findViewById(R.id.boxee_dpad_right);
        lp = v.getLayoutParams();
        lp.width = direction_height;
        lp.height = direction_width;

        v = findViewById(R.id.boxee_dpad_left);
        lp = v.getLayoutParams();
        lp.width = direction_height;
        lp.height = direction_width;

        v = findViewById(R.id.boxee_seek);
        RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams) v.getLayoutParams();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rp.addRule(RelativeLayout.LEFT_OF, R.id.boxee_dpad);
            rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        } else {
            rp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rp.addRule(RelativeLayout.LEFT_OF, 0);
        }
    }

    protected void initBoxeeBar() {
        new BoxeeBarInitializer(this, this).init();
    }

    public class NagThread extends Thread {
        public boolean stopped = false;
        private Handler handler;

        public NagThread() {
            handler = new Handler();
        }

        @Override
        public void run() {
            while (!stopped) {
                try {
                    Thread.sleep(1000 * 60 * 5);
                } catch (InterruptedException e) {
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            freshShowDialog(NAG_DIALOG);
                        } catch (Throwable t) {
                            // who cares what the reason was. We will try again in 5
                            // minutes.
                        }
                    }
                });
            }
        }
    }

    protected class MiniOnKeyboardActionListener extends OnKeyboardActionListenerAdapter {
        private KeyboardView keyboardView;
        private Keyboard lowercaseKeyboard;
        private Keyboard uppercaseKeyboard;
        private Keyboard symbolKeyboard;
        private Keyboard altSymbolKeyboard;
        private boolean shift = false;
        private boolean shiftLock = false;
        private boolean alt = false;
        private boolean altLock = false;

        private void syncAltKeys() {
            Iterator<Key> i = symbolKeyboard.getKeys().iterator();
            boolean found = false;
            while (!found && i.hasNext()) {
                Key key = i.next();
                if (key.sticky) {
                    key.on = altLock;
                    found = true;
                }
            }

            i = altSymbolKeyboard.getKeys().iterator();
            found = false;
            while (!found && i.hasNext()) {
                Key key = i.next();
                if (key.sticky) {
                    key.on = altLock;
                    found = true;
                }
            }
        }

        private void syncShiftKeys() {
            Iterator<Key> i = lowercaseKeyboard.getKeys().iterator();
            boolean found = false;
            while (!found && i.hasNext()) {
                Key key = i.next();
                if (key.sticky) {
                    key.on = shiftLock;
                    found = true;
                }
            }

            i = uppercaseKeyboard.getKeys().iterator();
            found = false;
            while (!found && i.hasNext()) {
                Key key = i.next();
                if (key.sticky) {
                    key.on = shiftLock;
                    found = true;
                }
            }
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            switch (primaryCode) {
                case -98:
                    if (!alt) {
                        alt = true;
                    } else if (!altLock) {
                        altLock = true;
                    } else {
                        alt = altLock = false;
                    }
                    syncAltKeys();
                case -99:
                    // switch to symbol keyboard
                    if (alt || altLock) {
                        keyboardView.setKeyboard(altSymbolKeyboard);
                    } else {
                        keyboardView.setKeyboard(symbolKeyboard);
                    }
                    break;
                case -96:
                    if (!shift) {
                        shift = true;
                    } else if (!shiftLock) {
                        shiftLock = true;
                    } else {
                        shift = shiftLock = false;
                    }
                    syncShiftKeys();
                case -97:
                    // switch to letter symbol keyboard
                    if (shift || shiftLock) {
                        keyboardView.setKeyboard(uppercaseKeyboard);
                    } else {
                        keyboardView.setKeyboard(lowercaseKeyboard);
                    }
                    break;
                default:
                    VirtualKeyEvent ke = new VirtualKeyEvent(KeyEvent.ACTION_DOWN, primaryCode);
                    ke.setVirtualAltPressed(false);
                    ke.setVirtualCtrlPressed(false);
                    ke.setVirtualWinPressed(false);
                    ke.setVirtualCommandPressed(false);
                    onKeyDown(primaryCode, ke);
                    if (!altLock) {
                        alt = false;
                        if (keyboardView.getKeyboard() == altSymbolKeyboard) {
                            keyboardView.setKeyboard(symbolKeyboard);
                        }
                    }
                    if (!shiftLock) {
                        shift = false;
                        if (keyboardView.getKeyboard() == uppercaseKeyboard) {
                            keyboardView.setKeyboard(lowercaseKeyboard);
                        }
                    }
            }
        }

        public KeyboardView getKeyboardView() {
            return keyboardView;
        }

        public void setKeyboardView(KeyboardView keyboardView) {
            this.keyboardView = keyboardView;
        }

        public Keyboard getLowercaseKeyboard() {
            return lowercaseKeyboard;
        }

        public void setLowercaseKeyboard(Keyboard lowercaseKeyboard) {
            this.lowercaseKeyboard = lowercaseKeyboard;
        }

        public Keyboard getUppercaseKeyboard() {
            return uppercaseKeyboard;
        }

        public void setUppercaseKeyboard(Keyboard uppercaseKeyboard) {
            this.uppercaseKeyboard = uppercaseKeyboard;
        }

        public Keyboard getSymbolKeyboard() {
            return symbolKeyboard;
        }

        public void setSymbolKeyboard(Keyboard symbolKeyboard) {
            this.symbolKeyboard = symbolKeyboard;
        }

        public Keyboard getAltSymbolKeyboard() {
            return altSymbolKeyboard;
        }

        public void setAltSymbolKeyboard(Keyboard altSymbolKeyboard) {
            this.altSymbolKeyboard = altSymbolKeyboard;
        }
    }

    ;

    protected class CustomOnKeyboardActionListener extends OnKeyboardActionListenerAdapter {

        private KeyboardView keyboardView;
        private boolean shiftState = false;
        private boolean altState = false;
        private boolean capsState = false;
        private boolean winState = false;
        private boolean ctrlState = false;
        private boolean commandState = false;
        private Keyboard toggleKeyboard;

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            switch (primaryCode) {
                case VirtualKeyCode.CLOSE:
                    // close down the keyboard
                    hide();
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    shiftState = !shiftState;
                    if (!capsState) {
                        toggleKeyboard();
                    }
                    break;
                case VirtualKeyCode.ALT:
                    altState = !altState;
                    break;
                case VirtualKeyCode.CAPS:
                    capsState = !capsState;
                    if (!shiftState) {
                        toggleKeyboard();
                    }
                    break;
                case VirtualKeyCode.CTRL:
                    ctrlState = !ctrlState;
                    break;
                case VirtualKeyCode.WIN:
                    winState = !winState;
                    break;
                case VirtualKeyCode.MAC_COMMAND:
                    commandState = !commandState;
                    break;
                default:
                    VirtualKeyEvent ke = new VirtualKeyEvent(KeyEvent.ACTION_DOWN, primaryCode);
                    ke.setVirtualAltPressed(altState);
                    ke.setVirtualCtrlPressed(ctrlState);
                    ke.setVirtualWinPressed(winState);
                    ke.setVirtualCommandPressed(commandState);
                    ctrlState = false;
                    altState = false;
                    winState = false;
                    commandState = false;
                    boolean old_shift_state = shiftState;
                    shiftState = false;
                    syncStateKeys(keyboardView.getKeyboard());
                    if (old_shift_state && !capsState) {
                        toggleKeyboard();
                    } else {
                        keyboardView.setKeyboard(keyboardView.getKeyboard());
                    }
                    onKeyDown(primaryCode, ke);
            }
        }

        private void toggleKeyboard() {
            Keyboard hold = keyboardView.getKeyboard();
            syncStateKeys(toggleKeyboard);
            keyboardView.setKeyboard(toggleKeyboard);
            toggleKeyboard = hold;
        }

        private void syncStateKeys(Keyboard k) {
            for (Keyboard.Key key : k.getKeys()) {
                if (key.sticky) {
                    switch (key.codes[0]) {
                        case VirtualKeyCode.ALT:
                            key.on = altState;
                            break;
                        case VirtualKeyCode.CAPS:
                            key.on = capsState;
                            break;
                        case VirtualKeyCode.CTRL:
                            key.on = ctrlState;
                            break;
                        case VirtualKeyCode.WIN:
                            key.on = winState;
                            break;
                        case VirtualKeyCode.MAC_COMMAND:
                            key.on = commandState;
                            break;
                    }
                }
            }
        }

        public KeyboardView getKeyboardView() {
            return keyboardView;
        }

        public void setKeyboardView(KeyboardView keyboardView) {
            this.keyboardView = keyboardView;
        }

        public Keyboard getToggleKeyboard() {
            return toggleKeyboard;
        }

        public void setToggleKeyboard(Keyboard toggleKeyboard) {
            this.toggleKeyboard = toggleKeyboard;
        }
    }
}
