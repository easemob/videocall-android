package com.easemob.videocall.adapter;
import android.view.View;


/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

/**
 * 条目长按点击事件
 */
public interface OnItemLongClickListener {
    /**
     * 条目点击
     * @param view
     * @param position
     */
    boolean onItemLongClick(View view, int position);
}
