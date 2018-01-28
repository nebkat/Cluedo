package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.card.Card;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class Game {
	private Set<Card> mCards;

	private Suggestion mAnswer;

	private List<Player> mPlayers;

    public static void main(String[] args) {
	    System.out.println("Welcome to Cluedo");

	    BoardModel.initialize();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	    JFrame frame = new JFrame();
	    frame.setTitle("Cluedo");
	    frame.setSize((int) (screenSize.getWidth() * 0.9), (int) (screenSize.getHeight() * 0.9));

	    JPanel panel = new JPanel();
	    panel.setLayout(new GridLayout(BoardModel.BOARD_HEIGHT, BoardModel.BOARD_WIDTH));
	    for (int i = 0; i < BoardModel.BOARD_WIDTH * BoardModel.BOARD_HEIGHT; i++) {
	        panel.add(new JButton(Integer.toString(i)));
        }

	    frame.add(panel);

	    frame.pack();
	    frame.setVisible(true);
    }

    public Game() {

	}
}
