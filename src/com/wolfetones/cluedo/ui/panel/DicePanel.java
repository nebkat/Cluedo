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

package com.wolfetones.cluedo.ui.panel;

import com.wolfetones.cluedo.util.Util;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Game;
import com.wolfetones.cluedo.ui.Animator;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.DoubleConsumer;

public class DicePanel extends JPanel implements Animator.Fadable {
    private static final int FRAMES_PER_SECOND = 60;
    private static final int NUM_DICE = Game.NUM_DICE;

    private static final double GRAVITY = 0.5;
    private static final int FLOOR_DEPTH = -1000;
    private static final int CUBE_SIZE = 75;
    private static final int SCREEN_CUBE_SIZE = Config.screenRelativeSize(CUBE_SIZE);

    private final Object mDiceMovingLock = new Object();

    private VerletPhysics mPhysics = new VerletPhysics(10);

    private Dice[] mDices = new Dice[NUM_DICE];

    private Timer mTimer;
    private boolean mForceFinish;

    private float mAlpha = 0;

    private int mTotalValue;
    private float mTotalTextAlpha = 0;
    private Font mTextFont = new Font(Font.SANS_SERIF, Font.BOLD, SCREEN_CUBE_SIZE);

    public DicePanel() {
        super();

        setOpaque(false);
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
        mPhysics.addConstraint(new PlaneConstraint(RenderUtils.getPlaneFromCameraToPoint(new Point2d(getWidth() / Config.screenRelativeSize(2.0), 0)), 1.0, 1.0));
        mPhysics.addConstraint(new PlaneConstraint(RenderUtils.getPlaneFromCameraToPoint(new Point2d(-getWidth() / Config.screenRelativeSize(2.0), 0)), 1.0, 1.0));
        mPhysics.addConstraint(new PlaneConstraint(RenderUtils.getPlaneFromCameraToPoint(new Point2d(0, getHeight() / Config.screenRelativeSize(2.0))), 1.0, 1.0));
        mPhysics.addConstraint(new PlaneConstraint(RenderUtils.getPlaneFromCameraToPoint(new Point2d(0, -getHeight() / Config.screenRelativeSize(2.0))), 1.0, 1.0));

        // Dice
        for (int i = 0; i < NUM_DICE; i++) {
            mDices[i] = new Dice(new Point3d(0, 0, 0), CUBE_SIZE);
            mPhysics.addBody(mDices[i]);
        }
    }

    public int rollDice(int[] diceValues, boolean waitForAnimation) {
        // Reset status
        mForceFinish = false;
        mAlpha = 0;
        mTotalTextAlpha = 0;

        throwDice();

        // Physics update task
        TimerTask physicsTick = new TimerTask() {
            @Override
            public void run() {
                mPhysics.update();
                repaintDice();
            }
        };

        // Check for dice movement task
        TimerTask checkDiceTick = new CheckDiceTask();

        // Force finish after timeout
        TimerTask forceFinishTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (mDiceMovingLock) {
                    mDiceMovingLock.notifyAll();
                }
            }
        };

        // Reset timer
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(physicsTick, 0, 1000 / FRAMES_PER_SECOND);
        mTimer.scheduleAtFixedRate(checkDiceTick, 500, 100);
        mTimer.schedule(forceFinishTask, 5000);

        // Fade in
        Animator.getInstance().animate(this)
                .fadeIn()
                .setDuration(400)
                .setInterpolator(Animator::easeInCubic)
                .start();

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

        // Don't wait for animation if force finished
        if (mForceFinish && waitForAnimation) {
            mAlpha = 0;
            repaint();

            return mTotalValue;
        }

        // Translations and transformations to move each dice to the front of the screen
        Vector3d[] moveToCenterTranslateDeltas = new Vector3d[NUM_DICE];
        AxisAngle4d[] moveToCenterTransformDeltas = new AxisAngle4d[NUM_DICE];

        getMoveToCenterTransformations(moveToCenterTranslateDeltas, moveToCenterTransformDeltas);

        Animator.getInstance().animate(this)
                .animate(0.0, 1.0, new DoubleConsumer() {
                    double previousInterpolation = 0;

                    Vector3d translation = new Vector3d();
                    AxisAngle4d rotation = new AxisAngle4d();
                    Matrix3d transform = new Matrix3d();

                    @Override
                    public void accept(double interpolation) {
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

                        repaintDice();
                    }
                })
                .animate(0.0, 1.0, progress -> mTotalTextAlpha = (float) progress)
                .setDuration(1500)
                .setDelay(250)
            .chain()
                .fadeOut()
                .after(this::repaint)
                .setDuration(500)
                .setDelay(1000)
                .setInterpolator(Animator::easeOutCubic)
                .awaitIf(waitForAnimation);

        // Ensure repaint happens if animation is skipped
        if (waitForAnimation) {
            mAlpha = 0;
            repaint();
        }

        return mTotalValue;
    }

    private void throwDice() {
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
                translate = new Vector3d((Math.random() - 0.5) * 15,
                        (Math.random() - 0.5) * 15, 0);

                if (previousTranslate == null) {
                    break;
                }

                previousTranslateDistance.sub(translate, previousTranslate);
            } while (previousTranslateDistance.length() < 5);

            // Store translation for angle calculations
            previousTranslate = translate;

            // Apply force
            dice.translate(translate);
        }
    }

    private void getMoveToCenterTransformations(Vector3d[] moveToCenterTranslateDeltas, AxisAngle4d[] moveToCenterTransformDeltas) {
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
    }

    public void forceFinish() {
        // Already force finished
        if (mForceFinish) {
            return;
        }

        mForceFinish = true;

        mTimer.cancel();
        for (int i = 0; i < FRAMES_PER_SECOND * 10; i++) {
            mPhysics.update();
        }

        synchronized (mDiceMovingLock) {
            mDiceMovingLock.notifyAll();
        }

        Animator.getInstance().interruptAllAnimations(this);
    }

    private void repaintDice() {
        for (Dice dice : mDices) {
            Point2d p = RenderUtils.projectToScreen(dice.getCenter());

            repaint((int) p.x + getWidth() / 2 - SCREEN_CUBE_SIZE,
                    (int) p.y + getHeight() / 2 - SCREEN_CUBE_SIZE,
                    SCREEN_CUBE_SIZE * 2,
                    SCREEN_CUBE_SIZE * 2);
        }

        if (mTotalTextAlpha > 0) {
            repaint(getWidth() / 2 - SCREEN_CUBE_SIZE / 2,
                    getHeight() / 2 - SCREEN_CUBE_SIZE / 2,
                    SCREEN_CUBE_SIZE * 5, SCREEN_CUBE_SIZE);
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

        // Fade in/out
        if (mAlpha < 1) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, mAlpha));
        }

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
            Util.drawCenteredString("=", 2 * SCREEN_CUBE_SIZE, 0, 0, 0, g);
            Util.drawCenteredString(Integer.toString(mTotalValue), (int) (2.5 * SCREEN_CUBE_SIZE), 0, -1, 0, g);
        }
    }

    @Override
    public double getAlpha() {
        return mAlpha;
    }

    @Override
    public void setAlpha(double alpha) {
        mAlpha = (float) alpha;

        repaintDice();
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

    private class CheckDiceTask extends TimerTask {
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
    }
}
