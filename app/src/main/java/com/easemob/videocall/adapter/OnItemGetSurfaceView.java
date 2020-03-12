package com.easemob.videocall.adapter;

import android.widget.ImageView;

import com.hyphenate.media.EMCallSurfaceView;

/**
 * 条目长按点击事件
 */
public interface OnItemGetSurfaceView {
    /**
     * 条目点击
     * @param surfaceView
     */
     void OnItemGetSurfaceView(EMCallSurfaceView surfaceView , int position , ImageView imageView);
}

