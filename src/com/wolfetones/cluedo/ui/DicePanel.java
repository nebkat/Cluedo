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

import com.wolfetones.cluedo.Util;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Game;
import com.wolfetones.physics.body.Dice;
import com.wolfetones.physics.Particle;
import com.wolfetones.physics.RenderUtils;
import com.wolfetones.physics.VerletPhysics;
import com.wolfetones.physics.behavior.GravityBehavior;
import com.wolfetones.physics.constraint.PlaneConstraint;
import com.wolfetones.physics.geometry.Plane;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class DicePanel extends JPanel {
    private static final int NUM_DICE = Game.NUM_DICE;

    private static final double GRAVITY = (double) Config.screenRelativeSize(10) / 20;
    private static final int FLOOR_DEPTH = -Config.screenRelativeSize(1000);
    private static final int CUBE_SIZE = Config.screenRelativeSize(75);

    private final Object mDiceMovingLock = new Object();

    private VerletPhysics mPhysics = new VerletPhysics(5);

    private Dice[] mDices = new Dice[NUM_DICE];

    private Timer mTimer;

    public DicePanel() {
        super();

        setOpaque(false);
        setVisible(false);
    }

    public void initPhysics() {
        // Gravity
        mPhysics.addBehavior(new GravityBehavior(GRAVITY));

        // Floor
        mPhysics.addConstraint(new PlaneConstraint(
                new Plane(
                        new Point3d(0, 0, FLOOR_DEPTH),
                        new Vector3d(0, 0, 1)), 0.9, 0.5));

        // Ceiling
        mPhysics.addConstraint(new PlaneConstraint(
                new Plane(
                        new Point3d(0, 0, RenderUtils.CAMERA_HEIGHT_FROM_SCREEN),
                        new Vector3d(0, 0, -1)), 1, 1));

        // Walls
        mPhysics.addConstraint(new PlaneConstraint(RenderUtils.getPlaneFromCameraToPoint(new Point2d(getWidth() / 2, 0)), 1.0, 1.0));
        mPhysics.addConstraint(new PlaneConstraint(RenderUtils.getPlaneFromCameraToPoint(new Point2d(-getWidth() / 2, 0)), 1.0, 1.0));
        mPhysics.addConstraint(new PlaneConstraint(RenderUtils.getPlaneFromCameraToPoint(new Point2d(0, getHeight() / 2)), 1.0, 1.0));
        mPhysics.addConstraint(new PlaneConstraint(RenderUtils.getPlaneFromCameraToPoint(new Point2d(0, -getHeight() / 2)), 1.0, 1.0));

        // Dice
        for (int i = 0; i < NUM_DICE; i++) {
            mDices[i] = new Dice(new Point3d(0, 0, 0), CUBE_SIZE);
            mPhysics.addBody(mDices[i]);
        }
    }

    private Matrix3d randomRotationMatrix() {
        Matrix3d transform = new Matrix3d();
        transform.setIdentity();

        Matrix3d rotate = new Matrix3d();
        rotate.rotX(Math.random() * 2 * Math.PI);
        transform.mul(rotate);
        rotate.rotY(Math.random() * 2 * Math.PI);
        transform.mul(rotate);
        rotate.rotZ(Math.random() * 2 * Math.PI);
        transform.mul(rotate);

        return transform;
    }

    public int rollDice(int[] diceValues) {
        // Show panel
        setVisible(true);

        // Translation of previous dice
        Vector3d previousTranslate = null;
        for (Dice dice : mDices) {
            // Reset dice position
            dice.reset();

            // Rotate by random amount without velocity
            dice.transform(randomRotationMatrix(), true);

            // Rotate by random amount with velocity
            dice.transform(randomRotationMatrix());

            // Apply force in random direction, ensuring at least 10 degrees of separation between two dice, or sufficient magnitude difference
            Vector3d translate;
            do {
                translate = new Vector3d((Math.random() - 0.5) * Config.screenRelativeSize(15),
                        (Math.random() - 0.5) * Config.screenRelativeSize(15), 0);
            } while (previousTranslate != null &&
                    previousTranslate.angle(translate) < (5.0 / 180.0 * Math.PI) &&
                    Math.abs(translate.length() - previousTranslate.length()) < Config.screenRelativeSize(10));

            // Store translation for angle calculations
            previousTranslate = translate;

            // Apply force
            dice.translate(translate);
        }

        // Physics update task
        TimerTask physicsTick = new TimerTask() {
            @Override
            public void run() {
                mPhysics.update();
                repaint();
            }
        };

        // Check for dice movement task
        TimerTask checkDiceTick = new TimerTask() {
            private double[] previousAverageVelocity = new double[NUM_DICE];
            private Point3d[] previousCenters = new Point3d[NUM_DICE];

            @Override
            public void run() {
                boolean moved = false;
                for (int i = 0; i < NUM_DICE; i++) {
                    Dice dice = mDices[i];

                    double averageVelocity = 0;
                    Point3d center = dice.getCenter();
                    for (Particle particle : dice.getParticles()) {
                        averageVelocity += particle.distance(particle.previousPosition);
                    }

                    // Only perform checks if movement has not been detected in previous dice
                    if (!moved) {
                        // Dice has moved if it's center has moved by 0.05, or velocity has changed by 0.01
                        if (previousCenters[i] == null ||
                                previousCenters[i].distance(center) > 0.05 ||
                                Math.abs(previousAverageVelocity[i] - averageVelocity) > 0.01) {
                            moved = true;
                        }
                    }

                    previousAverageVelocity[i] = averageVelocity;
                    previousCenters[i] = center;
                }

                if (!moved) {
                    // If dices have not moved release lock
                    synchronized (mDiceMovingLock) {
                        mDiceMovingLock.notifyAll();
                    }
                }
            }
        };

        // Fade dice in task
        TimerTask fadeInTask = new TimerTask() {
            double progress = 0;

            @Override
            public void run() {
                progress += 1.0 / 20.0;

                if (progress >= 1) {
                    progress = 1;
                    cancel();
                }

                setDiceAlpha((int) (255 * progress));
            }
        };

        // Fade dice out task
        TimerTask fadeOutTask = new TimerTask() {
            double progress = 0;

            @Override
            public void run() {
                progress += 1.0 / 20.0;

                if (progress >= 1) {
                    progress = 1;
                    cancel();
                    setVisible(false);
                }

                setDiceAlpha(255 - (int) (255 * progress));
                repaint();
            }
        };

        // Reset timer
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(physicsTick, 0, 1000 / 60);
        mTimer.scheduleAtFixedRate(checkDiceTick, 500, 200);
        mTimer.scheduleAtFixedRate(fadeInTask, 0, 1000 / 60);

        // Wait until dice have stopped moving
        try {
            synchronized (mDiceMovingLock) {
                mDiceMovingLock.wait();
            }
        } catch (InterruptedException e) {
            // Ignore
        }

        mTimer.scheduleAtFixedRate(fadeOutTask, 5000, 1000 / 60);

        physicsTick.cancel();
        checkDiceTick.cancel();
        fadeInTask.cancel();

        // Return values
        int totalValue = 0;
        for (int i = 0; i < NUM_DICE; i++) {
            diceValues[i] = mDices[i].getValue();
            totalValue += diceValues[i];
        }

        return totalValue;
    }

    private void setDiceAlpha(int alpha) {
        for (Dice dice : mDices) {
            if (alpha < 255) {
                dice.setColors(
                        Util.getColorWithAlpha(Color.WHITE, alpha),
                        Util.getColorWithAlpha(Color.BLACK, alpha),
                        Util.getColorWithAlpha(Color.BLACK, alpha)
                );
            } else {
                dice.setColors(Color.WHITE, Color.BLACK, Color.BLACK);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;

        // Enable anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Translate rendering to screen center so world based elements don't have to
        g.translate(getWidth() / 2, getHeight() / 2);

        // Draw dice
        for (Dice dice : mDices) {
            dice.draw(g);
        }
    }
}
