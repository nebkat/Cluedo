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
import java.util.function.*;

public class Animator {
    /** Target number of frames/updates per second */
    private static final int FRAMES_PER_SECOND = 60;

    /** Scaling of all durations and delays, used for debugging */
    private static final double TIME_SCALE = 1.0;

    /** The default easing function for all animations when not specified */
    private static final DoubleUnaryOperator DEFAULT_INTERPOLATOR = Animator::easeInOutQuintInterpolator;

    /** Singleton instance */
    private static Animator sInstance;

    private Timer mTimer = new Timer();
    private Set<Animation> mAnimations = new HashSet<>();

    /**
     * Returns the animator object associated with the current Java application.
     *
     * @return the animator object associated with the current Java application.
     */
    public static Animator getInstance() {
        if (sInstance == null) {
            sInstance = new Animator();
        }
        return sInstance;
    }

    /**
     * Creates a new animation object for the specified target.
     *
     * @param target the animation target
     * @return new animation
     */
    public Animation animate(Object target) {
        return new Animation(target);
    }

    /**
     * Cancels all currently running animations on the specified target
     *
     * @param target the target for which to cancel animations
     */
    public void interruptAllAnimations(Object target) {
        Iterator<Animation> iterator = mAnimations.iterator();
        while (iterator.hasNext()) {
            Animation animation = iterator.next();
            if (target == null || animation.target == target) {
                animation.cancel();

                // Release all locks from this animation and its chained children
                Animation a = animation;
                do {
                    synchronized (a.lock) {
                        a.lock.notifyAll();
                    }
                } while ((a = a.chain) != null);

                iterator.remove();
            }
        }
    }

    /**
     * Creates a new animation object for the specified target and interrupts all previously running animations on it.
     *
     * @param target the animation target
     * @return new animation
     */
    public Animation animateAndInterruptAll(Object target) {
        interruptAllAnimations(target);
        return animate(target);
    }

    /**
     * Object representing an animation of a set of properties on a target.
     */
    public class Animation extends TimerTask {
        private Object target;

        private boolean scheduled = false;
        private boolean completed = false;

        private int totalFrames;
        private int remainingFrames;

        private int duration;
        private int delay = 0;
        private DoubleUnaryOperator interpolator = DEFAULT_INTERPOLATOR;

        private Runnable preRunnable;
        private Runnable postRunnable;

        private Animation parent;
        private Animation chain;

        private boolean skip = false;
        private final Object lock = new Object();

        private List<Property> properties = new ArrayList<>(1);

        private Animation(Object target) {
            this.target = target;
        }

        /**
         * Starts the animation, or the animation's parent animation if it is chained.
         *
         *
         */
        public void start() {
            // Start parent first
            if (parent != null) {
                parent.start();
                return;
            }

            // Already scheduled
            if (scheduled) return;
            scheduled = true;

            // Timings
            totalFrames = (int) (duration * FRAMES_PER_SECOND / 1000 * TIME_SCALE);
            remainingFrames = totalFrames;

            // Initial/target property values
            Iterator<Property> iterator = properties.iterator();
            while (iterator.hasNext()) {
                Property property = iterator.next();
                if (property.initialSupplier != null) {
                    property.initial = property.initialSupplier.getAsDouble();
                }

                if (property.initial == property.target) {
                    property.consumer.accept(property.target);
                    iterator.remove();
                }
            }

            // Pre runnable
            if (preRunnable != null) {
                preRunnable.run();
            }

            // Schedule
            mTimer.scheduleAtFixedRate(this, (int) (delay * TIME_SCALE), 1000 / FRAMES_PER_SECOND);

            // Add to animations list
            mAnimations.add(this);
        }

        private void complete() {
            // Already completed
            if (completed) return;
            completed = true;

            // Disable timer task
            cancel();

            // Post runnable
            if (postRunnable != null) {
                postRunnable.run();
            }

            // Start chained animation
            if (chain != null) {
                chain.parent = null;
                chain.start();
            }

            // Unlock locks
            synchronized (lock) {
                lock.notifyAll();
            }

            // Remove from animations list
            mAnimations.remove(this);
        }

