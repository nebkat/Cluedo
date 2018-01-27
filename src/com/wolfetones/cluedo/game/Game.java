package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.Board;

import javax.swing.*;
import java.awt.*;

public class Game {

    public static void main(String[] args) {
	    System.out.println("Welcome to Cluedo");

	    Board.initialize();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	    JFrame frame = new JFrame();
	    frame.setTitle("Cluedo");
	    frame.setSize((int) (screenSize.getWidth() * 0.9), (int) (screenSize.getHeight() * 0.9));

	    JPanel panel = new JPanel();
	    panel.setLayout(new GridLayout(Board.BOARD_TILE_HEIGHT, Board.BOARD_TILE_WIDTH));
	    for (int i = 0; i < Board.BOARD_TILE_WIDTH * Board.BOARD_TILE_HEIGHT; i++) {
	        panel.add(new JButton(Integer.toString(i)));
        }

	    frame.add(panel);

	    frame.pack();
	    frame.setVisible(true);
    }
}
