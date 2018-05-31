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

package com.accessibility.speakingassistant.summarization.model;

import java.util.ArrayList;

public class Document {
    private int numberSentence;
    private ArrayList<Sentence> arrS;
    private ArrayList<Word> arrWordAll;

    public Document() {
        super();
    }

    public Document(int numberSentence, ArrayList<Sentence> arrS, ArrayList<Word> arrWordAll) {
        super();
        this.numberSentence = numberSentence;
        this.arrS = arrS;
        this.arrWordAll = arrWordAll;
    }

    public ArrayList<Word> getArrWordAll() {
        return arrWordAll;
    }

    public void setArrWordAll(ArrayList<Word> arrWordAll) {
        this.arrWordAll = arrWordAll;
    }

    public int getNumberSentence() {
        return numberSentence;
    }

    public void setNumberSentence(int numberSentence) {
        this.numberSentence = numberSentence;
    }

    public ArrayList<Sentence> getArrS() {
        return arrS;
    }

    public void setArrS(ArrayList<Sentence> arrS) {
        this.arrS = arrS;
    }
}