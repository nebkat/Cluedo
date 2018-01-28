package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.PathFinder;
import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.board.tiles.RoomTile;
import com.wolfetones.cluedo.board.tiles.StartTile;
import com.wolfetones.cluedo.board.tiles.Tile;
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

		List<Tile> path = PathFinder.findShortestPath(BoardModel.TILES[0][9], BoardModel.TILES[24][16], 100);
		if (path == null) return;

	    JPanel panel = new JPanel();
	    panel.setLayout(new GridLayout(BoardModel.BOARD_HEIGHT, BoardModel.BOARD_WIDTH));
	    for (int i = 0; i < BoardModel.BOARD_HEIGHT; i++) {
	    	for (int j = 0; j < BoardModel.BOARD_WIDTH; j++) {
	    		Tile tile = BoardModel.TILES[i][j];
	    		JButton button = new JButton(i + ", " + j);

	    		if (path.contains(tile)) {
	    			button.setBackground(Color.GREEN);
				} else if (tile instanceof RoomTile) {
	    			button.setBackground(Color.GRAY);
	    			button.setBorderPainted(false);
				} else if (tile instanceof StartTile) {
	    			button.setBackground(Color.RED);
				} else if (tile instanceof CorridorTile) {
	    			button.setBackground(Color.YELLOW);
				} else if (tile == null) {
	    			button.setBackground(Color.WHITE);
					button.setBorderPainted(false);
				}

				panel.add(button);
			}
		}

	    frame.add(panel);

	    frame.pack();
	    frame.setVisible(true);
    }

    public Game() {

	}
}
