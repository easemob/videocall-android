package com.src.videocall.easemobvideocall.adapter;

import android.graphics.PixelFormat;
import android.os.Build;
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
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;
import com.src.videocall.easemobvideocall.R;
import com.src.videocall.easemobvideocall.ui.ConferenceActivity;
import com.src.videocall.easemobvideocall.ui.ConferenceMemberView;
import com.src.videocall.easemobvideocall.utils.ConferenceInfo;
import com.superrtc.sdk.VideoView;

import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.JoiningType.TRANSPARENT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MemberAvatarAdapter extends EaseBaseRecyclerViewAdapter<EMConferenceStream> {

    private final String TAG = this.getClass().getSimpleName();

    private  OnItemGetSurfaceView callback = null;
    private List<EMConferenceStream> streamList;

    public MemberAvatarAdapter() {
        streamList = ConferenceInfo.getInstance().getConferenceStreamList();
    }


    public void setCallback(OnItemGetSurfaceView callback){
        this.callback = callback;
    }


    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_conference_member_view, parent, false);
        view.bringToFront();
        return new AvatarViewHolder(view);
    }


    private class AvatarViewHolder extends ViewHolder<EMConferenceStream> {
        private EMCallSurfaceView surfaceView;
        private ImageView avatar_view;
        private ImageView video_view;
        private ImageView audio_view;
        private TextView  icon_text;
        private RelativeLayout show_layout;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            surfaceView = (EMCallSurfaceView)findViewById(R.id.surface_view_listItem);
            avatar_view = (ImageView)findViewById(R.id.call_avatar);
            audio_view = (ImageView)findViewById(R.id.icon_speaking);
            video_view = (ImageView)findViewById(R.id.icon_videoing);
            icon_text = (TextView)findViewById(R.id.icon_text);
            surfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
            //show_layout.removeView(video_view);
            //show_layout.removeView(audio_view);

            surfaceView.setZOrderMediaOverlay(true);
            audio_view.bringToFront();
            video_view.bringToFront();
            surfaceView.setZOrderOnTop(false);
        }

        @Override
        public void setData(EMConferenceStream item, int position) {
            //surfaceView.setZOrderOnTop(false);
            EMLog.i(TAG,"MemberAvatarAdapter setData start: postion：" + position + " userId: " + item.getUsername());
            surfaceView.setVisibility(VISIBLE);
            int currentIndex = ConferenceInfo.getInstance().getConferenceStreamList() .indexOf(ConferenceInfo.currentStream);
            if(ConferenceInfo.changeflag && currentIndex  == position) {
                if (ConferenceInfo.getInstance().getLocalStream().isAudioOff()) {
                    audio_view.setBackgroundResource(R.drawable.call_mic_off);
                } else {
                    audio_view.setBackgroundResource(R.drawable.call_mic_on);
                }

                //设置用户标识
                String fristStr = EMClient.getInstance().getCurrentUser().substring(0,5);
                String lastStr =  EMClient.getInstance().getCurrentUser().substring(EMClient.getInstance().getCurrentUser().length()-5);
                fristStr = fristStr+"***"+lastStr;
                icon_text.setText(fristStr + "(我)");

                if (ConferenceInfo.getInstance().getLocalStream().isVideoOff()) {
                    video_view.setBackgroundResource(R.drawable.call_video_off);
                    avatar_view.setVisibility(VISIBLE);
                }else{
                    video_view.setBackgroundResource(R.drawable.call_video_on);
                    avatar_view.setVisibility(GONE);
                    if(ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience){
                        surfaceView.release();
                        EMClient.getInstance().conferenceManager().setLocalSurfaceView(surfaceView);
                    }else{
                        avatar_view.setVisibility(VISIBLE);
                        surfaceView.setVisibility(GONE);
                    }
                }

                /*if(ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience){
                     avatar_view.setVisibility(VISIBLE);
                     icon_text.setText("");
                }*/
            }else{
                //设置用户标识
                String fristStr = item.getUsername().substring(0,5);
                String lastStr =  item.getUsername().substring(item.getUsername().length()-5);
                fristStr = fristStr+"***"+lastStr;
                if(item.getUsername() == EMClient.getInstance().getCurrentUser()){
                    icon_text.setText(fristStr + " (我)");
                }else{
                    icon_text.setText(fristStr);
                }
                if (item.isAudioOff()) {
                    audio_view.setBackgroundResource(R.drawable.call_mic_off);
                } else {
                    audio_view.setBackgroundResource(R.drawable.call_mic_on);
                }
                if (item.isVideoOff()){
                    avatar_view.setVisibility(VISIBLE);
                    video_view.setBackgroundResource(R.drawable.call_video_off);
                } else{
                    avatar_view.setVisibility(GONE);
                    video_view.setBackgroundResource(R.drawable.call_video_on);
                }
            }

            if(callback != null){
                callback.OnItemGetSurfaceView(surfaceView,position,avatar_view);
                EMLog.i(TAG,"MemberAvatarAdapter setData start: postion：" + position + " userId: " + item.getUsername());
            }
        }
    }
}