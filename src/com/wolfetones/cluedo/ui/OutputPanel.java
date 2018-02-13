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