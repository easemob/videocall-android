package com.src.videocall.easemobvideocall.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.media.EMCallSurfaceView;
import com.src.videocall.easemobvideocall.R;
import com.src.videocall.easemobvideocall.utils.ConferenceInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 作为RecyclerView Adapter的基类，有默认空白布局
 * 如果要修改默认布局可以采用以下两种方式：1、在app layout中新建ease_layout_default_no_data.xml覆盖。
 * 2、继承EaseBaseRecyclerViewAdapter后，重写getEmptyLayoutId()方法，返回自定义的布局即可。
 * @param <T>
 */
public abstract class EaseBaseRecyclerViewAdapter<T> extends EaseBaseAdapter<EaseBaseRecyclerViewAdapter.ViewHolder> {
    private static final int VIEW_TYPE_EMPTY = 1;
    private static final int VIEW_TYPE_ITEM = 0;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemGetSurfaceView  mOnItemGetSurfaceView;
    public Context mContext;
    public List<T> mData;
    private EMCallSurfaceView SurfaceView;
    private boolean fristFlag = false;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if(viewType == VIEW_TYPE_EMPTY) {
            return getEmptyViewHolder(parent);
        }
        return getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull EaseBaseRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.setAdapter(this);
        if(mData == null || mData.isEmpty()) {
            return;
        }
        T item = mData.get(position);
        holder.setData(item, position);
        holder.setDataList(mData, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickAction(v, position);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return itemLongClickAction(v, position);
            }
        });
    }

    public boolean itemLongClickAction(View v, int position) {
        if(mOnItemLongClickListener != null) {
            return mOnItemLongClickListener.onItemLongClick(v,position);
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return (mData == null || mData.isEmpty()) ? 1 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (mData == null || mData.isEmpty()) ? VIEW_TYPE_EMPTY : VIEW_TYPE_ITEM;
        //return position;
    }


    /**
     * 点击事件
     * @param v
     * @param position
     */
    public void itemClickAction(View v, int position) {
        if(mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, position);
        }
    }


    /**
     * 返回数据为空时的布局
     * @param parent
     * @return
     */
    private ViewHolder getEmptyViewHolder(ViewGroup parent) {
        View emptyView = getEmptyView(parent);
        return new ViewHolder<T>(emptyView) {
            @Override
            public void initView(View itemView) {

            }
            @Override
            public void setData(T item, int position) {

            }

        };
    }

    /**
     * 获取空白布局
     * @param parent
     * @return
     */
    private View getEmptyView(ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(getEmptyLayoutId(), parent, false);
    }

    /**
     * 获取ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    public abstract ViewHolder getViewHolder(ViewGroup parent, int viewType);

    /**
     * 根据position获取相应的data
     * @param position
     * @return
     */
    public T getItem(int position) {
        return mData.get(position);
    }

    /**
     * 添加数据
     * @param data
     */
    public void setData(List<T> data) {
        this.mData = data;
        notifyDataSetChanged();
    }


    /**
     * 添加单个数据
     * @param item
     */
    public void addData(T item) {
        synchronized (EaseBaseRecyclerViewAdapter.class) {
            if(this.mData == null) {
                this.mData = new ArrayList<>();
            }
            this.mData.add(item);
        }
        //notifyDataSetChanged();
        //notifyItemChanged(this.mData.size());
        notifyItemInserted(this.mData.size());
        notifyItemRangeChanged(this.mData.size()-1, this.mData.size());
    }


    /**
     * 删除单个数据
     * @param item
     */
    public void removeData(T item) {
        int position = this.mData.indexOf(item);
        synchronized (EaseBaseRecyclerViewAdapter.class) {
            if(mData.contains(item)){
                this.mData.remove(item);
            }
        }
        notifyItemRemoved(position);
        if(position < mData.size()){
            notifyItemRangeChanged(position,getItemCount()); //刷新被删除数据，以及其后面的数据
        }
    }

    /**
     * 更新单个数据
     *
     */
    public void updataData() {
        notifyDataSetChanged();
    }


    /**
     * 添加更多数据
     * @param data
     */
    public void addData(List<T> data) {
        synchronized (EaseBaseRecyclerViewAdapter.class) {
            if(data == null || data.isEmpty()) {
                return;
            }
            if(this.mData == null) {
                this.mData = data;
            }else {
                this.mData.addAll(data);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 获取数据
     * @return
     */
    public List<T> getData() {
        return mData;
    }

    /**
     * 清除数据
     */
    public void clearData() {
        if(mData != null) {
            mData.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * set item click
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }


    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        private EaseBaseAdapter adapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initView(itemView);
        }


        /**
         * 初始化控件
         * @param itemView
         */
        public abstract void initView(View itemView);


        /**
         * 设置数据
         * @param item
         * @param position
         */
        public abstract void setData(T item, int position);


        /**
         * @param id
         * @param <E>
         * @return
         */
        public  <E extends View> E findViewById(@IdRes int id) {
            return this.itemView.findViewById(id);
        }

        /**
         * 设置数据，提供数据集合
         * @param data
         * @param position
         */
        public void setDataList(List<T> data, int position) { }

        /**
         * 设置 adapter
         * @param adapter
         */
        private void setAdapter(EaseBaseRecyclerViewAdapter adapter) {
            this.adapter = adapter;
        }

        /**
         * get adapter
         * @return
         */
        public EaseBaseAdapter getAdapter() {
            return adapter;
        }
    }

    /**
     * 返回空白布局
     * @return
     */
    public int getEmptyLayoutId() {
        return R.layout.activity_conference_member_view;
    }
}