        /**
         * Sets the duration of the animation.
         *
         * @param duration the duration of the animation
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation setDuration(int duration) {
            this.duration = duration;

            return this;
        }

        /**
         * Sets the starting delay of the animation.
         *
         * @param delay the starting delay of the animation
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation setDelay(int delay) {
            this.delay = delay;

            return this;
        }

        /**
         * Sets the easing function of the animation.
         *
         * @param interpolator the easing function of the animation
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation setInterpolator(DoubleUnaryOperator interpolator) {
            this.interpolator = interpolator;

            return this;
        }

        /**
         * Adds a property to be animated by the animation.
         *
         * @param from a supplier of the initial value of this property when the animations starts
         * @param to the target value of this property when the animation completes
         * @param consumer the setter of this animated values of this property
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation animate(DoubleSupplier from, double to, DoubleConsumer consumer) {
            Property property = new Property();

            property.initialSupplier = from;
            property.target = to;
            property.consumer = consumer;

            properties.add(property);

            return this;
        }

        /**
         * Adds a property to be animated by the animation.
         *
         * @param from the initial value of this property when the animations starts
         * @param to the target value of this property when the animation completes
         * @param consumer the setter of this animated values of this property
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation animate(double from, double to, DoubleConsumer consumer) {
            Property property = new Property();

            property.initial = from;
            property.target = to;
            property.consumer = consumer;

            properties.add(property);

            return this;
        }

        /**
         * Translates the object being animated.
         *
         * Requires that the object animated implement the {@link Translatable} interface,
         * or be an AWT {@link Component}.
         *
         * @param x the target X location of the object when the animation completes
         * @param y the target Y location of the object when the animation completes
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation translate(int x, int y) {
            if (!(target instanceof Translatable || target instanceof Component)) {
                throw new IllegalArgumentException("Component does not implement Translatable interface");
            }

            Translatable component;
            if (target instanceof Component) {
                component = new ComponentTranslatableWrapper((Component) target);
            } else {
                component = (Translatable) target;
            }

            animate(component::getX, x, component::setX);
            animate(component::getY, y, component::setY);

            return this;
        }


        /**
         * Translates the object being animated.
         *
         * Requires that the object being animated implement the {@link Translatable} interface,
         * or be an AWT {@link Component}.
         *
         * @param fromX the initial X location of the object when the animation starts
         * @param fromY the initial Y location of the object when the animation starts
         * @param toX the target X location of the object when the animation completes
         * @param toY the target Y location of the object when the animation completes
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation translate(int fromX, int fromY, int toX, int toY) {
            if (!(target instanceof Translatable || target instanceof Component)) {
                throw new IllegalArgumentException("Component does not implement Translatable interface");
            }

            Translatable component;
            if (target instanceof Component) {
                component = new ComponentTranslatableWrapper((Component) target);
            } else {
                component = (Translatable) target;
            }

            animate(fromX, toX, component::setX);
            animate(fromY, toY, component::setY);

            return this;
        }

        /**
         * Scales the object being animated.
         *
         * Requires that the object being animated implement the {@link Scalable} interface.
         *
         * @param scale the target scale of the object when the animation completes
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation scale(double scale) {
            if (!(target instanceof Scalable)) {
                throw new IllegalArgumentException("Target does not implement Scalable interface");
            }

            Scalable component = (Scalable) target;
            animate(component::getScale, scale, component::setScale);

            return this;
        }

        /**
         * Scales the object being animated.
         *
         * Requires that the object being animated implement the {@link ScalableXY} interface.
         *
         * @param scaleX the target X scale of the object when the animation completes
         * @param scaleY the target Y scale of the object when the animation completes
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation scale(double scaleX, double scaleY) {
            if (!(target instanceof ScalableXY)) {
                throw new IllegalArgumentException("Target does not implement ScalableXY interface");
            }

            ScalableXY component = (ScalableXY) target;
            animate(component::getScaleX, scaleX, component::setScaleX);
            animate(component::getScaleY, scaleY, component::setScaleY);

            return this;
        }

        /**
         * Fades the object being animated.
         *
         * Requires that the object being animated implement the {@link Fadable} interface.
         *
         * @param alpha the target opacity of the object when the animation completes
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation fade(double alpha) {
            if (!(target instanceof Fadable)) {
                throw new IllegalArgumentException("Target does not implement Fadable interface");
            }

            Fadable component = (Fadable) target;
            animate(component::getAlpha, alpha, component::setAlpha);

            return this;
        }

        /**
         * Fades the object being animated to full opacity.
         *
         * Requires that the object being animated implement the {@link Fadable} interface.
         *
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation fadeIn() {
            return fade(1.0);
        }

        /**
         * Fades the object being animated to full transparency.
         *
         * Requires that the object being animated implement the {@link Fadable} interface.
         *
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation fadeOut() {
            return fade(0.0);
        }

        /**
         * Runs the runnable before the animation starts.
         *
         * @param runnable the runnable to run.
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation before(Runnable runnable) {
            preRunnable = runnable;

            return this;
        }

        /**
         * Runs the runnable after the animation starts.
         *
         * @param runnable the runnable to run.
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation after(Runnable runnable) {
            postRunnable = runnable;

            return this;
        }

        /**
         * Initiates a new animation to be run once this animation has completed.
         *
         * @return a new animation to be run once this animation has completed.
         */
        public Animation chain() {
            chain = new Animation(target);
            chain.parent = this;

            return chain;
        }

