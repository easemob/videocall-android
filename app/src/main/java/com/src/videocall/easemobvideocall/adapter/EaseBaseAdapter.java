package com.src.videocall.easemobvideocall.adapter;

import android.support.v7.widget.RecyclerView;

public abstract class EaseBaseAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * Get the data item associated with the specified position in the data set.
     * @param position
     * @return
     */
    public abstract Object getItem(int position);
}