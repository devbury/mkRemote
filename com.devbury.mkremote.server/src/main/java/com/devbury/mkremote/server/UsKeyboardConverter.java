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
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsKeyboardConverter implements UnicodeToKeyCodeSequenceConverter {

    private Logger logger = LoggerFactory.getLogger(UsKeyboardConverter.class);
    private HashMap<Character, int[]> map = new HashMap<Character, int[]>();

    public UsKeyboardConverter() {
        map.put('a', new int[]{KeyEvent.VK_A});
        map.put('b', new int[]{KeyEvent.VK_B});
        map.put('c', new int[]{KeyEvent.VK_C});
        map.put('d', new int[]{KeyEvent.VK_D});
        map.put('e', new int[]{KeyEvent.VK_E});
        map.put('f', new int[]{KeyEvent.VK_F});
        map.put('g', new int[]{KeyEvent.VK_G});
        map.put('h', new int[]{KeyEvent.VK_H});
        map.put('i', new int[]{KeyEvent.VK_I});
        map.put('j', new int[]{KeyEvent.VK_J});
        map.put('k', new int[]{KeyEvent.VK_K});
        map.put('l', new int[]{KeyEvent.VK_L});
        map.put('m', new int[]{KeyEvent.VK_M});
        map.put('n', new int[]{KeyEvent.VK_N});
        map.put('o', new int[]{KeyEvent.VK_O});
        map.put('p', new int[]{KeyEvent.VK_P});
        map.put('q', new int[]{KeyEvent.VK_Q});
        map.put('r', new int[]{KeyEvent.VK_R});
        map.put('s', new int[]{KeyEvent.VK_S});
        map.put('t', new int[]{KeyEvent.VK_T});
        map.put('u', new int[]{KeyEvent.VK_U});
        map.put('v', new int[]{KeyEvent.VK_V});
        map.put('w', new int[]{KeyEvent.VK_W});
        map.put('x', new int[]{KeyEvent.VK_X});
        map.put('y', new int[]{KeyEvent.VK_Y});
        map.put('z', new int[]{KeyEvent.VK_Z});

        map.put('A', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_A});
        map.put('B', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_B});
        map.put('C', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_C});
        map.put('D', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_D});
        map.put('E', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_E});
        map.put('F', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_F});
        map.put('G', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_G});
        map.put('H', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_H});
        map.put('I', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_I});
        map.put('J', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_J});
        map.put('K', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_K});
        map.put('L', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_L});
        map.put('M', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_M});
        map.put('N', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_N});
        map.put('O', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_O});
        map.put('P', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_P});
        map.put('Q', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Q});
        map.put('R', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_R});
        map.put('S', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_S});
        map.put('T', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_T});
        map.put('U', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_U});
        map.put('V', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_V});
        map.put('W', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_W});
        map.put('X', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_X});
        map.put('Y', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Y});
        map.put('Z', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Z});

        map.put('1', new int[]{KeyEvent.VK_1});
        map.put('2', new int[]{KeyEvent.VK_2});
        map.put('3', new int[]{KeyEvent.VK_3});
        map.put('4', new int[]{KeyEvent.VK_4});
        map.put('5', new int[]{KeyEvent.VK_5});
        map.put('6', new int[]{KeyEvent.VK_6});
        map.put('7', new int[]{KeyEvent.VK_7});
        map.put('8', new int[]{KeyEvent.VK_8});
        map.put('9', new int[]{KeyEvent.VK_9});
        map.put('0', new int[]{KeyEvent.VK_0});

        map.put('!', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_1});
        map.put('@', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_2});
        map.put('#', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_3});
        map.put('$', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_4});
        map.put('%', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_5});
        map.put('^', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_6});
        map.put('&', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_7});
        map.put('*', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_8});
        map.put('(', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_9});
        map.put(')', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_0});

        map.put('\t', new int[]{KeyEvent.VK_TAB});
        map.put('`', new int[]{KeyEvent.VK_BACK_QUOTE});
        map.put('_', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS});
        // TODO POUND
        // TODO EURO
        // TODO divide
        // TODO times
        // TODO sym
        map.put('-', new int[]{KeyEvent.VK_MINUS});
        map.put('+', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS});
        map.put('=', new int[]{KeyEvent.VK_EQUALS});

        map.put('|', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH});
        map.put('\\', new int[]{KeyEvent.VK_BACK_SLASH});
        map.put('{', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET});
        map.put('}', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET});
        map.put(':', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON});
        map.put(';', new int[]{KeyEvent.VK_SEMICOLON});
        map.put('"', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE});
        map.put('\'', new int[]{KeyEvent.VK_QUOTE});
        map.put('\b', new int[]{KeyEvent.VK_BACK_SPACE});

        map.put('[', new int[]{KeyEvent.VK_OPEN_BRACKET});
        map.put(']', new int[]{KeyEvent.VK_CLOSE_BRACKET});
        map.put('<', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA});
        map.put('>', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD});
        map.put(',', new int[]{KeyEvent.VK_COMMA});
        map.put('?', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH});
        map.put('\n', new int[]{KeyEvent.VK_ENTER});

        map.put('~', new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE});
        map.put(' ', new int[]{KeyEvent.VK_SPACE});
        map.put('.', new int[]{KeyEvent.VK_PERIOD});
        map.put('/', new int[]{KeyEvent.VK_SLASH});
    }

    @Override
    public int[] convert(int c) {
        int[] ret = map.get((char) c);
        if (ret == null && logger.isDebugEnabled()) {
            logger.debug("No keyCodeSequence found for {}", c);
        }
        return ret;
    }

    public HashMap<Character, int[]> getMap() {
        return map;
    }

    public void setMap(HashMap<Character, int[]> map) {
        this.map = map;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
