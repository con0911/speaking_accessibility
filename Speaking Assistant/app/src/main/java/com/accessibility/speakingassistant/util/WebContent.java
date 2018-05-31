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

public class WebContent {

    private static String listSkip = "[+/=%!@#$%^*()><,./?:;\\\"\\'\\{\\[\\]\\}|\\\\”“’‘•]";



    public static String checkTextTwice(String text){//avoid duplicate text
        int length = text.length();
        if (length < 4) return text;
        String textLeft = "";
        String textRight = "";
        if (text.length()%2 == 0){
            textLeft = text.substring(0, length/2).trim();
            textRight = text.substring(length/2, text.length()).trim();
        }else {
            textLeft = text.substring(0, (length-1)/2).trim();
            textRight = text.substring((length+1)/2, text.length()).trim();
        }
        if (textLeft.equals(textRight)) return textLeft;
        return text.trim();
    }

    public static boolean checkTextEmpty(String text) {//avoid duplicate text
        if (text == null) return false;
        if (text.isEmpty()) return false;
        if (text.trim().isEmpty()) return false;
        return true;
    }
}
