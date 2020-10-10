package com.easemob.videocall.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.easemob.videocall.utils.X5WebView;

public class TouchWebView extends X5WebView {
    private OnTouchScreenListener onTouchScreenListener;

    public TouchWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public TouchWebView(Context context) {
        super(context);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (onTouchScreenListener != null)
                onTouchScreenListener.onTouchScreen();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (onTouchScreenListener != null)
                onTouchScreenListener.onReleaseScreen();
        }
        return super.dispatchTouchEvent(event);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            if (onTouchScreenListener != null)
//                onTouchScreenListener.onTouchScreen();
//        }
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            if (onTouchScreenListener != null)
//                onTouchScreenListener.onReleaseScreen();
//        }
//
//        return super.onTouchEvent(event);
//    }

    public interface OnTouchScreenListener {
        void onTouchScreen();

        void onReleaseScreen();
    }

    public void setOnTouchScreenListener(OnTouchScreenListener onTouchScreenListener) {
        this.onTouchScreenListener = onTouchScreenListener;
    }

}
