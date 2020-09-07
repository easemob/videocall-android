package com.easemob.videocall.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.utils.Config;
import com.easemob.videocall.utils.ConfigManager;
import com.easemob.videocall.utils.PreferenceManager;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.util.EMLog;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;

import com.easemob.videocall.adapter.TalkerItemAdapter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.superrtc.mediamanager.EMediaManager.getContext;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class TalkerListActivity extends AppCompatActivity  implements View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView recyclerView;
    private TextView  attendance_count_view;
    private Button btn_mute_all;
    private Button btn_unmute_all;
    private Button btn_start_record;
    private Button btn_stop_record;
    private RelativeLayout buttonLayout;

    private List<EMConferenceStream> streamList;
    private List<EMConferenceStream> talkerList;

    DividerItemDecoration decoration;
    private TalkerItemAdapter adapter;

    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    private int mId;
    private String mAction;


    // 实例化一个广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver () {
        public void onReceive (Context context, Intent intent){
            // TODO 接收到广播时的逻辑
            if (mAction.equals(intent.getAction())) {
                String[] changedParts = intent.getStringArrayExtra(Config.KEY_CHANGED_PARTS);
                Config config = ConfigManager.getInstance().getConfig(mId);
                String key = changedParts[0];
                String value = (String) config.get(getContext(), key);
                upDateTalkerList(key ,value);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talker_list);

        attendance_count_view = (TextView)findViewById(R.id.attendance_count);
        recyclerView = (RecyclerView)findViewById(R.id.talker_recyclerView);

        decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

        buttonLayout = findViewById(R.id.btn_mute_layout);

        btn_mute_all = findViewById(R.id.btn_mute_all);
        btn_unmute_all = findViewById(R.id.btn_unmute_all);
        btn_start_record = findViewById(R.id.btn_start_record);
        btn_stop_record = findViewById(R.id.btn_stop_record);

        btn_mute_all.setOnClickListener(this);
        btn_unmute_all.setOnClickListener(this);
        btn_start_record.setOnClickListener(this);
        btn_stop_record.setOnClickListener(this);

//        btn_start_record.setVisibility(View.GONE);
//        btn_stop_record.setVisibility(View.GONE);

        mId = getIntent().getIntExtra(ConferenceActivity.KEY_ID, -1);
        mAction = Config.ACTION_CONFIG_CHANGE + mId;
        if (-1 == mId) {
            return;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(mAction));
        Config config = ConfigManager.getInstance().getConfig(mId);

        InitRoomInfo();
    }


    EMConferenceManager.EMRecordMediaFormat getStreamRecordFormat(){
        String format = PreferenceManager.getInstance().getPushStreamRecordFormat();
        if(format.equals("(Auto)MP4")){
            return EMConferenceManager.EMRecordMediaFormat.MP4;
        }else if(format.equals("MP3")){
            return  EMConferenceManager.EMRecordMediaFormat.MP3;
        }else if(format.equals("M4A")){
            return  EMConferenceManager.EMRecordMediaFormat.M4A;
        }else if(format.equals("WAV")){
            return  EMConferenceManager.EMRecordMediaFormat.WAV;
        }
        return  EMConferenceManager.EMRecordMediaFormat.MP4;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mute_all:
                onMuteAll();
                break;
            case R.id.btn_unmute_all:
                onUnMuteAll();
                break;
            case R.id.btn_start_record:
                startRecord();
                break;
            case R.id.btn_stop_record:
                stopRecord();
                break;
            default:
                break;
        }
    }

    private void startRecord(){
        Map<String,String>  livecfgs = EMClient.getInstance().conferenceManager().getLiveCfgs();
        btn_start_record.setClickable(false);
        if(livecfgs.size() > 0){
            String liveId = null;
            Iterator<String> iter = livecfgs.keySet().iterator();
            while(iter.hasNext()){
                liveId=iter.next();
                break;
            }
            EMClient.getInstance().conferenceManager().enableRecordLivestream(liveId,getStreamRecordFormat(),true, new EMValueCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    btn_start_record.setClickable(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "成功开启录制", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(int error, String errorMsg) {
                    btn_start_record.setClickable(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "开启录制失败:" + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
        }

    }

    private void stopRecord(){
        Map<String,String>  livecfgs = EMClient.getInstance().conferenceManager().getLiveCfgs();
        if(livecfgs.size() > 0) {
            btn_stop_record.setClickable(false);
            String liveId = null;
            Iterator<String> iter = livecfgs.keySet().iterator();
            while (iter.hasNext()) {
                liveId = iter.next();
                break;
            }
            EMClient.getInstance().conferenceManager().enableRecordLivestream(liveId, false, new EMValueCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    btn_stop_record.setClickable(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "成功停止录制", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(int error, String errorMsg) {
                    btn_stop_record.setClickable(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "停止录制失败:" + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    /**
     * 全体静音
     */
    private  void onMuteAll(){
        EMClient.getInstance().conferenceManager().muteAll(ConferenceInfo.getInstance().getConference().getConferenceId(),
                true, new  EMValueCallBack<String>(){
                    @Override
                    public void onSuccess(String value) {
                        EMLog.i(TAG, "request_tobe_mute_all scuessed");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "您已成功设置全体静音", Toast.LENGTH_SHORT).show();
                                btn_mute_all.setClickable(true);
                            }
                        });
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.i(TAG, "request_tobe_mute_all failed: error=" + error + ", msg=" + errorMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "设置全体静音失败", Toast.LENGTH_SHORT).show();
                                btn_mute_all.setClickable(true);
                            }
                        });
                    }
                });

    }

    /**
     * 解除全体静音
     */
    private void onUnMuteAll(){
        btn_unmute_all.setClickable(false);
        EMClient.getInstance().conferenceManager().muteAll(ConferenceInfo.getInstance().getConference().getConferenceId(),
                false, new  EMValueCallBack<String>(){
                    @Override
                    public void onSuccess(String value) {
                        EMLog.i(TAG, "request_tobe_unmute_all scuessed");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "您已成功解除全体静音", Toast.LENGTH_SHORT).show();
                                btn_unmute_all.setClickable(true);
                            }
                        });
                    }
                    @Override
                    public void onError(int error, String errorMsg){
                        EMLog.i(TAG, "request_tobe_unmute_all failed: error=" + error + ", msg=" + errorMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "解除全体静音失败", Toast.LENGTH_SHORT).show();
                                btn_unmute_all.setClickable(false);
                            }
                        });

                    }
                });
    }

    public void InitRoomInfo(){
        EMClient.getInstance().conferenceManager().getConferenceInfo(ConferenceInfo.getInstance().getConference().getConferenceId(),ConferenceInfo.getInstance().getPassword(),
                new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(EMConference value) {
                        ConferenceInfo.getInstance().getConference().setTalkers(value.getTalkers());
                        ConferenceInfo.getInstance().getConference().setAudienceTotal(value.getAudienceTotal());
                        ConferenceInfo.getInstance().getConference().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().getConference().setMemberNum(value.getMemberNum());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                attendance_count_view.setText("当前观众人数 " +String.valueOf(ConferenceInfo.getInstance().getConference().getAudienceTotal()));
                                streamList = ConferenceInfo.getInstance().getConferenceStreamList();
                                talkerList = ConferenceInfo.getInstance().getTalkerList();
                                if(talkerList != null){
                                    talkerList.clear();
                                }

                                for (EMConferenceStream stream : streamList){
                                    if (stream.getStreamType() != EMConferenceStream.StreamType.DESKTOP){
                                        talkerList.add(stream);
                                    }
                                }

                                if(talkerList != null){
                                    if (ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience){
                                        if(!talkerList.contains(ConferenceInfo.getInstance().getLocalStream())){
                                            talkerList.add(ConferenceInfo.getInstance().getLocalStream());
                                        }
                                    }

                                    Collections.reverse(talkerList);
                                    if(talkerList.size() == 0){
                                        recyclerView.setVisibility(View.GONE);
                                    }else {
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                    LinearLayoutManager layoutManager = new LinearLayoutManager(TalkerListActivity.this);
                                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                    recyclerView.setLayoutManager(layoutManager);

                                    adapter = new TalkerItemAdapter();
                                    adapter.setData(talkerList);
                                    recyclerView.setAdapter(adapter);
                                }

                                if(!ConferenceInfo.getInstance().getAdmins().contains(EMClient.getInstance().getCurrentUser())){
                                    buttonLayout.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.i(TAG, "getConferenceInfo failed: error=" + error + ", msg=" + errorMsg);
                    }
                });
    }

    public void onTalkerListback(View view){
        if(talkerList != null){
            if(talkerList.contains(ConferenceInfo.getInstance().getLocalStream())){
                talkerList.remove(ConferenceInfo.getInstance().getLocalStream());
            }
        }
        finish();
    }

    private void upDateTalkerList(String key ,String value){
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(talkerList != null) {
                if (talkerList.contains(ConferenceInfo.getInstance().getLocalStream())) {
                    talkerList.remove(ConferenceInfo.getInstance().getLocalStream());
                }
            }
            finish();
            return true;
       }
       return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            x1 = event.getX();
            y1 = event.getY();
        }
        if(event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            x2 = event.getX();
            y2 = event.getY();
            if(y1 - y2 > 50) {
            } else if(y2 - y1 > 50) {
            } else if(x1 - x2 > 50) {
            } else if(x2 - x1 > 50) {
                if(talkerList != null) {
                    if (talkerList.contains(ConferenceInfo.getInstance().getLocalStream())) {
                        talkerList.remove(ConferenceInfo.getInstance().getLocalStream());
                    }
                }
                finish();
            }
        }
        return super.onTouchEvent(event);
    }
}
