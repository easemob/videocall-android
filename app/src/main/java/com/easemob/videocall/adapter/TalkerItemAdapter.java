package com.easemob.videocall.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.videocall.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceStream;
import com.easemob.videocall.ui.widget.EaseImageView;
import com.easemob.videocall.utils.ConferenceInfo;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class TalkerItemAdapter extends EaseBaseRecyclerViewAdapter<EMConferenceStream> {

    public TalkerItemAdapter() {
    }
    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.content_talker_list, parent, false);
        return new AvatarViewHolder(view);
    }

    private class AvatarViewHolder extends ViewHolder<EMConferenceStream> {
        private TextView userIdView;
        private ImageView mic_image;
        private ImageView video_image;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
             userIdView = (TextView)findViewById(R.id.userId_view);
             mic_image  = (ImageView)findViewById(R.id.mic_image);
             video_image = (ImageView)findViewById(R.id.video_image);
        }


        @Override
        public void setData(EMConferenceStream item, int position) {

            String fristStr =  item.getUsername().substring(0,4);
            String lastStr =  item.getUsername().substring(item.getUsername().length()-4);
            fristStr = fristStr+"***"+lastStr;
            if(item.getUsername() == EMClient.getInstance().getCurrentUser()){
                if(item.getUsername().equals(ConferenceInfo.getInstance().getAdmin())){
                    userIdView.setText(fristStr + " (我)" + " (管理员)");
                }else {
                    userIdView.setText(fristStr + " (我)");
                }
            }else {
                if(item.getUsername().equals(ConferenceInfo.getInstance().getAdmin())){
                    userIdView.setText(fristStr +" (管理员)");
                }else {
                    userIdView.setText(fristStr);
                }
            }
            if(item.isAudioOff()){
                mic_image.setVisibility(GONE);
            }else{
                mic_image.setVisibility(VISIBLE);
                mic_image.setBackgroundResource(R.drawable.em_audio_show);
            }
            if(item.isVideoOff()){
                //video_image.setBackgroundResource(R.drawable.em_call_video_off);
                video_image.setVisibility(GONE);
            }else {
                video_image.setVisibility(VISIBLE);
                video_image.setBackgroundResource(R.drawable.em_video_show);
            }
        }
    }
}