package com.src.videocall.easemobvideocall.adapter;

import android.view.SurfaceView;
import android.view.View;

import com.hyphenate.media.EMCallSurfaceView;

/**
 * 条目长按点击事件
 */
public interface OnItemGetSurfaceView {
    /**
     * 条目点击
     * @param surfaceView
     */
     void OnItemGetSurfaceView(EMCallSurfaceView surfaceView ,int position);
}

