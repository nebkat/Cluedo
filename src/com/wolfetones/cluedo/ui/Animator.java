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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Animator {
    private static final int FRAMES_PER_SECOND = 60;
    private static final Function<Double, Double> DEFAULT_INTERPOLATOR = Animator::easeInOutQuintInterpolator;

    private static Animator sInstance;

    private Timer mTimer = new Timer();

    private Set<Animation> mAnimations = new HashSet<>();

    public static Animator getInstance() {
        if (sInstance == null) {
            sInstance = new Animator();
        }
        return sInstance;
    }

    private void startAnimation(Animation animation) {
        // Animation delays
        animation.totalFrames = animation.duration * FRAMES_PER_SECOND / 1000;
        animation.remainingFrames = animation.totalFrames;

        if (animation.preRunnable != null) {
            animation.preRunnable.run();
        }

        mTimer.scheduleAtFixedRate(animation, animation.delay, 1000 / FRAMES_PER_SECOND);
        mAnimations.add(animation);
    }

    private void stopAnimation(Animation animation) {
        animation.cancel();

        if (animation.postRunnable != null) {
            animation.postRunnable.run();
        }

        mAnimations.remove(animation);
    }

    public void interruptAllAnimations(Object target) {
        Iterator<Animation> iterator = mAnimations.iterator();
        while (iterator.hasNext()) {
            Animation animation = iterator.next();
            if (target == null || animation.target == target) {
                animation.cancel();
                iterator.remove();
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

    public class Animation extends TimerTask {
        private Object target;

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

        public Animation setInterpolator(Function<Double, Double> interpolator) {
            this.interpolator = interpolator;

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
            if (!(target instanceof Translatable || target instanceof Component)) {
                throw new IllegalArgumentException("Component does not implement Translatable interface");
            }

            if (target instanceof Translatable) {
                Translatable component = (Translatable) target;
                int initialX = component.getX();
                int initialY = component.getY();
                animate(0.0, 1.0,
                        progress -> component.setLocation((int) interpolate(initialX, x, progress),
                                (int) interpolate(initialY, y, progress)));
            } else {
                Component component = (Component) target;
                int initialX = component.getX();
                int initialY = component.getY();
                animate(0.0, 1.0,
                        progress -> component.setLocation((int) interpolate(initialX, x, progress),
                                (int) interpolate(initialY, y, progress)));
            }

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

        public Animation fadeIn() {
            return fade(1.0);
        }

        public Animation fadeOut() {
            return fade(0.0);
        }

        public Animation before(Runnable runnable) {
            preRunnable = runnable;

            return this;
        }

        public Animation after(Runnable runnable) {
            postRunnable = runnable;

            return this;
        }

        public Animation after(Animation animation) {
            postRunnable = animation::start;

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

        @Override
        public void run() {
            remainingFrames--;

            double progress = 1.0 - ((double) remainingFrames / totalFrames);
            progress(interpolator != null ? interpolator.apply(progress) : progress);

            if (remainingFrames == 0) {
                stopAnimation(this);
            }
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

    public static double easeInCubic(double t) {
        return t * t * t;
    }

    public static double easeOutCubic(double t) {
        return (--t) * t * t + 1;
    }

    public static double easeInOutQuintInterpolator(double t) {
        return t < .5 ? 16 * t * t * t * t * t : 1 + 16 * (--t) * t * t * t * t;
    }
}
