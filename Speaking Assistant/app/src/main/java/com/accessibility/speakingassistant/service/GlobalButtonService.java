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


package com.accessibility.speakingassistant.service;

import com.App;
import com.accessibility.speakingassistant.R;
import com.accessibility.speakingassistant.app.CustomView;
import com.accessibility.speakingassistant.app.DrawLayout;
import com.accessibility.speakingassistant.overlay.TextToSpeechOverlay;
import com.accessibility.speakingassistant.summarization.control.Summary;
import com.accessibility.speakingassistant.util.BuildVersionUtils;
import com.accessibility.speakingassistant.util.WebContent;
import com.accessibility.speakingassistant.util.WebUtil;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import vn.hus.nlp.tokenizer.VietTokenizer;

import static android.util.Log.e;


public class GlobalButtonService extends AccessibilityService implements TextToSpeech.OnInitListener, View.OnTouchListener {
    private View mLayout;
    private View collapsedView;
    private View expandedView;
    private View backView;
    private ImageView imageView;

    private static GlobalButtonService mService;

    private Animation fab_open, fab_close;

    private CustomView customView;
    private DrawLayout drawLayout;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams params_draw_view;
    private WindowManager wm;

    private ArrayList<AccessibilityNodeInfoCompat> arrayListNode;
    private ArrayList<AccessibilityNodeInfoCompat> arrayListNodeAll;
    private ArrayList<AccessibilityNodeInfoCompat> arrayListNodeSpeech;
    private ArrayList<Rect> arrRect;
    private ArrayList<String> ansSummary;

    private ArrayList<String> listNodeTextAll;
    private ArrayList<String> listNodeText;
    private String nodeTextAll;
    private String textSummary;
    private int sumSummary;

    private float xCoOrdinate, yCoOrdinate;
    private float initialTouchX, initialTouchY;

    private float heightScreen, widthScreen;
    private int barHeight = 0;

    private String TAG = "Global_button";

    private TextToSpeech mTts;
    private boolean mTextToSpeechInitialized;
    private boolean mSpeech;
    private boolean mSpeechSummary;

    private boolean mDrawLayout;
    private boolean mCustomView;
    private Context mAppContex;
    private TextToSpeech.OnInitListener mServiceListener;
    private ArrayList<String> arrStopword;
    private Summary summary;
    private boolean tapAndSpeak =false;

    private HashMap <Integer, String> speakMap;

    private TextToSpeechOverlay mTTSOverlay;
    private Boolean switchTTSEnable;
    private SharedPreferences sharedPreferences;

    private static final int HIDE_CUSTOM_VIEW = 1;
    private static final int HIDE_BACK_VIEW = 2;
    private static final int SHOW_TTS = 3;
    private static final int HIDE_TTS = 4;

    private Handler customViewHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_CUSTOM_VIEW: {
                    if (mCustomView) {
                        customView.setVisibility(View.GONE);
                        customView.drawMode = -1;
                        tapAndSpeak = false;
                        mCustomView = false;
                    }
                } break;
                case HIDE_BACK_VIEW:
                    if (backView.isShown()) {
                        backView.startAnimation(fab_close);
                        backView.setVisibility(View.GONE);
                    }
                    if (!expandedView.isShown()) {
                        expandedView.startAnimation(fab_open);
                        expandedView.setVisibility(View.VISIBLE);
                    }

