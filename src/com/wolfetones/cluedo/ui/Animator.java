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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Animator implements ActionListener {
    private static final int FRAMES_PER_SECOND = 60;
    private static final Function<Double, Double> DEFAULT_INTERPOLATOR = Util::easeInOutQuint;

    private static Animator sInstance;

    private Timer mTimer = new Timer(1000 / FRAMES_PER_SECOND, this);

    private Set<Animation> mAnimations = new HashSet<>();

    public static Animator getInstance() {
        if (sInstance == null) {
            sInstance = new Animator();
        }
        return sInstance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Animation animation : mAnimations) {
            if (animation.stopped) {
                continue;
            }

            animation.remainingFrames--;

            double progress = 1.0 - ((double) animation.remainingFrames / animation.totalFrames);
            animation.progress(animation.interpolator.apply(progress));

            if (animation.remainingFrames == 0) {
                stopAnimation(animation);
            }
        }

        mAnimations.removeIf(animation -> animation.stopped);
        if (mAnimations.isEmpty()) mTimer.stop();
    }

    private void startAnimation(Animation animation) {
        // Animation delays
        if (animation.delay > 0) {
            Timer delayTimer = new Timer(animation.delay, null);
            delayTimer.addActionListener(e -> {
                animation.delay = 0;
                startAnimation(animation);

                delayTimer.stop();
            });
            delayTimer.start();
            return;
        }

        animation.totalFrames = animation.duration * FRAMES_PER_SECOND / 1000;
        animation.remainingFrames = animation.totalFrames;

        if (animation.preRunnable != null) {
            animation.preRunnable.run();
        }

        if (mAnimations.isEmpty()) {
            mTimer.start();
        }

        mAnimations.add(animation);
    }

    private void stopAnimation(Animation animation) {
        animation.stopped = true;

        if (animation.postRunnable != null) {
            animation.postRunnable.run();
        }
    }

    public void interruptAllAnimations(Object target) {
        for (Animation animation : mAnimations) {
            if (target == null || animation.target == target) {
                stopAnimation(animation);
            }
        }
    }

    public Animation animateAndInterruptAll(Object target) {
        interruptAllAnimations(target);
        return animate(target);
    }

    public Animation animate(Object target) {
        return new Animation(target);
    }

    public class Animation {
        private Object target;
        private boolean stopped = false;

        private int totalFrames;
        private int remainingFrames;

        private int duration;
        private int delay = 0;
        private Function<Double, Double> interpolator = DEFAULT_INTERPOLATOR;

        private Runnable preRunnable;
        private Runnable postRunnable;

        private int count = 0;
        private List<Double> initialValues = new ArrayList<>(1);
        private List<Double> targetValues = new ArrayList<>(1);
        private List<Consumer<Double>> setters = new ArrayList<>(1);

        private Animation(Object target) {
            this.target = target;
        }

        public void start() {
            startAnimation(this);
        }

        public Animation setDuration(int duration) {
            this.duration = duration;

            return this;
        }

        public Animation setDelay(int delay) {
            this.delay = delay;

            return this;
        }

        public Animation animate(double from, double to, Consumer<Double> setter) {
            initialValues.add(from);
            targetValues.add(to);
            setters.add(setter);
            count++;

            return this;
        }

        public Animation translate(int x, int y) {
            if (!(target instanceof Translatable)) {
                throw new IllegalArgumentException("Component does not implement Translatable interface");
            }

            Translatable component = (Translatable) target;
            int initialX = component.getX();
            int initialY = component.getY();
            animate(0.0, 1.0,
                    progress -> component.setLocation((int) interpolate(initialX, x, progress),
                            (int) interpolate(initialY, y, progress)));

            return this;
        }

        public Animation scale(double scale) {
            if (!(target instanceof Scalable)) {
                throw new IllegalArgumentException("Target does not implement Scalable interface");
            }

            Scalable component = (Scalable) target;
            animate(component.getScale(), scale, component::setScale);

            return this;
        }

        public Animation fade(double alpha) {
            if (!(target instanceof Fadable)) {
                throw new IllegalArgumentException("Target does not implement Fadable interface");
            }

            Fadable component = (Fadable) target;
            animate(component.getAlpha(), alpha, component::setAlpha);

            return this;
        }

        public Animation before(Runnable runnable) {
            preRunnable = runnable;

            return this;
        }

        public Animation after(Runnable runnable) {
            postRunnable = runnable;

            return this;
        }

        private void progress(double progress) {
            for (int i = 0; i < count; i++) {
                double initial = initialValues.get(i);
                double target = targetValues.get(i);
                double interpolation = interpolate(initial, target, progress);

                setters.get(i).accept(interpolation);
            }
        }

        private double interpolate(double initial, double target, double progress) {
            return initial + (target - initial) * progress;
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

    public interface Fadable {
        double getAlpha();
        void setAlpha(double alpha);
    }

    public static class TranslatableComponentAdapter implements Translatable {
        private JComponent mComponent;

        public TranslatableComponentAdapter(JComponent component) {
            mComponent = component;
        }

        @Override
        public int getX() {
            return mComponent.getX();
        }

        @Override
        public int getY() {
            return mComponent.getY();
        }

        @Override
        public void setLocation(int x, int y) {
            mComponent.setLocation(x, y);
        }
    }
}
