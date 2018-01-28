package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.board.tiles.RoomTile;
import com.wolfetones.cluedo.board.tiles.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TileComponent extends JButton implements MouseListener {
    public static final Color COLOR_CORRIDOR_A = Color.decode("#e4c17f");
    public static final Color COLOR_CORRIDOR_B = Color.decode("#e0c070");

    public static final Color COLOR_ROOM = Color.decode("#ab9e85");
    public static final Color COLOR_EMPTY = Color.decode("#4f8967");

    private final Tile mTile;

    private Color mDefaultBackgroundColor;
    private Color mActiveBackgroundColor;

    private boolean mMouseOver = false;

    public TileComponent(Tile tile) {
        super("[" + tile.getX() + ", " + tile.getY() + "]");
        addMouseListener(this);

        mTile = tile;

        if (mTile instanceof CorridorTile || mTile instanceof RoomTile) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    public Tile getTile() {
        return mTile;
    }

    public void setBackgroundColors(Color def, Color active) {
        mDefaultBackgroundColor = def;
        mActiveBackgroundColor = active;

        setBackground(!mMouseOver ? def : active);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mMouseOver = true;

        if (mActiveBackgroundColor != null) {
            setBackground(mActiveBackgroundColor);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mMouseOver = false;

        if (mActiveBackgroundColor != null) {
            setBackground(mDefaultBackgroundColor);
        }
    }
}
