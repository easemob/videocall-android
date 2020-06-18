package com.easemob.videocall.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

public class MyWebView extends WebView {

    private String whiteboardUrl;
    private String whiteboardRoomId;
    private boolean iswhiteboardCreator;

    public MyWebView(Context context, AttributeSet attrs, int defStyle,
                     boolean privateBrowsing) {
        super(context, attrs, defStyle, privateBrowsing);
        // TODO Auto-generated constructor stub
    }

    public MyWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public MyWebView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        Log.d("touchevent", "touchevent"+super.onTouchEvent(ev));
        Intent intent = new Intent(this.getContext(), WhiteBoardActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("roomId", getWhiteboardRoomId());
        bundle.putString("roomUrl", getWhiteboardUrl());
        bundle.putBoolean("creator", isIswhiteboardCreator());
        intent.putExtras(bundle);
        this.getContext().startActivity(intent);
        return false;
    }

    public String getWhiteboardUrl() {
        return whiteboardUrl;
    }

    public void setWhiteboardUrl(String whiteboardUrl) {
        this.whiteboardUrl = whiteboardUrl;
    }

    public String getWhiteboardRoomId() {
        return whiteboardRoomId;
    }

    public void setWhiteboardRoomId(String whiteboardRoomId) {
        this.whiteboardRoomId = whiteboardRoomId;
    }

    public boolean isIswhiteboardCreator() {
        return iswhiteboardCreator;
    }

    public void setIswhiteboardCreator(boolean iswhiteboardCreator) {
        this.iswhiteboardCreator = iswhiteboardCreator;
    }
}
