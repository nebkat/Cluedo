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
import com.wolfetones.physics.VectorUtils;
import com.wolfetones.physics.body.Dice;
import com.wolfetones.physics.Particle;
import com.wolfetones.physics.RenderUtils;
import com.wolfetones.physics.VerletPhysics;
import com.wolfetones.physics.behavior.GravityBehavior;
import com.wolfetones.physics.constraint.PlaneConstraint;
import com.wolfetones.physics.geometry.Axis;
import com.wolfetones.physics.geometry.Plane;

import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class DicePanel extends JPanel {
    private static final int FRAMES_PER_SECOND = 60;
    private static final int NUM_DICE = Game.NUM_DICE;

    private static final double GRAVITY = (double) Config.screenRelativeSize(10) / 20;
    private static final int FLOOR_DEPTH = -Config.screenRelativeSize(1000);
    private static final int CUBE_SIZE = Config.screenRelativeSize(75);

    private final Object mDiceMovingLock = new Object();
    private final Object mWaitForAnimationLock = new Object();

    private VerletPhysics mPhysics = new VerletPhysics(10);

    private Dice[] mDices = new Dice[NUM_DICE];

    private Timer mTimer;
    private boolean mForceFinish;

    private float mAlpha = 0f;

    private int mTotalValue;
    private float mTotalTextAlpha = 0f;
    private Font mTextFont = new Font(Font.SANS_SERIF, Font.BOLD, CUBE_SIZE);

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

    public int rollDice(int[] diceValues, boolean waitForAnimation) {
        // Show panel
        setVisible(true);

        // Reset status
        mForceFinish = false;
        mAlpha = 0;
        mTotalTextAlpha = 0;

        // Translation of previous dice
        Vector3d previousTranslate = null;
        Vector3d previousTranslateDistance = new Vector3d();
        for (Dice dice : mDices) {
            // Reset dice position
            dice.reset();

            // Rotate by random amount without velocity
            dice.transform(randomRotationMatrix(), true);

            // Rotate by random amount with velocity
            dice.transform(randomRotationMatrix());

            // Apply force in random direction, ensuring sufficient distance between translate vectors of the two dice
            Vector3d translate;
            do {
                translate = new Vector3d((Math.random() - 0.5) * Config.screenRelativeSize(15),
                        (Math.random() - 0.5) * Config.screenRelativeSize(15), 0);

                if (previousTranslate == null) {
                    break;
                }

                previousTranslateDistance.sub(translate, previousTranslate);
            } while (previousTranslateDistance.length() < Config.screenRelativeSize(5));

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
                        // Dice has moved if it's center has moved by 0.5, or velocity has changed by 0.05
                        if (previousCenters[i] == null ||
                                previousCenters[i].distance(center) > 0.5 ||
                                Math.abs(previousAverageVelocity[i] - averageVelocity) > 0.05) {
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

        // Force finish after timeout
        TimerTask forceFinishTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (mDiceMovingLock) {
                    mDiceMovingLock.notifyAll();
                }
            }
        };

        // Fade dice in task
        TimerTask fadeInTask = new FixedCountTimerTask(400, FRAMES_PER_SECOND) {
            @Override
            protected void progress(double progress) {
                mAlpha = (float) (progress);
                repaint();
            }
        };

        // Fade dice out task
        TimerTask fadeOutTask = new FixedCountTimerTask(400, FRAMES_PER_SECOND) {
            @Override
            protected void progress(double progress) {
                if (progress == 1) {
                    setVisible(false);
                    if (waitForAnimation) {
                        synchronized (mWaitForAnimationLock) {
                            mWaitForAnimationLock.notifyAll();
                        }
                    }
                }

                mAlpha = (float) (1 - progress);
                repaint();
            }
        };

        // Reset timer
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(physicsTick, 0, 1000 / FRAMES_PER_SECOND);
        mTimer.scheduleAtFixedRate(checkDiceTick, 500, 250);
        mTimer.scheduleAtFixedRate(fadeInTask, 0, 1000 / FRAMES_PER_SECOND);
        mTimer.schedule(forceFinishTask, 5000);

        // Wait until dice have stopped moving
        try {
            synchronized (mDiceMovingLock) {
                mDiceMovingLock.wait();
            }
        } catch (InterruptedException e) {
            // Ignore
        }

        physicsTick.cancel();
        checkDiceTick.cancel();
        fadeInTask.cancel();
        forceFinishTask.cancel();

        // Return values
        mTotalValue = 0;
        for (int i = 0; i < NUM_DICE; i++) {
            int value = mDices[i].getValue();
            if (diceValues != null) {
                diceValues[i] = value;
            }
            mTotalValue += value;
        }

        // Force finish
        if (mForceFinish) {
            return mTotalValue;
        }

        // Translations and transformations to move each dice to the front of the screen
        Vector3d[] moveToCenterTranslateDeltas = new Vector3d[NUM_DICE];
        AxisAngle4d[] moveToCenterTransformDeltas = new AxisAngle4d[NUM_DICE];

        // Whether to put first dice on left or right
        boolean reverseOrder = mDices[1].getCenter().x < mDices[0].getCenter().x;
        for (int i = 0; i < NUM_DICE; i++) {
            Dice dice = mDices[i];

            Point3d[] face = dice.getFace(dice.getHighestZFace());

            int positioningIndex = !reverseOrder ? i : (NUM_DICE - i - 1);

            Point3d center = VectorUtils.average(face);
            Point3d target = new Point3d((2 * positioningIndex - NUM_DICE + 1) * CUBE_SIZE, 0, 0);

            Vector3d translate = new Vector3d();
            translate.sub(target, center);
            moveToCenterTranslateDeltas[i] = translate;

            // Vector along one edge of the top face
            Vector3d x = new Vector3d();
            x.sub(face[1], face[0]);
            x.normalize();

            // Axis around which to rotate
            Vector3d rotationNormal = new Vector3d();
            rotationNormal.cross(x, Axis.X);

            // Rotate to closest axis (x/y)
            double rotationAngle = x.angle(Axis.X) % (Math.PI / 2);
            if (rotationAngle > Math.PI / 4) {
                rotationAngle -= Math.PI / 2;
            }

            // Axis angle rotation to align dice with an axis
            AxisAngle4d transform = new AxisAngle4d(rotationNormal.x, rotationNormal.y, rotationNormal.z, rotationAngle);
            moveToCenterTransformDeltas[i] = transform;
        }

        TimerTask moveDiceToCenterTask = new FixedCountTimerTask(1500, FRAMES_PER_SECOND) {
            double previousInterpolation = 0;

            Vector3d translation = new Vector3d();
            AxisAngle4d rotation = new AxisAngle4d();
            Matrix3d transform = new Matrix3d();

            @Override
            protected void progress(double progress) {
                double interpolation = Util.easeInOutQuint(progress);

                mTotalTextAlpha = (float) interpolation;

                double scale = interpolation - previousInterpolation;

                previousInterpolation = interpolation;

                for (int i = 0; i < NUM_DICE; i++) {
                    translation.scale(scale, moveToCenterTranslateDeltas[i]);
                    rotation.set(moveToCenterTransformDeltas[i]);
                    rotation.angle *= scale;

                    transform.set(rotation);

                    mDices[i].translate(translation, true);
                    mDices[i].transform(transform, true);
                }

                repaint();
            }
        };

        mTimer.scheduleAtFixedRate(moveDiceToCenterTask, 100, 1000 / FRAMES_PER_SECOND);
        mTimer.scheduleAtFixedRate(fadeOutTask, 2000, 1000 / FRAMES_PER_SECOND);

        // Wait for dice to fade out
        if (waitForAnimation) {
            try {
                synchronized (mWaitForAnimationLock) {
                    mWaitForAnimationLock.wait();
                }
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        return mTotalValue;
    }

    public void forceFinish() {
        // Already force finished
        if (mForceFinish) {
            return;
        }

        mForceFinish = true;

        mTimer.cancel();

        for (int i = 0; i < 100; i++) {
            mPhysics.update();
        }

        synchronized (mDiceMovingLock) {
            mDiceMovingLock.notifyAll();
        }

        synchronized (mWaitForAnimationLock) {
            mWaitForAnimationLock.notifyAll();
        }

        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;

        // Enable anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Translate rendering to screen center so world based elements don't have to
        g.translate(getWidth() / 2, getHeight() / 2);

        // Fade in/out
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, mAlpha));

        // Draw dice
        for (Dice dice : mDices) {
            dice.draw(g);
        }

        // Draw + = text
        if (mTotalTextAlpha > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, mTotalTextAlpha * mAlpha));

            g.setFont(mTextFont);
            g.setColor(Color.BLACK);

            Util.drawCenteredString("+", 0, 0, 0, 0, g);
            Util.drawCenteredString("=", 2 * CUBE_SIZE, 0, 0, 0, g);
            Util.drawCenteredString(Integer.toString(mTotalValue), (int) (2.5 * CUBE_SIZE), 0, -1, 0, g);
        }
    }

    private abstract class FixedCountTimerTask extends TimerTask {
        private int mTotalFrames;
        private int mCurrentFrame = 0;

        private FixedCountTimerTask(int duration, int fps) {
            mTotalFrames = duration * fps / 1000;
        }

        @Override
        public void run() {
            double progress = (double) mCurrentFrame / mTotalFrames;
            mCurrentFrame++;
            if (progress >= 1) {
                cancel();
            }

            progress(progress);
        }

        protected abstract void progress(double progress);
    }
}
