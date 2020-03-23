package com.easemob.videocall.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.easemob.videocall.R;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class HeadImageItemAdapter extends EaseBaseRecyclerViewAdapter<String> {
    public static int chooseIndex = -1;
    public HeadImageItemAdapter(){
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_headimage_content, parent, false);
        return new AvatarViewHolder(view);
    }

    private class AvatarViewHolder extends ViewHolder<String> {
        private ImageView headImage_view;
        private RadioButton id_checkbox;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            headImage_view = (ImageView) findViewById(R.id.headImage_avatar);
            id_checkbox = (RadioButton) findViewById(R.id.headImage_checkbox);
            chooseIndex = -1;
        }

        @Override
        public void setData(String imageStr, int position) {
            headImage_view.setImageResource(R.drawable.systemlogo1);
            if (position == chooseIndex) {
                id_checkbox.setChecked(true);
            } else {
                id_checkbox.setChecked(false);
            }
        }
    }
}
