package com.wolfetones.cluedo.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.nio.BufferOverflowException;

/**
 * User text input panel.
 */
public class InputPanel extends JTextField implements KeyListener {
    private PanelInputStream mInputStream = new PanelInputStream();

    public InputPanel () {
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        setBackground(Color.BLACK);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 48));
        setForeground(Color.GREEN);

        setMaximumSize(new Dimension(Short.MAX_VALUE, 48));

        addKeyListener(this);
        setEditable(true);
    }

    public void clear() {
        setText(null);
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
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