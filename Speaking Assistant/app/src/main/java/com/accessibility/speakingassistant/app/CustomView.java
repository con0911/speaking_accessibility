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
import com.accessibility.speakingassistant.service.GlobalButtonService;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class CustomView extends View {

    private Context mcontext;
    private View currentView;


    private Paint paint = null;
    private Paint fillPaint = null;
    private Bitmap resizeRect = null;
    private int resizeWidth = 0;
    private int resizeHeight = 0;

    private ArrayList<Point> arrPoint;//store all point
    private Rect rect;//store rect
    private int focusRect = -1; // store index of rect focus
    private int DRAW_POINT_MODE = 1;
    private int DRAW_DOWN_MODE = 2;
    private int DRAW_RECT_MODE = 3;
    private boolean hadRect;
    public int drawMode = -1;

    private Resources resources;
    private Canvas canvas_;

    private int STROKE_WIDTH = 5;

    private GlobalButtonService globalButtonService;


    public CustomView(Context context, GlobalButtonService globalButtonService) {
        super(context);
        mcontext = context;
        this.globalButtonService = globalButtonService;
        currentView = this;
        resources = context.getResources();
        arrPoint = new ArrayList<>();
        rect = new Rect(0,0,100,100);
        drawMode = DRAW_RECT_MODE;
        postInvalidate();
        hadRect = false;
        Init();
    }

    private void Init() {

        //stroke
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setColor(Color.MAGENTA);

        int color = ContextCompat.getColor(mcontext, R.color.fillarea_color);

        //fill
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(color);
        drawMode = 0;

        //bitmap
//        int width_height_size = getResources().getDimensionPixelSize(R.dimen.width_height_size);
//        resizeWidth = resizeHeight = width_height_size;
//        resizeRect = Bitmap.createBitmap(width_height_size,width_height_size,Bitmap.Config.ARGB_8888);
//
//        canvas_ = new Canvas(resizeRect);
        //canvas_.drawBitmap(resizeRect, 100, 100, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Log.e("DRAW","1");

        if (drawMode == DRAW_DOWN_MODE) {


            //canvas.drawRect(rectanglle, paint);
        } else if (drawMode == DRAW_POINT_MODE) {
            for (int i = 0; i < arrPoint.size(); i++) {
                Point p = arrPoint.get(i);
                if (i == 0) {
                    canvas.drawPoint(p.x, p.y, paint);
                } else {
                    Point p_pre = arrPoint.get(i - 1);
                    canvas.drawLine(p.x, p.y, p_pre.x, p_pre.y, paint);
                }
            }

        } else if (drawMode == DRAW_RECT_MODE){
            canvas.drawRect(rect,paint);
            canvas.drawRect(rect,fillPaint);
        }

        Bitmap button = Bitmap.createBitmap(70,70,Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(button);
        canvas.drawBitmap(button,100,100,paint);
        c.drawRect(200,200,300,300,paint);
//        canvas_.drawBitmap(resizeRect, 50, 50, paint);
//
//        if (focusRect != -1 && focusRect < arrRect.size())  drawButtonResize(canvas_, arrRect.get(0));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pos[] = new int[2];
        currentView.getLocationOnScreen(pos);
        int viewX = pos[0];
        int viewY = pos[1];
        int action = event.getAction();
        int x = (int) event.getRawX() - viewX;
        int y = (int) event.getRawY() - viewY;


        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                //  rectanglle = new Rect(x, y, x+50, y+50);
                drawMode = DRAW_DOWN_MODE;
                postInvalidate();
//                Log.e("EVENT", "DOWN " + x+" " + y);
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.e("EVENT","MOVE " + x + "-" + y );
                Point p = new Point(x, y);
                arrPoint.add(p);
                drawMode = DRAW_POINT_MODE;
                hadRect = true;
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
//                Log.e("EVENT", "UP");
                if (hadRect) {
                    getRectFromPoint(arrPoint);
                }
                postInvalidate();
                break;
            default:
                return false;
        }
        return true;

        //** canvas1.drawRect( X-1.25f, Y-1.25f, 2.5F, 2.5f, paint );

//        return super.onTouchEvent(event);
    }


//    private void getText(){
//        AccessibilityNodeInfo accessibilityNodeInfo = getRootInAc
//    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean getRectFromPoint(ArrayList<Point> arrPoint) {
        if (arrPoint.size() == 0) return false;
        else {
            int rectTop = arrPoint.get(0).y;
            int rectBottom = arrPoint.get(0).y;
            int rectLeft = arrPoint.get(0).x;
            int rectRight = arrPoint.get(0).x;

            for (Point p : arrPoint) {
                if (rectTop > p.y) rectTop = p.y;
                else if (rectBottom < p.y) rectBottom = p.y;
                if (rectLeft > p.x) rectLeft = p.x;
                else if (rectRight < p.x) rectRight = p.x;
            }
            if (rectLeft < 0) rectLeft=0;
            if (rectTop < 0) rectTop=STROKE_WIDTH;
            rect = new Rect(rectLeft, rectTop, rectRight, rectBottom);
//            Log.e("Global_button", "Draw rect:" + "Top: " + rect.top+ " Bottom: "+ rect.bottom +" Right: "+ rect.right + " Left: "+ rect.left);
            drawMode = DRAW_RECT_MODE;
            globalButtonService.getTextFromTouch();

            arrPoint.clear();
            return true;
        }
    }

    public Rect getRect() {
        return rect;
    }

}