        /**
         * Skips the execution of the animation.
         *
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation skip() {
            skip = true;

            return this;
        }

        /**
         * Skips the execution of the animation based on a condition.
         *
         * @param condition whether to skip the execution of the animation.
         * @return a reference to this {@code Animation} object to fulfill the "Builder" pattern
         */
        public Animation skipIf(boolean condition) {
            if (condition) skip();

            return this;
        }

        /**
         * Starts the animation and waits for it to complete before returning.
         */
        public void await() {
            start();

            if (skip) {
                return;
            }

            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        /**
         * Awaits or starts the animation based on a condition.
         *
         * Useful in loops when wishing to await only the last object being animated.
         *
         * @param condition whether to await completion of the animation
         */
        public void awaitIf(boolean condition) {
            if (condition) {
                await();
            } else {
                start();
            }
        }

        private void progress(double progress) {
            for (Property property : properties) {
                double interpolation = Animator.interpolate(property.initial, property.target, progress);

                property.consumer.accept(interpolation);
            }
        }

        @Override
        public void run() {
            remainingFrames--;

            double progress = totalFrames == 0 ? 1.0 : 1.0 - ((double) remainingFrames / totalFrames);
            progress(interpolator != null ? interpolator.applyAsDouble(progress) : progress);

            if (remainingFrames <= 0) {
                complete();
            }
        }

        private class Property {
            DoubleSupplier initialSupplier;

            double initial;
            double target;

            DoubleConsumer consumer;
        }
    }

    private class ComponentTranslatableWrapper implements Translatable {
        private Component mComponent;

        private ComponentTranslatableWrapper(Component component) {
            mComponent = component;
        }

        @Override
        public double getX() {
            return mComponent.getX();
        }

        @Override
        public double getY() {
            return mComponent.getY();
        }

        @Override
        public void setX(double x) {
            mComponent.setLocation((int) x, mComponent.getY());
        }

        @Override
        public void setY(double y) {
            mComponent.setLocation(mComponent.getX(), (int) y);
        }
    }

    /**
     * Implementing this interface allows objects to be translated using {@link Animation#translate(int, int)}.
     */
    public interface Translatable {
        double getX();
        double getY();
        void setX(double x);
        void setY(double y);
    }

    /**
     * Implementing this interface allows objects to be scaled using {@link Animation#scale(double)}.
     */
    public interface Scalable {
        double getScale();
        void setScale(double scale);
    }

    /**
     * Implementing this interface allows objects to be scaled using {@link Animation#scale(double, double)}.
     */
    public interface ScalableXY {
        double getScaleX();
        double getScaleY();
        void setScaleX(double scale);
        void setScaleY(double scale);
    }

    /**
     * Implementing this interface allows objects to be faded using {@link Animation#fade(double)}.
     */
    public interface Fadable {
        double getAlpha();
        void setAlpha(double alpha);
    }

    private static double interpolate(double initial, double target, double progress) {
        return initial + (target - initial) * progress;
    }

    /**
     * Cubic ease in interpolator.
     *
     * @param t current progress [0..1]
     * @return interpolated progress [0..1]
     */
    public static double easeInCubic(double t) {
        return t * t * t;
    }

    /**
     * Cubic ease out interpolator.
     *
     * @param t current progress [0..1]
     * @return interpolated progress [0..1]
     */
    public static double easeOutCubic(double t) {
        return (--t) * t * t + 1;
    }

    /**
     * Quintic ease out interpolator.
     *
     * @param t current progress [0..1]
     * @return interpolated progress [0..1]
     */
    public static double easeOutQuint(double t) {
        return 1 + (--t) * t * t * t * t;
    }

    /**
     * Quintic ease in-out in interpolator.
     *
     * @param t current progress [0..1]
     * @return interpolated progress [0..1]
     */
    public static double easeInOutQuintInterpolator(double t) {
        return t < .5 ? 16 * t * t * t * t * t : 1 + 16 * (--t) * t * t * t * t;
    }
}
