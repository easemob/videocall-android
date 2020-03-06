package com.src.videocall.easemobvideocall.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.util.EMLog;
import com.src.videocall.easemobvideocall.R;
import com.src.videocall.easemobvideocall.adapter.MemberAvatarAdapter;
import com.src.videocall.easemobvideocall.adapter.TalkerItemAdapter;
import com.src.videocall.easemobvideocall.utils.ConferenceInfo;
import com.src.videocall.easemobvideocall.utils.PhoneStateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TalkerListActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private RecyclerView recyclerView;
    private TextView  attendance_count_view;
    private TextView  attendance_hot_view;
    private List<String> list;
    private EMConference currentConference;

    private List<EMConferenceStream> streamList;

    DividerItemDecoration decoration;

    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talker_list);

        attendance_count_view = (TextView)findViewById(R.id.attendance_count);
        recyclerView = (RecyclerView)findViewById(R.id.talker_recyclerView);

        decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

        InitRoomInfo();
    }

    public void InitRoomInfo(){
        EMClient.getInstance().conferenceManager().getConferenceInfo(ConferenceInfo.getInstance().getConference().getConferenceId(),ConferenceInfo.getInstance().getPassword(),
                new EMValueCallBack<EMConference>() {


                    @Override
                    public void onSuccess(EMConference value) {
                        ConferenceInfo.getInstance().getConference().setTalkers(value.getTalkers());
                        ConferenceInfo.getInstance().getConference().setAudienceTotal(value.getAudienceTotal());
                        ConferenceInfo.getInstance().getConference().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().getConference().setMemberNum(value.getMemberNum());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                attendance_count_view.setText(String.valueOf(ConferenceInfo.getInstance().getConference().getAudienceTotal())+"人");

                                streamList = ConferenceInfo.getInstance().getConferenceStreamList();

                                if (ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience){
                                    if(!streamList.contains(ConferenceInfo.getInstance().getLocalStream())){
                                        streamList.add(ConferenceInfo.getInstance().getLocalStream());
                                    }
                                }

                                Collections.reverse(streamList);
                                if(streamList.size() == 0){
                                    recyclerView.setVisibility(View.GONE);
                                }else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                }

                                LinearLayoutManager layoutManager = new LinearLayoutManager(TalkerListActivity.this);
                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                recyclerView.setLayoutManager(layoutManager);

                                TalkerItemAdapter adapter = new TalkerItemAdapter();
                                adapter.setData(streamList);
                                recyclerView.setAdapter(adapter);
                                decoration.setDrawable(getResources().getDrawable(R.drawable.divider));
                                recyclerView.addItemDecoration(decoration);
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
        if(streamList.contains(ConferenceInfo.getInstance().getLocalStream())){
            streamList.remove(ConferenceInfo.getInstance().getLocalStream());
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(streamList.contains(ConferenceInfo.getInstance().getLocalStream())){
                streamList.remove(ConferenceInfo.getInstance().getLocalStream());
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
                //Toast.makeText(MainActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
            } else if(y2 - y1 > 50) {
                //Toast.makeText(MainActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
            } else if(x1 - x2 > 50) {
               // Toast.makeText(MainActivity.this, "向左滑", Toast.LENGTH_SHORT).show();
            } else if(x2 - x1 > 50) {
                if(streamList.contains(ConferenceInfo.getInstance().getLocalStream())){
                    streamList.remove(ConferenceInfo.getInstance().getLocalStream());
                }
                finish();
            }
        }
        return super.onTouchEvent(event);
    }
}