                    if (mSpeech == true) {
                        mSpeech = false;
                        mTts.stop();
                    }
                    if (mSpeechSummary == true){
                        mSpeechSummary = false;
                        mTts.stop();
                    }
                    if (mDrawLayout) {
                        drawLayout.setVisibility(View.GONE);
                        mDrawLayout = false;
                    }
                    break;
                case SHOW_TTS:
                    String speakText = (String) msg.obj;
                    Log.d(TAG,"handleMessage " + speakText);
                    if (speakText != null && switchTTSEnable) {
                        mTTSOverlay.speak(speakText);
                    }
                    break;
                case HIDE_TTS:
                    mTTSOverlay.hide();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mService = this;
    }

    @Override
    protected void onServiceConnected() {
        mAppContex = getApplicationContext();
        mServiceListener = this;
        mSpeech = false;
        mSpeechSummary = false;
        mTts = new TextToSpeech(getApplicationContext(), this);

        mTTSOverlay = new TextToSpeechOverlay(this);
//        resetTts();
        customView = new CustomView(getApplicationContext(), this);
        drawLayout = new DrawLayout(getApplicationContext(), new ArrayList<Rect>());
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        //mLayout = new View(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        mLayout = inflater.inflate(R.layout.button_layout, null);

        collapsedView = mLayout.findViewById(R.id.collapse_view);
        expandedView = mLayout.findViewById(R.id.expand_view);
        backView = mLayout.findViewById(R.id.back_view);
        imageView = mLayout.findViewById(R.id.back_btn);
        imageView.setColorFilter(getResources().getColor(R.color.icon_color));
//        insideView = mLayout.findViewById(R.id.inside_view);
        collapsedView.setVisibility(View.VISIBLE);
        expandedView.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        arrayListNode = new ArrayList<>();
        arrRect = new ArrayList<>();
        arrayListNodeAll = new ArrayList<>();
        arrayListNodeSpeech = new ArrayList<>();
        ansSummary = new ArrayList<>();

        listNodeTextAll = new ArrayList<>();
        listNodeText = new ArrayList<>();
        nodeTextAll = "";
        textSummary = "";
        sumSummary = 0;


        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.LEFT;

//        wm.addView(customView,params);

        params_draw_view = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        heightScreen = displayMetrics.heightPixels;
        widthScreen = displayMetrics.widthPixels;

        Log.e(TAG, heightScreen + "-" + widthScreen);

//        wm.addView(mLayout, params);
        wm.addView(drawLayout, params_draw_view);
        wm.addView(customView, params_draw_view);
//        wm.addView(floatLayout, params);
        wm.addView(mLayout, params);

        customView.setVisibility(View.GONE);
        drawLayout.setVisibility(View.GONE);
        mDrawLayout = false;
        mCustomView = false;
        Log.e(TAG, "Initial");
        configImg();
//        token();
        //summarization
        summary = new Summary(getApplicationContext());
        arrStopword = summary.readStopWord();

        final AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 100;
        info.flags |= AccessibilityServiceInfo.DEFAULT;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        setServiceInfo(info);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        switchTTSEnable = sharedPreferences.getBoolean("tts_output_checked", true);

    }

    public static GlobalButtonService getService(){
        return mService;
    }

    public void setTTSOutput(boolean isChecked){
        switchTTSEnable = isChecked;
    }

    private void resetTts() {
        mTts = new TextToSpeech(mAppContex, mServiceListener);
    }

    private void token() {

        VietTokenizer tokenizer = new VietTokenizer(getApplicationContext());

//        List<String> ans = tokenizer.tokenize("Đôi khi chúng ta không thể sắp xếp được theo như yêu cầu đề bài, vì vậy hãy in ra nếu không thể.");
//        for (String s : ans){
//            Log.e(TAG, s);
//        }


        Summary summary = new Summary(getApplicationContext());

        Log.e(TAG, "Text Summary" + textSummary);

        mSpeechSummary = true;
        ansSummary.clear();
        HashMap<String, String> map = new HashMap<>();

        if (textSummary.equals("")) {
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, 0 + "");
            mTts.speak("No text for summary.", TextToSpeech.QUEUE_FLUSH, map);
            speakMap.put(0, "No text for summary.");
            //Toast.makeText(this,"No text for summary!",Toast.LENGTH_LONG).show();
            //customViewHander.sendEmptyMessage(HIDE_BACK_VIEW);
        } else {
            ArrayList<String> arrStopWord = summary.readStopWord();
            ansSummary = summary.process(textSummary, arrStopword, 0);
            Log.e(TAG, "Summary " + ansSummary);
            WebUtil.LAST_TEXT = textSummary;
            WebUtil.LAST_CONTENT = ansSummary.toString();//for compare
           // mTts.speak("Bắt đầu", TextToSpeech.QUEUE_FLUSH, null);
            for (int i=0; i<ansSummary.size(); i++){
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, i + "");
                if (i==0){
                    mTts.speak(ansSummary.get(i), TextToSpeech.QUEUE_FLUSH, map);
                }else{
                    mTts.speak(ansSummary.get(i), TextToSpeech.QUEUE_ADD, map);
                }
                speakMap.put(i, ansSummary.get(i));
            }
