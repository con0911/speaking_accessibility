/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.accessibility.speakingassistant.util;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

public abstract class WeakReferenceHandler<T> extends Handler {
    private final WeakReference<T> mParentRef;

    /**
     * Constructs a new {@link WeakReferenceHandler} with a reference to its parent class.
     *
     * @param parent The handler's parent class.
     */
    public WeakReferenceHandler(T parent) {
        mParentRef = new WeakReference<>(parent);
    }

    /**
     * Constructs a new {@link WeakReferenceHandler} with a reference to its parent class.
     *
     * @param parent The handler's parent class.
     * @param looper The looper.
     */
    public WeakReferenceHandler(T parent, Looper looper) {
        super(looper);
        mParentRef = new WeakReference<>(parent);
    }

    @Override
    public final void handleMessage(Message msg) {
        final T parent = getParent();

        if (parent == null) {
            return;
        }

        handleMessage(msg, parent);
    }

    /** @return The parent class, or {@code null} if the reference has been cleared. */
    protected T getParent() {
        return mParentRef.get();
    }

    /** Subclasses must implement this to receive messages. */
    protected abstract void handleMessage(Message msg, T parent);
}