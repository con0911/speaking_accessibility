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

public class Word {
    private String w;
    private int f;// frequent
    private int d;// idf
    private double tfidf;

    public Word() {
    }

    public Word(String w, int f, double tfidf) {
        this.w = w;
        this.f = f;
        this.tfidf = tfidf;
    }

    public Word(String w, int f) {
        this.w = w;
        this.f = f;
    }

    public Word(String w, int f, int d) {
        this.w = w;
        this.f = f;
        this.d = d;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public double getTfidf() {
        return tfidf;
    }

    public void setTfidf(double tfidf) {
        this.tfidf = tfidf;
    }

}
