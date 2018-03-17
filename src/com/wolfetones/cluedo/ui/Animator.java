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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;

public class Animator implements ActionListener {
    private static final int FRAMES_PER_SECOND = 60;

    private Object mComponent;
    private Timer mTimer = new Timer(1000 / FRAMES_PER_SECOND, this);

    private Function<Double, Double> mInterpolator = Util::easeInOutQuint;

    private boolean mTranslating;
    private int mInitialX;
    private int mInitialY;
    private int mTargetX;
    private int mTargetY;

    private boolean mScaling;
    private double mInitialScale;
    private double mTargetScale;

    private int mTotalFrames;
    private int mRemainingFrames;

    public Animator(Object component) {
        mComponent = component;
    }

    public Animator(Object component, int defaultDuration) {
        this(component);
        setDuration(defaultDuration);
    }

    public void translate(int x, int y) {
        if (!(mComponent instanceof Translatable)) {
            throw new IllegalArgumentException("Component does not implement Translatable interface");
        }

        mTranslating = true;

        Translatable component = (Translatable) mComponent;

        mInitialX = component.getX();
        mInitialY = component.getY();

        mTargetX = x;
        mTargetY = y;
    }

    public void scale(double scale) {
        if (!(mComponent instanceof Scalable)) {
            throw new IllegalArgumentException("Component does not implement Scalable interface");
        }

        mScaling = true;

        Scalable component = (Scalable) mComponent;

        mInitialScale = component.getScale();
        mTargetScale = scale;
    }

    public void start() {
        mRemainingFrames = mTotalFrames;

        mTimer.restart();
    }

    public void stop() {
        mTranslating = false;
        mScaling = false;

        mTimer.stop();
    }

    public void setDuration(int duration) {
        mTotalFrames = duration * FRAMES_PER_SECOND / 1000;
    }

    public void setDelay(int delay) {
        mTimer.setInitialDelay(delay);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (--mRemainingFrames == 0) {
            stop();
        }

        double progress = 1.0 - ((double) mRemainingFrames / (double) mTotalFrames);
        double interpolation = mInterpolator.apply(progress);

        if (mTranslating) {
            int x = mInitialX + (int) ((mTargetX - mInitialX) * interpolation);
            int y = mInitialY + (int) ((mTargetY - mInitialY) * interpolation);

            ((Translatable) mComponent).setLocation(x, y);
        }

        if (mScaling) {
            double scale = mInitialScale + (mTargetScale - mInitialScale) * interpolation;

            ((Scalable) mComponent).setScale(scale);
        }
    }

    public interface Translatable {
        int getX();
        int getY();
        void setLocation(int x, int y);
    }

    public interface Scalable {
        double getScale();
        void setScale(double scale);
    }
}
