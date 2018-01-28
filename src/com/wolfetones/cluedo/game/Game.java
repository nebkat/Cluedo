package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.PathFinder;
import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.ui.TileBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Game {
	private Set<Card> mCards;

	private Suggestion mAnswer;

	private List<Player> mPlayers;

	private static Tile mStartTile;
	private static Tile mEndTile;

    public static void main(String[] args) {
	    System.out.println("Welcome to Cluedo");

	    BoardModel.initialize();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	    JFrame frame = new JFrame();
	    frame.setTitle("Cluedo");
	    frame.setSize((int) (screenSize.getWidth() * 0.9), (int) (screenSize.getHeight() * 0.9));

		mStartTile = BoardModel.TILES[0][9];
		mEndTile = BoardModel.TILES[24][16];

		Runnable update = () -> {
			List<Tile> path = PathFinder.findShortestPath(mStartTile, mEndTile, 12);
			if (path == null) return;

			for (int i = 0; i < BoardModel.BOARD_HEIGHT; i++) {
				for (int j = 0; j < BoardModel.BOARD_WIDTH; j++) {
					Tile tile = BoardModel.TILES[i][j];
					JButton button = tile.getButton();

					if (tile instanceof CorridorTile) {
						button.setBackground(path.contains(tile) ? Color.GREEN : Color.decode(((i + j) % 2 == 0) ? "#e4c17f" : "#e0c070"));
					}
				}
			}
		};

	    JPanel panel = new JPanel();
	    panel.setLayout(new GridLayout(BoardModel.BOARD_HEIGHT, BoardModel.BOARD_WIDTH));
	    for (int i = 0; i < BoardModel.BOARD_HEIGHT; i++) {
	    	for (int j = 0; j < BoardModel.BOARD_WIDTH; j++) {
	    		Tile tile = BoardModel.TILES[i][j];
	    		JButton button = new JButton();
	    		button.setPreferredSize(new Dimension(75, 75));
	    		tile.setButton(button);

	    		char[] bc = BoardModel.getTileBordersAndCorners(j, i);

	    		System.out.println("row: " + i + ", col: " + j + " " + Arrays.toString(bc));

	    		button.setBorder(new TileBorder(bc));

	    		button.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						mStartTile = tile;
						update.run();
					}

					@Override
					public void mouseEntered(MouseEvent e) {
						mEndTile = tile;
						update.run();
					}
				});

				if (tile instanceof RoomTile) {
					button.setBackground(Color.decode("#ab9e85"));
				} else if (tile instanceof StartTile) {
					button.setBackground(Color.RED);
				} else if (tile instanceof CorridorTile) {
					button.setBackground(Color.decode(((i + j) % 2 == 0) ? "#e4c17f" : "#e0c070"));
				} else if (tile instanceof EmptyTile) {
	    			button.setEnabled(false);
					button.setBackground(Color.decode("#4f8967"));
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
