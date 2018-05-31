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

package com.accessibility.speakingassistant.app;

import com.accessibility.speakingassistant.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class DrawLayout extends View {

    private Context mContext;
    private ArrayList<Rect> arrRect;
    private Rect rect;

    private Paint paint;
    private Paint fillPaint;
    private int STROKE_WIDTH = 5;



    public DrawLayout(Context context, ArrayList<Rect> arrRect) {
        super(context);
        mContext = context;
        this.arrRect = arrRect;
        rect = new Rect();
        Init();
        postInvalidate();
    }

    private void Init(){
        //stroke
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setColor(Color.MAGENTA);

        int color = ContextCompat.getColor(mContext, R.color.fillarea_color);

        //fill
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(color);
    }

    public void setArrayListRect(ArrayList<Rect> arrRect){
        this.arrRect = arrRect;
        postInvalidate();
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
        postInvalidate();
//        Log.e("Global_button","Draw");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        for (Rect rect : arrRect){
//            canvas.drawRect(rect,paint);
//            canvas.drawRect(rect,fillPaint);
//        }
        if (rect != null) {
            Log.e("Draw", rect.toString());
            canvas.drawRect(rect, paint);
            canvas.drawRect(rect, fillPaint);
        }
    }
}