//            mTts.speak(ansSummary, TextToSpeech.QUEUE_FLUSH, null);
        }

    }

    private void configImg() {

        Button btn = mLayout.findViewById(R.id.btn);

        btn.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int initialX = 0;
                int initialY = 0;


                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:

                        //get init pos para,
                        initialX = params.x;
                        initialY = params.y;

                        //get touch
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        xCoOrdinate = initialX - initialTouchX;
                        yCoOrdinate = initialY - initialTouchY;

//                        Log.e("IMG","DOWN" + initialTouchX + " " + initialTouchY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //Log.e(TAG, "OnListener");
                        params.x = (int) (event.getRawX() + xCoOrdinate);
                        params.y = (int) (event.getRawY() + yCoOrdinate);
                        //Log.e(TAG, "MOVE " +params.x + " " + params.y);
                        wm.updateViewLayout(mLayout, params);
                        break;
                    case MotionEvent.ACTION_UP:

//                        float x = (int) (event.getRawX() + xCoOrdinate);
//                        float y = (int) (event.getRawY() + yCoOrdinate);
//
//                        Log.e("IMG", initialTouchX + " " + initialTouchY);
//                        Log.e("IMG",event.getRawX() + " " + event.getRawY());

                        float x_tmp = Math.abs(event.getRawX() - initialTouchX);
                        float y_tmp = Math.abs(event.getRawY() - initialTouchY);

//                        Log.e("IMG", "UP " + x_tmp + " " + y_tmp);
                        if (x_tmp < 10 && y_tmp < 10) {
//                            Log.e("IMG", "no move"  );

                            collapsedView.startAnimation(fab_close);
                            collapsedView.setVisibility(View.GONE);
                            expandedView.startAnimation(fab_open);
                            expandedView.setVisibility(View.VISIBLE);
                            mTts = new TextToSpeech(getApplicationContext(), GlobalButtonService.this);

                        }
                    default:
                        return false;
                }
                return true;
            }
        });

        LinearLayout selectAll = mLayout.findViewById(R.id.select_all_btn);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                Log.e(TAG, "selectAll");
                if (mSpeech == false) {
                    mSpeech = true;

                    expandedView.startAnimation(fab_close);
                    expandedView.setVisibility(View.GONE);
                    backView.startAnimation(fab_open);
                    backView.setVisibility(View.VISIBLE);

                    getTextAll();
                    //  token();
//                    resetTts();
//                    mTts = new TextToSpeech(mAppContex, mServiceListener);
                }

            }
        });

        LinearLayout select = mLayout.findViewById(R.id.select_btn);
        select.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                customView.setVisibility(View.VISIBLE);
//                AccessibilityNodeInfoCompat viewDraw = new AccessibilityNodeInfoCompat(customView);
                //Rect r = new Rect();
                //viewDraw.getBoundsInScreen(r);
//                Log.d("Pos","--------" + viewDraw);
                mCustomView = true;
                mSpeech = true;

                expandedView.startAnimation(fab_close);
                expandedView.setVisibility(View.GONE);
                backView.startAnimation(fab_open);
                backView.setVisibility(View.VISIBLE);
                tapAndSpeak = true;
            }
        });

        LinearLayout exit = mLayout.findViewById(R.id.exit_btn);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.startAnimation(fab_open);
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.startAnimation(fab_close);
                expandedView.setVisibility(View.GONE);
                mTTSOverlay.hide();

//                            wm.removeView(customView);
//                            customView.setVisibility(View.GONE);
//                            customView.drawMode = -1;

                if (mDrawLayout) drawLayout.setVisibility(View.GONE);

                //customViewHander.sendEmptyMessage(HIDE_CUSTOM_VIEW);

                if (mSpeech == true) {
                    mSpeech = false;
                    mTts.stop();
                }
            }
        });

        LinearLayout summary = mLayout.findViewById(R.id.summary_btn);
        summary.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                expandedView.startAnimation(fab_close);
                expandedView.setVisibility(View.GONE);
                backView.startAnimation(fab_open);
                backView.setVisibility(View.VISIBLE);
                getSummary();
            }
        });

        final ImageView back = mLayout.findViewById(R.id.back_btn);
        back.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int initialX = 0;
                int initialY = 0;


                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:

                        //get init pos para,
                        initialX = params.x;
                        initialY = params.y;

                        //get touch
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        xCoOrdinate = initialX - initialTouchX;
                        yCoOrdinate = initialY - initialTouchY;

