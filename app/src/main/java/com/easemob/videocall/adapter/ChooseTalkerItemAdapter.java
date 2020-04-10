package com.easemob.videocall.adapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.R;
import com.easemob.videocall.ui.widget.EaseImageView;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.PreferenceManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.media.EMCallSurfaceView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class ChooseTalkerItemAdapter extends EaseBaseRecyclerViewAdapter<EMConferenceStream> {
    public static int chooseIndex = -1;
    public ChooseTalkerItemAdapter(){

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.choose_talker_list, parent, false);
        return new AvatarViewHolder(view);
    }

    private class AvatarViewHolder extends ViewHolder<EMConferenceStream> {
        private EMCallSurfaceView surfaceView;
        private TextView userId_view;
        private RadioButton id_checkbox;
        private EaseImageView ImageView;
        private String headImage;
        private String url;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            userId_view = (TextView)findViewById(R.id.chooseId_view);
            id_checkbox = (RadioButton)findViewById(R.id.id_checkbox);
            ImageView= (EaseImageView)findViewById(R.id.avatar_choose_view);
            chooseIndex = -1;
        }

        @Override
        public void setData(EMConferenceStream item, int position){
            EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(item.getUsername());
            if(item.getUsername().equals(EMClient.getInstance().getCurrentUser())){
                if(ConferenceInfo.getInstance().getAdmins().contains(item.getUsername())){
                    userId_view.setText(PreferenceManager.getInstance().getCurrentUserNick() + " (我)" + " (主持人)");
                }else {
                    userId_view.setText(PreferenceManager.getInstance().getCurrentUserNick() + " (我)");
                }
                headImage = PreferenceManager.getInstance().getCurrentUserAvatar();
                url =  DemoApplication.baseurl + headImage;
            }else {
                if(ConferenceInfo.getInstance().getAdmins().contains(item.getUsername())){
                    userId_view.setText(memberInfo.nickName +" (主持人)");
                }else {
                    userId_view.setText(memberInfo.nickName);
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

            if(position == chooseIndex){
                id_checkbox.setChecked(true);
            }else{
                id_checkbox.setChecked(false);
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
}

