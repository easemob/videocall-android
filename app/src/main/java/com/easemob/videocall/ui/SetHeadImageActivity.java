package com.easemob.videocall.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.R;
import com.easemob.videocall.adapter.ChooseTalkerItemAdapter;
import com.easemob.videocall.adapter.HeadImageItemAdapter;
import com.easemob.videocall.adapter.OnItemClickListener;
import com.easemob.videocall.utils.PreferenceManager;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.util.EMLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SetHeadImageActivity extends Activity implements View.OnClickListener {
    RecyclerView headImageView;
    String url;
    String urlparm = "headImage.conf";
    private List<String> headImageList;
    DividerItemDecoration decoration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_headimage_setting);

        headImageView = findViewById(R.id.headImage_recyclerView);

        decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

        Button  editHeadImage_back = findViewById(R.id.editHeadImage_back);
        editHeadImage_back.setOnClickListener(this);

        Button  save_HeadImage = findViewById(R.id.save_HeadImage);
        save_HeadImage.setOnClickListener(this);

        //获取头像资源
        url = DemoApplication.baseurl;
        url = url + urlparm;
        loadHeadList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editHeadImage_back:
                setImageHead(false);
                break;
            case R.id.save_HeadImage:
                setImageHead(true);
                break;
            default:
                break;
        }
    }

    private void setImageHead(boolean save){
        if(save){
            String headImage = headImageList.get(HeadImageItemAdapter.chooseIndex);
            PreferenceManager.getInstance().setCurrentUserAvatar(headImage);
            getIntent().putExtra("headImage", headImage);
            setResult(RESULT_OK, getIntent());
            finish();
        }else{
            getIntent().putExtra("headImage", PreferenceManager.getInstance().getCurrentUserAvatar());
            setResult(RESULT_OK, getIntent());
            finish();
        }
    }


    /**
     * 获取网落图片资源
     * @return
     */
    private void loadHeadList() {
        new AsyncTask<String, Void, String>() {
            //该方法运行在后台线程中，因此不能在该线程中更新UI，UI线程为主线程
            @Override
            protected String doInBackground(String... params) {
                String headImage = null;
                try {
                    String url = params[0];
                    URL HttpURL = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) HttpURL.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    String line;

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    while((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    headImage = sb.toString();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return headImage;
            }

            //在doInBackground 执行完成后，onPostExecute 方法将被UI 线程调用，
            // 后台的计算结果将通过该方法传递到UI线程，并且在界面上展示给用户.
            @Override
            protected void onPostExecute(String ImageStr) {
                if(ImageStr != null){
                    try {
                        ImageStr = ImageStr.replace(" ","");
                        JSONObject object = new JSONObject(ImageStr);
                        JSONObject headImageobject = object.optJSONObject("headImageList");
                        Iterator it = headImageobject.keys();
                        while(it.hasNext()){
                            String key = it.next().toString();
                            if(headImageList == null){
                                headImageList = new ArrayList<>();
                            }
                            headImageList.add(headImageobject.optString(key));
                            if( PreferenceManager.getInstance().getCurrentUserAvatar().equals(headImageobject.optString(key))){
                                HeadImageItemAdapter.chooseIndex = headImageList.size()-1;
                            }
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                headImageView.setLayoutManager(layoutManager);
                                HeadImageItemAdapter adapter = new HeadImageItemAdapter();
                                //decoration.setDrawable(getResources().getDrawable(R.drawable.divider));
                                headImageView.setAdapter(adapter);
                                //headImageView.addItemDecoration(decoration);
                                adapter.setData(headImageList);

                                adapter.setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        HeadImageItemAdapter.chooseIndex = position;
                                        adapter.updataData();
                                    }
                                });
                                adapter.updataData();
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.execute(url);
    }
}
