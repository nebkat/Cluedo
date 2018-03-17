/*
 * Copyright (c) 2018
 *
 * The Wolfe Tones
 * -------------------
 * Nebojsa Cvetkovic - 16376551
 * Hugh Ormond - 16312941
 *
 * This file is a part of Cluedo
 *
 * Cluedo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cluedo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cluedo.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.config.Config;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.util.List;

/**
 * User text input panel.
 */
public class InputPanel extends JTextField implements KeyListener {
    private PanelInputStream mInputStream = new PanelInputStream();

    private String mUnhintedText;
    private List<String> mCommandHints;

    public InputPanel() {
        super();

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(Config.screenRelativeSize(10),
                        Config.screenRelativeSize(10),
                        Config.screenRelativeSize(10),
                        Config.screenRelativeSize(10))));

        setBackground(Color.BLACK);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, Config.screenRelativeSize(24)));
        setForeground(Color.GREEN);
        setCaretColor(Color.GREEN);

        setMaximumSize(new Dimension(Short.MAX_VALUE, Config.screenRelativeSize(24)));

        addKeyListener(this);
        setFocusTraversalKeysEnabled(false);
        setEditable(true);

        mUnhintedText = getText();
    }

    public void clear() {
        setText(null);
        mUnhintedText = "";
    }

    public void append(String text) {
        System.out.println("> " + text);
        inject(text);
    }

    public void inject(String text) {
        mInputStream.append(text + "\n");
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public void setCommandHints(List<String> commands) {
        mCommandHints = commands;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Command hints
        if (e.getKeyCode() == KeyEvent.VK_TAB && mCommandHints != null) {
            // Whether a command equal to the currently typed text has been found
            // Initially set to true if the current text is equal to the unhinted text, i.e. not currently hinted
            boolean foundCurrent = getText().toLowerCase().equals(mUnhintedText);

            // Whether a valid next command to hint has been found
            // If not next command is found, the text is set to the unhinted text
            boolean foundNext = false;
            for (String command : mCommandHints) {
                if (!foundCurrent) {
                    if (getText().equalsIgnoreCase(command)) {
                        foundCurrent = true;
                    }
                } else {
                    if (command.toLowerCase().startsWith(mUnhintedText)) {
                        setText(command);
                        foundNext = true;
                        break;
                    }
                }
            }
            if (!foundNext) {
                setText(mUnhintedText);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Update unhinted text if modified by user (excluding arrow keys, etc)
        if (!Character.isISOControl(e.getKeyChar()) ||
                e.getKeyCode() == KeyEvent.VK_DELETE ||
                e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            mUnhintedText = getText().toLowerCase();
        }

        // Submit command on enter
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            mInputStream.append(getText() + "\n");
            System.out.println("> " + getText());

            clear();

            // Don't create newline
            e.consume();
        }
    }

    /**
     * Buffered input stream that allows text to be appended
     */
    private class PanelInputStream extends InputStream {
        private static final int BUFFER_SIZE = 128;

        private byte[] mBuffer = new byte[BUFFER_SIZE];
        private int mHead = 0;
        private int mTail = 0;
        private int mCount = 0;

        @Override
        public synchronized int read() {
            while (mCount <= 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return -1;
                }
            }

            byte r = mBuffer[mHead];
            mHead++;
            mHead %= BUFFER_SIZE;

            mCount--;

            return r & 0xff;
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            while (mCount <= 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return -1;
                }
            }

            int i;
            for (i = 0; i < len && mCount > 0; i++) {
                b[off + i] = mBuffer[mHead];
                mHead++;
                mHead %= BUFFER_SIZE;

                mCount--;
            }

            return i;
        }

        @Override
        public int available() {
            return mCount;
        }

        private synchronized void append(String text) {
            char[] chars = text.toCharArray();

            // Check if there is enough space in the buffer
            if (mCount + chars.length > BUFFER_SIZE) {
                throw new BufferOverflowException();
            }

            // Copy characters to buffer
            for (char c : chars) {
                mBuffer[mTail] = (byte) c;
                mTail++;
                mTail %= BUFFER_SIZE;
            }

            mCount += chars.length;

            notifyAll();
        }
    }
}