//                        Log.e("IMG","DOWN" + initialTouchX + " " + initialTouchY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //Log.e(TAG, "OnListener");
                        params.x = (int) (event.getRawX() + xCoOrdinate);
                        params.y = (int) (event.getRawY() + yCoOrdinate);
                        //Log.e(TAG, "MOVE " +params.x + " " + params.y);
                        wm.updateViewLayout(mLayout, params);
                        break;
                    case MotionEvent.ACTION_UP:

//                        float x = (int) (event.getRawX() + xCoOrdinate);
//                        float y = (int) (event.getRawY() + yCoOrdinate);
//
//                        Log.e("IMG", initialTouchX + " " + initialTouchY);
//                        Log.e("IMG",event.getRawX() + " " + event.getRawY());

                        float x_tmp = Math.abs(event.getRawX() - initialTouchX);
                        float y_tmp = Math.abs(event.getRawY() - initialTouchY);

//                        Log.e("IMG", "UP " + x_tmp + " " + y_tmp);
                        if (x_tmp < 10 && y_tmp < 10) {
//                            Log.e("IMG", "no move"  );
                            customViewHander.sendEmptyMessage(HIDE_CUSTOM_VIEW);
                            customViewHander.sendEmptyMessage(HIDE_BACK_VIEW);
                            customViewHander.sendEmptyMessage(HIDE_TTS);

                        }
                    default:
                        return false;
                }
                return true;
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void getTextAll() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            barHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ArrayList<AccessibilityNodeInfoCompat> listNode = getListNodeAll();
        arrayListNode.clear();
        arrayListNodeAll.clear();
        listNodeTextAll.clear();
        nodeTextAll = "";
        speakMap = new HashMap<>();
        if (listNode == null) return;
        Rect rect = new Rect();
        for (AccessibilityNodeInfoCompat nodeInfo : listNode) {
            Rect r = new Rect();
            nodeInfo.getBoundsInScreen(r);
            //Log.e(TAG, "Class: " + nodeInfo.getClassName() + nodeInfo.getChildCount() + " - Text: " + nodeInfo.getText() + " - Des: " + nodeInfo.getContentDescription());
            //Log.e(TAG, "Pos: " + r.top + " Bottom " + r.bottom + " Left " + r.left + " Right " + r.right);
            getNodeText(nodeInfo, rect, barHeight);
        }

        arrRect.clear();
        arrayListNodeSpeech.clear();
        //drawLayout.setVisibility(View.VISIBLE);
        //mDrawLayout = true;

        HashMap<String, String> map = new HashMap<>();
