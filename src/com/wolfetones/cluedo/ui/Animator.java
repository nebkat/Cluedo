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
    private static final int FRAMES_PER_SECOND = 60;
    private static final double TIME_SCALE = 1.0;
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
        animation.totalFrames = (int) (animation.duration * FRAMES_PER_SECOND / 1000 * TIME_SCALE);
        animation.remainingFrames = animation.totalFrames;

        if (animation.preRunnable != null) {
            animation.preRunnable.run();
        }

        mTimer.scheduleAtFixedRate(animation, (int) (animation.delay * TIME_SCALE), 1000 / FRAMES_PER_SECOND);
        mAnimations.add(animation);
    }

    private void stopAnimation(Animation animation) {
        animation.cancel();

        if (animation.postRunnable != null) {
            animation.postRunnable.run();
        }

        if (animation.chain != null) {
            animation.chain.parent = null;
            animation.chain.start();
        }

        synchronized (animation.lock) {
            animation.lock.notifyAll();
        }

        mAnimations.remove(animation);
    }

    public void interruptAllAnimations(Object target) {
        Iterator<Animation> iterator = mAnimations.iterator();
        while (iterator.hasNext()) {
            Animation animation = iterator.next();
            if (target == null || animation.target == target) {
                animation.cancel();

                // Release all locks from this animation and it's chained children
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

        private Animation parent;
        private Animation chain;

        private boolean skip = false;
        private final Object lock = new Object();

        private List<Property> properties = new ArrayList<>(1);

        private Animation(Object target) {
            this.target = target;
        }

        public void start() {
            if (parent != null) {
                parent.start();
                return;
            }

            if (skip) {
                return;
            }

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

        public Animation animate(DoubleSupplier from, double to, DoubleConsumer consumer) {
            Property property = new Property();

            property.initialSupplier = from;
            property.target = to;
            property.consumer = consumer;

            properties.add(property);

            return this;
        }

        public Animation animate(double from, double to, DoubleConsumer consumer) {
            Property property = new Property();

            property.initial = from;
            property.target = to;
            property.consumer = consumer;

            properties.add(property);

            return this;
        }

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

        public Animation scale(double scale) {
            if (!(target instanceof Scalable)) {
                throw new IllegalArgumentException("Target does not implement Scalable interface");
            }

            Scalable component = (Scalable) target;
            animate(component::getScale, scale, component::setScale);

            return this;
        }

        public Animation scale(double scaleX, double scaleY) {
            if (!(target instanceof ScalableXY)) {
                throw new IllegalArgumentException("Target does not implement ScalableXY interface");
            }

            ScalableXY component = (ScalableXY) target;
            animate(component::getScaleX, scaleX, component::setScaleX);
            animate(component::getScaleY, scaleY, component::setScaleY);

            return this;
        }

        public Animation fade(double alpha) {
            if (!(target instanceof Fadable)) {
                throw new IllegalArgumentException("Target does not implement Fadable interface");
            }

            Fadable component = (Fadable) target;
            animate(component::getAlpha, alpha, component::setAlpha);

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

        public Animation chain() {
            chain = new Animation(target);
            chain.parent = this;

            return chain;
        }

        public Animation skipIf(boolean condition) {
            skip = condition;

            return this;
        }

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

        public void awaitIf(boolean await) {
            if (await) {
                await();
            } else {
                start();
            }
        }

        private void progress(double progress) {
            for (Property property : properties) {
                double interpolation = interpolate(property.initial, property.target, progress);

                property.consumer.accept(interpolation);
            }
        }

        private double interpolate(double initial, double target, double progress) {
            return initial + (target - initial) * progress;
        }

        @Override
        public void run() {
            remainingFrames--;

            double progress = totalFrames == 0 ? 1.0 : 1.0 - ((double) remainingFrames / totalFrames);
            progress(interpolator != null ? interpolator.apply(progress) : progress);

            if (remainingFrames <= 0) {
                stopAnimation(this);
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

    public interface Translatable {
        double getX();
        double getY();
        void setX(double x);
        void setY(double y);
    }

    public interface Scalable {
        double getScale();
        void setScale(double scale);
    }

    public interface ScalableXY {
        double getScaleX();
        double getScaleY();
        void setScaleX(double scale);
        void setScaleY(double scale);
    }

    public interface Fadable {
        double getAlpha();
        void setAlpha(double alpha);
    }

    public static boolean applyTransformations(Graphics2D g, Component c, double scaleX, double scaleY, double alpha) {
        if (alpha < 1) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
        }

        if (scaleX == 0 || scaleY == 0) {
            return false;
        } else if (scaleX != 1 || scaleY != 1) {
            g.translate(c.getWidth() / 2, c.getHeight() / 2);
            g.scale(scaleX, scaleY);
            g.translate(-c.getWidth() / 2, -c.getHeight() / 2);
        }

        return true;
    }

    public static double easeInCubic(double t) {
        return t * t * t;
    }

    public static double easeOutCubic(double t) {
        return (--t) * t * t + 1;
    }

    public static double easeOutQuint(double t) {
        return 1 + (--t) * t * t * t * t;
    }

    public static double easeInOutQuintInterpolator(double t) {
        return t < .5 ? 16 * t * t * t * t * t : 1 + 16 * (--t) * t * t * t * t;
    }
}
