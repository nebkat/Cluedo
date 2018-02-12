package com.wolfetones.cluedo.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.OutputStream;

/**
 * Text output panel.
 */
public class OutputPanel extends JTextArea {
    private PanelOutputStream mOutputStream = new PanelOutputStream();

    public OutputPanel() {
        super();

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(20, 5, 10, 10)));

        setBackground(Color.GRAY);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        setWrapStyleWord(true);
        setLineWrap(true);

        setEditable(false);
    }

    public void clear() {
        setText(null);
    }

    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    public class PanelOutputStream extends OutputStream {
        @Override
        public void write(int b) {
            OutputPanel.this.append(String.valueOf((char) b));
            OutputPanel.this.setCaretPosition(OutputPanel.this.getDocument().getLength());
        }
    }
}