//            hashMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));

        for (AccessibilityNodeInfoCompat nodeInfo : arrayListNodeAll) {
            if (nodeInfo.getClassName().equals("android.widget.TextView") || nodeInfo.getClassName().equals("android.view.View") || nodeInfo.getClassName().equals("android.widget.EditText")) {
                if (nodeInfo.getText() != null || nodeInfo.getContentDescription() != null) {
                    Rect r = new Rect();
                    nodeInfo.getBoundsInScreen(r);
                    if (r.top != 0) r.top = r.top - barHeight;
                    r.bottom = r.bottom - barHeight;
                    arrRect.add(r);
                }
            }
        }

        int tmp = 0;
        for (AccessibilityNodeInfoCompat nodeInfo : arrayListNodeAll) {
            Rect r = new Rect();
            nodeInfo.getBoundsInScreen(r);

            if (r.top != 0) r.top = r.top - barHeight;
            r.bottom = r.bottom - barHeight;


            //speech
            if (nodeInfo.getClassName().equals("android.widget.TextView") || nodeInfo.getClassName().equals("android.view.View") || nodeInfo.getClassName().equals("android.widget.EditText")) {
                drawLayout.setRect(arrRect.get(0));
                String textNode = getText(nodeInfo);
                if (textNode != null) {
                    if (tmp == 0) {
//                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                        nodeInfo.performAction(AccessibilityNodeInfoCompat.FOCUS_ACCESSIBILITY);
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, tmp + "");
                        mTts.speak(textNode, TextToSpeech.QUEUE_FLUSH, map);
                        speakMap.put(tmp, textNode);
                        arrayListNodeSpeech.add(nodeInfo);
                        Log.e(TAG, "SPEECH /" + textNode+"/");
//                        nodeInfo.performAction(AccessibilityNodeInfoCompat.FOCUS_ACCESSIBILITY);
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    } else {
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, tmp + "");
                        mTts.speak(textNode, TextToSpeech.QUEUE_ADD, map);
                        speakMap.put(tmp, textNode);
                        arrayListNodeSpeech.add(nodeInfo);
                        Log.e(TAG, "SPEECH /" + textNode+"/");
                    }

                    tmp++;
                    mTts.playSilentUtterance(350, TextToSpeech.QUEUE_ADD, null);
                }
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void getSummary() {

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            barHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ArrayList<AccessibilityNodeInfoCompat> listNode = getListNodeAll();
        arrayListNode.clear();
        arrayListNodeAll.clear();
        listNodeTextAll.clear();
        nodeTextAll = "";
        speakMap = new HashMap<>();

        Rect rect = new Rect();
        textSummary = "";
        sumSummary = 0;
        if (listNode == null) return;
        for (AccessibilityNodeInfoCompat nodeInfo : listNode) {
            Rect r = new Rect();
            nodeInfo.getBoundsInScreen(r);
            //Log.e(TAG, "Class: " + nodeInfo.getClassName() + nodeInfo.getChildCount() + " - Text: " + nodeInfo.getText() + " - Des: " + nodeInfo.getContentDescription());
            //Log.e(TAG, "Pos: " + r.top + " Bottom " + r.bottom + " Left " + r.left + " Right " + r.right);
            getNodeText(nodeInfo, rect, barHeight);
        }

        arrRect.clear();
        arrayListNodeSpeech.clear();
//        drawLayout.setVisibility(View.VISIBLE);
//        mDrawLayout = true;

        HashMap<String, String> map = new HashMap<>();
//            hashMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));

        for (AccessibilityNodeInfoCompat nodeInfo : arrayListNodeAll) {
            if (nodeInfo.getClassName().equals("android.widget.TextView") || nodeInfo.getClassName().equals("android.view.View") || nodeInfo.getClassName().equals("android.widget.EditText")) {
                if (nodeInfo.getText() != null || nodeInfo.getContentDescription() != null) {
                    Rect r = new Rect();
                    nodeInfo.getBoundsInScreen(r);
                    if (r.top != 0) r.top = r.top - barHeight;
                    r.bottom = r.bottom - barHeight;
                    arrRect.add(r);
                }
            }
        }

        token();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void getTextFromTouch() {

        if (customView.drawMode == 3) {

            drawLayout.setVisibility(View.GONE);
            mDrawLayout = false;
            // int result = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                barHeight = getResources().getDimensionPixelSize(resourceId);
            }
//            Log.e(TAG, "TOP: " + result);

            Rect rect = customView.getRect();
            Log.e("Pos", "\tonEvent Top" + rect.top + " Bottom " + rect.bottom + " Left " + rect.left + " Right " + rect.right);

            ArrayList<AccessibilityNodeInfoCompat> listNode = getListNode(rect);
            arrayListNode.clear();
            arrayListNodeAll.clear();

            listNodeTextAll.clear();
            nodeTextAll = "";
            if (listNode == null) {
                return;
            }
            for (AccessibilityNodeInfoCompat nodeInfo : listNode) {
                Rect r = new Rect();
                nodeInfo.getBoundsInScreen(r);
//                Log.e(TAG, "Class: " + nodeInfo.getClassName() + nodeInfo.getChildCount() + " - Text: " + nodeInfo.getText() + " - Des: " + nodeInfo.getContentDescription());
//                Log.e(TAG, "Pos: " + r.top + " Bottom " + r.bottom + " Left " + r.left + " Right " + r.right);
                getNodeText(nodeInfo, rect, barHeight);
            }

            arrRect.clear();
            arrayListNodeSpeech.clear();
            drawLayout.setRect(null);
            drawLayout.setVisibility(View.VISIBLE);
            mDrawLayout = true;

            HashMap<String, String> map = new HashMap<>();
            mTts.speak("", TextToSpeech.QUEUE_FLUSH, map);
//            hashMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
            speakMap = new HashMap<>();

            int tmp = 0;
            for (AccessibilityNodeInfoCompat nodeInfo : arrayListNode) {
                Rect r = new Rect();
                nodeInfo.getBoundsInScreen(r);

                if (r.top != 0) r.top = r.top - barHeight;
                r.bottom = r.bottom - barHeight;


                String textNode = getText(nodeInfo);

                if (textNode != null) {
                    arrRect.add(r);
                    if (tmp == 0) {
                        drawLayout.setRect(r);
//                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
//                            nodeInfo.performAction(AccessibilityNodeInfoCompat.FOCUS_ACCESSIBILITY);
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, tmp + "");
                        mTts.speak(textNode, TextToSpeech.QUEUE_FLUSH, map);
                        arrayListNodeSpeech.add(nodeInfo);
                        Log.e(TAG, "SPEECH /" + textNode +"/");
//                        nodeInfo.performAction(AccessibilityNodeInfoCompat.FOCUS_ACCESSIBILITY);
//                            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    } else {

                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, tmp + "");
                        mTts.speak(textNode, TextToSpeech.QUEUE_ADD, map);
                        arrayListNodeSpeech.add(nodeInfo);
                        Log.e(TAG, "SPEECH /" + textNode +"/");
                    }
                    speakMap.put(tmp, textNode);
                    tmp++;
                    mTts.playSilentUtterance(350, TextToSpeech.QUEUE_ADD, null);
                }

            }
            if (tmp == 0) {
                //Toast.makeText(this,"No text for select!",Toast.LENGTH_LONG).show();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, 0 + "");
                speakMap.put(tmp, "No text for select.");
                mTts.speak("No text for select.", TextToSpeech.QUEUE_FLUSH, map);
                //customViewHander.sendEmptyMessage(HIDE_CUSTOM_VIEW);
                //customViewHander.sendEmptyMessage(HIDE_BACK_VIEW);

            }

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private ArrayList<AccessibilityNodeInfoCompat> getListNodeAll() {
        ArrayList<AccessibilityNodeInfoCompat> listNode = new ArrayList<>();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return null;
        AccessibilityNodeInfoCompat nodeWindow = new AccessibilityNodeInfoCompat(root);
        for (int i = 0; i < nodeWindow.getChildCount(); i++) {
            listNode.add(nodeWindow.getChild(i));
        }
        return listNode;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private ArrayList<AccessibilityNodeInfoCompat> getListNode(Rect rect) {
        ArrayList<AccessibilityNodeInfoCompat> listNode = new ArrayList<>();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return null;
        AccessibilityNodeInfoCompat nodeWindow = new AccessibilityNodeInfoCompat(root);
        for (int i = 0; i < nodeWindow.getChildCount(); i++) {
            Rect rectChild = new Rect();
            AccessibilityNodeInfoCompat childNode = nodeWindow.getChild(i);
            childNode.getBoundsInScreen(rectChild);
            if (rectChild.intersect(rect)) listNode.add(childNode);
        }
        return listNode;

    }

    private void getNodeText(AccessibilityNodeInfoCompat nodeSelect, Rect rect, int top) {
//        if (parentNode.getText() != null)   Log.e(TAG, parentNode.getClassName().toString() +"-" + parentNode.getText().toString());
//        else Log.e(TAG, parentNode.getClassName().toString() );
        if ( nodeSelect == null ||  nodeSelect.getClassName() == null) return;
        int childCount =  nodeSelect.getChildCount();
        //check Duplicate
        String nodeText = getText( nodeSelect);
        AccessibilityNodeInfoCompat parentView =  nodeSelect.getParent();
        String parentText = getText( parentView);
        boolean notDuplicate = nodeText != null && !(parentText != null && nodeText.equals(parentText));
        //
        if (!WebUtil.hasNativeWebContent( nodeSelect) && ! nodeSelect.getClassName().equals("android.webkit.WebView") &&
                /*(nodeSelect.getClassName().equals("android.widget.TextView") ||
                 nodeSelect.getClassName().equals("android.widget.EditText") ||*/
                notDuplicate) {
            Rect r = new Rect();
             nodeSelect.getBoundsInScreen(r);
            r.top = r.top - top;
            r.bottom = r.bottom - top;
            if (r.intersect(rect)) {
                arrayListNode.add( nodeSelect);
                listNodeText.add(getText( nodeSelect));
            }
            arrayListNodeAll.add( nodeSelect);
            listNodeTextAll.add(getText( nodeSelect));

            // parentNode.performAction(AccessibilityNodeInfoCompat.FOCUS_ACCESSIBILITY)
//            return;
        }

        if (WebUtil.hasNativeWebContent( nodeSelect) && getText( nodeSelect) != null) {
            String textNode = getText( nodeSelect);
            String[] dauGach = textNode.split("-");
            String[] dauBang = textNode.split("=");
            if (dauGach.length < 5 && dauBang.length < 5 && textNode != null
                    && ((textNode.length() > 20 && !listNodeTextAll.contains(textNode) && !nodeTextAll.contains(textNode)) || tapAndSpeak)
                    ) {
                Rect r = new Rect();
                 nodeSelect.getBoundsInScreen(r);
                r.top = r.top - top;
                r.bottom = r.bottom - top;
                if (r.intersect(rect)) {
                    arrayListNode.add( nodeSelect);
                    listNodeText.add(getText( nodeSelect));
                }
                arrayListNodeAll.add( nodeSelect);
                listNodeTextAll.add(getText( nodeSelect));
                nodeTextAll += textNode + ". ";
                if (sumSummary <= 30) {
                    textSummary += textNode + ". ";
                    sumSummary++;
                }
                return;
            }
        }

        for (int i = 0; i < childCount; i++) {
            getNodeText( nodeSelect.getChild(i), rect, top);
        }

    }

    private String getText(AccessibilityNodeInfoCompat node) {
        String textNode = null;
        if (node == null) return null;
        if (node.getText() != null && WebContent.checkTextEmpty(node.getText().toString()))
            textNode = node.getText().toString();
        if (node.getContentDescription() != null && WebContent.checkTextEmpty(node.getContentDescription().toString()))
            textNode = node.getContentDescription().toString();
        if (textNode!=null) textNode = WebContent.checkTextTwice(textNode);//for duplicate text
        return textNode;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int evenType = event.getEventType();
        Log.d(TAG, "event " + event);
        switch (evenType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                AccessibilityNodeInfo source = event.getSource();
                if (source != null && source.getText() != null) {
                    String text = source.getText() + "";
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (event.getPackageName()!= null && !event.getPackageName().toString().equals(App.PACKAGE_NAME)) {
                    if (expandedView.getVisibility() == View.VISIBLE){
                        mLayout.findViewById(R.id.exit_btn).performClick();
                    }
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {
        if (mTextToSpeechInitialized) {
            mTts.stop();
            mTts.shutdown();
        }
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            mTextToSpeechInitialized = true;
            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {
                    final String speakText = speakMap.get(Integer.parseInt(s));
                    Log.d(TAG,"onStart " + speakText);
                    Message msg = Message.obtain(customViewHander, SHOW_TTS, speakText);
                    customViewHander.sendMessage(msg);
                }

                @Override
                public void onDone(String s) {
                    if (mSpeech) {
//                        Log.e("Draw", "Con");
                        int i = Integer.parseInt(s) + 1;

                        if (i < arrayListNodeSpeech.size()) {
                            AccessibilityNodeInfoCompat node = arrayListNodeSpeech.get(i);
                            node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                            Rect r = new Rect();
                            node.getBoundsInScreen(r);
                            if (r.top != 0) r.top = r.top - barHeight;
                            r.bottom = r.bottom - barHeight;
                            drawLayout.setRect(r);

                        } else {
                            Log.e("Draw", "Done");
                            drawLayout.setRect(null);
                            if (tapAndSpeak) {
                                customViewHander.sendEmptyMessage(HIDE_CUSTOM_VIEW);
                            }
                            customViewHander.sendEmptyMessage(HIDE_BACK_VIEW);


                        }
                    }else if(mSpeechSummary){
                        int i = Integer.parseInt(s) + 1;
                        if (i >= ansSummary.size()) {
                            customViewHander.sendEmptyMessage(HIDE_BACK_VIEW);
                        }
                    }
                    customViewHander.sendEmptyMessage(HIDE_TTS);
                }

                @Override
                public void onError(String s) {
                    Log.e(TAG, "onError "+ s);
                }
            });


        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTextToSpeechInitialized) {
            mTts.stop();
            mTts.shutdown();
        }

        mService = null;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}
