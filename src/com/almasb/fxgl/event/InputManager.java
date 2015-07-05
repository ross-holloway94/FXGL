/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.almasb.fxgl.event;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public final class InputManager {

    /**
     * Holds mouse state information
     */
    private Mouse mouse = new Mouse();

    private Map<KeyCode, Boolean> keys = new HashMap<>();
    private Map<KeyCode, Runnable> keyPressActions = new HashMap<>();
    private Map<KeyCode, Runnable> keyTypedActions = new HashMap<>();

    public void init(Scene mainScene) {
        mainScene.setOnKeyPressed(event -> {
            if (!isPressed(event.getCode()) && keyTypedActions.containsKey(event.getCode())) {
                keys.put(event.getCode(), true);
                keyTypedActions.get(event.getCode()).run();
            }
            else {
                keys.put(event.getCode(), true);
            }

        });
        mainScene.setOnKeyReleased(event -> keys.put(event.getCode(), false));

        mainScene.setOnMousePressed(mouse::update);
        mainScene.setOnMouseDragged(mouse::update);
        mainScene.setOnMouseReleased(mouse::update);
        mainScene.setOnMouseMoved(mouse::update);
    }

    /**
     * Do NOT call manually. Called by FXGL GameApplication
     *
     * @param now
     */
    public void onUpdate(long now) {
        keyPressActions.forEach((key, action) -> {if (isPressed(key)) action.run();});
    }

    /**
     * @param key
     * @return
     *          true iff key is currently pressed
     */
    private boolean isPressed(KeyCode key) {
        return keys.getOrDefault(key, false);
    }

    /**
     * Add an action that is executed constantly
     * WHILE the key is physically pressed
     *
     * @param key
     * @param action
     */
    public void addKeyPressBinding(KeyCode key, Runnable action) {
        keyPressActions.put(key, action);
    }

    /**
     * Add an action that is executed only ONCE
     * per single physical key press
     *
     * @param key
     * @param action
     */
    public void addKeyTypedBinding(KeyCode key, Runnable action) {
        keyTypedActions.put(key, action);
    }

    public void clearAllInput() {
        keys.keySet().forEach(key -> keys.put(key, false));
        mouse.leftPressed = false;
        mouse.rightPressed = false;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public static class Mouse {
        /**
         * Hold the value of x and y coordinate of the mouse cursor
         * in the current frame (tick)
         */
        public double x, y;

        /**
         * Hold the state of left and right
         * mouse buttons in the current frame (tick)
         */
        public boolean leftPressed, rightPressed;
        private MouseEvent event;

        private void update(MouseEvent event) {
            this.event = event;
            this.x = event.getSceneX();
            this.y = event.getSceneY();
            if (leftPressed) {
                if (event.getButton() == MouseButton.PRIMARY && isReleased(event)) {
                    leftPressed = false;
                }
            }
            else {
                leftPressed = event.getButton() == MouseButton.PRIMARY && isPressed(event);
            }

            if (rightPressed) {
                if (event.getButton() == MouseButton.SECONDARY && isReleased(event)) {
                    rightPressed = false;
                }
            }
            else {
                rightPressed = event.getButton() == MouseButton.SECONDARY && isPressed(event);
            }
        }

        private boolean isPressed(MouseEvent event) {
            return event.getEventType() == MouseEvent.MOUSE_PRESSED
                    || event.getEventType() == MouseEvent.MOUSE_DRAGGED;
        }

        private boolean isReleased(MouseEvent event) {
            return event.getEventType() == MouseEvent.MOUSE_RELEASED
                    || event.getEventType() == MouseEvent.MOUSE_MOVED;
        }

        public MouseEvent getEvent() {
            return event;
        }
    }
}