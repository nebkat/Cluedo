package com.wolfetones.cluedo.ui;

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
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        setBackground(Color.BLACK);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 48));
        setForeground(Color.GREEN);

        setMaximumSize(new Dimension(Short.MAX_VALUE, 48));

        addKeyListener(this);
        setFocusTraversalKeysEnabled(false);
        setEditable(true);

        mUnhintedText = getText();
    }

    public void clear() {
        setText(null);
        mUnhintedText = "";
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
        if (e.getKeyCode() == KeyEvent.VK_TAB && mCommandHints != null) {
            boolean foundCurrent = getText().equals(mUnhintedText);
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
        if (!Character.isISOControl(e.getKeyChar()) ||
                e.getKeyCode() == KeyEvent.VK_DELETE ||
                e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            mUnhintedText = getText().toLowerCase();
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            mInputStream.append(getText() + "\n");
            System.out.println("> " + getText());
            clear();
            e.consume();
        }
    }

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

        public synchronized void append(String text) {
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