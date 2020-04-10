package com.easemob.videocall.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        private String url;
        private boolean initFlag = false;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            headImage_view = (ImageView) findViewById(R.id.headImage_avatar);
            id_checkbox = (RadioButton) findViewById(R.id.headImage_checkbox);
        }

        @Override
        public void setData(String imageStr, int position) {
            if (position == chooseIndex) {
                id_checkbox.setChecked(true);
            } else {
                id_checkbox.setChecked(false);
            }
            url = DemoApplication.baseurl;
            url = url + imageStr;
            loadImage();
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
                    }catch(IOException e) {
                        e.printStackTrace();
                    }
                    return bitmap;
                }

                //在doInBackground 执行完成后，onPostExecute 方法将被UI 线程调用，
                // 后台的计算结果将通过该方法传递到UI线程，并且在界面上展示给用户.
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if(bitmap != null){
                        headImage_view.setImageBitmap(bitmap);
                    }
                }
            }.execute(url);
        }
    }
}
