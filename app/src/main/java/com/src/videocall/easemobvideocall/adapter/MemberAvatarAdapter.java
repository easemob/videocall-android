package com.src.videocall.easemobvideocall.adapter;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;
import com.src.videocall.easemobvideocall.R;
import com.src.videocall.easemobvideocall.ui.ConferenceMemberView;
import com.superrtc.sdk.VideoView;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MemberAvatarAdapter extends EaseBaseRecyclerViewAdapter<EMConferenceStream> {

    public MemberAvatarAdapter() {
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_conference_member_view, parent, false);
        return new AvatarViewHolder(view);
    }

    private class AvatarViewHolder extends ViewHolder<EMConferenceStream> {
        private EMCallSurfaceView surfaceView;
        private ImageView avatar_view;
        private ImageView video_view;
        private ImageView audio_view;
        private boolean flag = false;
        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            surfaceView = (EMCallSurfaceView)findViewById(R.id.item_surface_view);
            avatar_view = (ImageView)findViewById(R.id.call_avatar);
            audio_view = (ImageView)findViewById(R.id.icon_speaking);
            video_view = (ImageView)findViewById(R.id.icon_videoing);
            surfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        }

        @Override
        public void setData(EMConferenceStream item, int position){
            //订阅流
            //if(!flag){
                avatar_view.setVisibility(GONE);
                EMClient.getInstance().conferenceManager().subscribe(item, surfaceView, new EMValueCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                    }
                    @Override
                    public void onError(int error, String errorMsg) {

                    }
                });
                flag = true;
           // }
            if(item.isAudioOff()){
                audio_view.setBackgroundResource(R.drawable.em_call_mic_off);
            }else{
                audio_view.setBackgroundResource(R.drawable.em_call_mic_on);
            }
            if(item.isVideoOff()){
                avatar_view.setVisibility(VISIBLE);
                video_view.setBackgroundResource(R.drawable.em_call_video_off);
            }else {
                avatar_view.setVisibility(GONE);
                video_view.setBackgroundResource(R.drawable.em_call_video_on);
            }
        }


    }
}