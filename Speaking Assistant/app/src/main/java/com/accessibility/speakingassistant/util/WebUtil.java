/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

public class WebUtil {

    public static String LAST_TEXT = "";
    public static String LAST_CONTENT = "";

    public static boolean hasNativeWebContent(AccessibilityNodeInfoCompat node) {
        if (node == null) return false;
        if (!supportsWebActions(node)) return false;
        //Chrome vox does not have sub elements
        AccessibilityNodeInfoCompat parent = node.getParent();
        if (supportsWebActions(parent)) {
            if (parent != null) parent.recycle();
            return true;
        }
        if (parent != null) parent.recycle();
        return node.getChildCount() == 0;
    }

    //determines whether or not the given node contains web content
    public static boolean supportsWebActions(AccessibilityNodeInfoCompat node) {
        return supportsAnyAction(node,
                AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT,
                AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT);
    }

    //have at least one of the specified actions
    public static boolean supportsAnyAction(AccessibilityNodeInfoCompat node, int... actions) {
        if (node != null) {
            int supportedActions = node.getActions();
            for (int action : actions) {
                if ((supportedActions & action) == action) return true;
            }
        }
        return false;
    }

}
