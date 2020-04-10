package com.easemob.videocall.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.DemoHelper;
import com.easemob.videocall.R;
import com.easemob.videocall.ui.SetTalkerItemDialog;
import com.easemob.videocall.ui.widget.EaseImageView;
import com.easemob.videocall.utils.PreferenceManager;
import com.easemob.videocall.utils.StringUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.easemob.videocall.utils.ConferenceInfo;


import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.superrtc.ContextUtils.getApplicationContext;


/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */


public class TalkerItemAdapter extends EaseBaseRecyclerViewAdapter<EMConferenceStream> {
    private TalkerItemAdapter adapter;

    public TalkerItemAdapter() {
        adapter = this;
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
        private EaseImageView  ImageView;
        private RelativeLayout btn_ItemSet;
        private String headImage;
        private String url;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
             userIdView = (TextView)findViewById(R.id.userId_view);
             mic_image  = (ImageView)findViewById(R.id.mic_image);
             video_image = (ImageView)findViewById(R.id.video_image);
             ImageView= (EaseImageView)findViewById(R.id.avatar_view);
             btn_ItemSet = (RelativeLayout)findViewById(R.id.btn_set_Item);
        }

        @Override
        public void setData(EMConferenceStream item, int position) {
            String username = item.getUsername();

            //item.getUsername() != EMClient.getInstance().getCurrentUser()
            if(ConferenceInfo.getInstance().getAdmins().contains(EMClient.getInstance().getCurrentUser()) ) {
                btn_ItemSet.setVisibility(VISIBLE);
                MyListener listener = new MyListener(position,username);
                btn_ItemSet.setOnClickListener(listener);
            }
            EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(item.getUsername());
            if(item.getUsername() == EMClient.getInstance().getCurrentUser()){
                if(ConferenceInfo.getInstance().getAdmins().contains(item.getUsername())){
                    userIdView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6) + " (我)" + " (主持人)");
                }else {
                    userIdView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6) + " (我)");
                }
                headImage = PreferenceManager.getInstance().getCurrentUserAvatar();
                url =  DemoApplication.baseurl + headImage;
            }else {
                if(ConferenceInfo.getInstance().getAdmins().contains(item.getUsername())){
                    userIdView.setText(StringUtils.tolongNickName(memberInfo.nickName,6) +" (主持人)");
                }else {
                    userIdView.setText(StringUtils.tolongNickName(memberInfo.nickName,6));
                }
                try {
                    JSONObject object = new JSONObject(memberInfo.extension);
                    this.headImage = object.optString("headImage");
                    url = DemoApplication.baseurl;
                    url = url + this.headImage;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            //加载头像图片
            loadImage();

            if(item.isAudioOff()){
                mic_image.setVisibility(VISIBLE);
                mic_image.setBackgroundResource(R.drawable.call_list_audio_off);
            }else{
                mic_image.setVisibility(VISIBLE);
                mic_image.setBackgroundResource(R.drawable.call_list_audio_on);
            }
            if(item.isVideoOff()){
                video_image.setVisibility(VISIBLE);
                video_image.setBackgroundResource(R.drawable.call_list_video_off);
            }else {
                video_image.setVisibility(VISIBLE);
                video_image.setBackgroundResource(R.drawable.call_list_viedo_on);
            }
        }

        /**
         * 获取网落图片资源
         * @return
         */
        private void loadImage() {
            new AsyncTask<String, Void, Bitmap>() {
                //该方法运行在后台线程中，因此不能在该线程中更新UI，UI线程为主线程
                @Override
                protected Bitmap doInBackground(String... params) {
                    Bitmap bitmap = null;
                    try {
                        String url = params[0];
                        URL HttpURL = new URL(url);
                        HttpURLConnection conn = (HttpURLConnection) HttpURL.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return bitmap;
                }

                //在doInBackground 执行完成后，onPostExecute 方法将被UI 线程调用，
                // 后台的计算结果将通过该方法传递到UI线程，并且在界面上展示给用户.
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if(bitmap != null){
                        ImageView.setImageBitmap(bitmap);
                    }
                }
            }.execute(url);
        }
    }

    private class MyListener implements View.OnClickListener {
        private int position;
        private String username;

        public MyListener(int position,String username) {
            this.position = position;
            this.username = username;
        }

        @Override
        public void onClick(View v) {
            setItemShow();
        }

        private void setItemShow(){
            SetTalkerItemDialog dialog = new SetTalkerItemDialog();
            Bundle bundle = new Bundle();
            bundle.putString("username",username);
            bundle.putInt("position",position);
            dialog.setArguments(bundle);
            dialog.setAdapter(adapter);
            dialog .show(((AppCompatActivity)mContext).getSupportFragmentManager(), "SetTalkerItemDialog");
        }
    }
}