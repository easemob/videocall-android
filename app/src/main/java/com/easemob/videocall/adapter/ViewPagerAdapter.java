package com.easemob.videocall.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private List<View> views;
    private OnItemClickListener clickListener = null;
    public ViewPagerAdapter( List<View> views,Context context) {
        this.context = context;
        this.views = views;

    }

    public void setListView(List<View> views){
        this.views = views;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        clickListener = listener;
    }

    //重新4个方法
//getCount()返回List<View>的size:
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return views.size();
    }
    //instantiateItem()：将当前view添加到ViewGroup中，并返回当前View
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = views.get(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null){
                    clickListener.onItemClick(view,position);
                }
            }
        });
        container.addView(views.get(position));
        return view;
    }
    //destroyItem()：删除当前的View;
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }
    //isViewFromObject判断当前的View 和 我们想要的Object(值为View) 是否一样;返回 trun / false;
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return (arg0 == arg1);
    }
}
