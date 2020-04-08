package com.easemob.videocall.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.adapter.AdminInfoAdapter;
import com.easemob.videocall.model.EaseCompat;
import com.easemob.videocall.ui.widget.MyListview;
import com.easemob.videocall.utils.ConferenceAttributeOption;
import com.easemob.videocall.utils.Config;
import com.easemob.videocall.utils.ConfigManager;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.util.EMLog;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.hyphenate.util.EasyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.superrtc.mediamanager.EMediaManager.getContext;


/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class RoomSettingActivity extends Activity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    TextView room_name;
    TextView room_password;
    TextView room_admin;
    MyListview listView;
    private AdminInfoAdapter adapter;
    private List<String> adminList;
    private int mId;
    private String mAction;

    // 实例化一个广播接收器 接受成为管理员的变化
    private  BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver () {
        public void onReceive (Context context, Intent intent){
            // TODO 接收到广播时的逻辑
            if (mAction.equals(intent.getAction())) {
                String[] changedParts = intent.getStringArrayExtra(Config.KEY_CHANGED_PARTS);
                Config config = ConfigManager.getInstance().getConfig(mId);
                String key = changedParts[0];
                String value = (String) config.get(getContext(), key);
                upDateAdmin(value);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_setting);

        room_name = (TextView)findViewById(R.id.room_name_edit);
        room_password = (TextView)findViewById(R.id.room_password_edit);
        room_admin = (TextView)findViewById(R.id.btn_request_admin);

        adminList = ConferenceInfo.getInstance().getAdmins();

        mId = getIntent().getIntExtra(ConferenceActivity.KEY_ID, -1);
        mAction = Config.ACTION_CONFIG_CHANGE + mId;
        if (-1 == mId) {
            return;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(mAction));
        Config config = ConfigManager.getInstance().getConfig(mId);

        listView = (MyListview) findViewById(R.id.adminListView) ;
        adapter = new AdminInfoAdapter(RoomSettingActivity.this ,R.layout.content_admin_item,adminList);
        listView.setAdapter(adapter);

        room_name.setText(ConferenceInfo.getInstance().getRoomname());
        room_password.setText(ConferenceInfo.getInstance().getPassword());

        InitRoomInfo();

        //upload button
        Button uploadlog = (Button)findViewById(R.id.btn_upload_roomlog);
        uploadlog.setOnClickListener(this);

        room_admin.setOnClickListener(this);

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
                        adminList = ConferenceInfo.getInstance().getAdmins();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(adminList != null || adminList.size() > 0){
                                    if(ConferenceInfo.getInstance().getAdmins().contains(EMClient.getInstance().getCurrentUser())){
                                            room_admin.setText("放弃主持人");
                                            room_admin.setClickable(true);
                                    }else {
                                        if(ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience){
                                            room_admin.setText("申请成为主持人");
                                            room_admin.setClickable(true);
                                        }else{
                                            room_admin.setClickable(false);
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_upload_roomlog:
                 sendLogThroughMail();
                 break;
            case R.id.btn_request_admin:
                 requesTobeAdmin();
                 break;
            default:
                break;
        }
    }

    void sendLogThroughMail() {
        String logPath = "";
        try {
            logPath = EMClient.getInstance().compressLogs();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "compress logs failed", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        File f = new File(logPath);
        File storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (f.exists() && f.canRead()) {
            try {
                storage.mkdirs();
                File temp = File.createTempFile("videocall-android", ".log.tar", storage);
                if (!temp.canWrite()) {
                    return;
                }
                boolean result = f.renameTo(temp);
                if (result == false) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "log");
                intent.putExtra(Intent.EXTRA_TEXT, "log in attachment: " + temp.getAbsolutePath());

                intent.setType("application/octet-stream");
                ArrayList<Uri> uris = new ArrayList<>();
                uris.add(EaseCompat.getUriForFile(getContext(), temp));
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);
                startActivity(intent);
            } catch (final Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private void upDateAdmin(String userId){
        if (userId.equals(EMClient.getInstance().getCurrentUser())){
            if(adminList.contains(EMClient.getInstance().getCurrentUser())){
                room_admin.setText("放弃主持人");
                room_admin.setClickable(true);
            }else{
                room_admin.setText("申请成为主持人");
                room_admin.setClickable(true);
            }
            adapter.notifyDataSetChanged();
        }else{
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 申请 放弃主持人
     */
    private void requesTobeAdmin(){
        if(adminList.contains(EMClient.getInstance().getCurrentUser())){
            if(ConferenceInfo.getInstance().getAdmins().size() == 1){
                Toast.makeText(getApplicationContext(), "当前只有您一个主持人，不允许放弃主持人!", Toast.LENGTH_SHORT).show();
                return;
            }
            room_admin.setClickable(false);
            String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), EMClient.getInstance().getCurrentUser());
            EMClient.getInstance().conferenceManager().grantRole(ConferenceInfo.getInstance().getConference().getConferenceId()
                    , new EMConferenceMember(memName, null, null, null)
                    , EMConferenceManager.EMConferenceRole.Talker, new EMValueCallBack<String>() {
                        @Override
                        public void onSuccess(String value) {
                            EMLog.i(TAG, "requesTobeAdmin  request_tobe_Talker changeRole success, result: " + value);
                            adminList.remove(EMClient.getInstance().getCurrentUser());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "放弃主持人成功！", Toast.LENGTH_SHORT).show();
                                    adapter.notifyDataSetChanged();
                                    room_admin.setClickable(true);
                                }
                            });
                        }
                        @Override
                        public void onError(int error, String errorMsg) {
                            EMLog.i(TAG, "requesTobeAdmin  request_tobe_Talke failed, error: " + error + " - " + errorMsg);
                            Toast.makeText(getApplicationContext(), "发送放弃主持人请求失败 请稍后重试!", Toast.LENGTH_SHORT).show();
                            room_admin.setClickable(true);
                        }
                    });
        }else {
            room_admin.setClickable(false);
            EMClient.getInstance().conferenceManager().setConferenceAttribute(EMClient.getInstance().getCurrentUser()
                    , ConferenceAttributeOption.REQUEST_TOBE_ADMIN, new EMValueCallBack<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            EMLog.i(TAG, "request_tobe_admin scuessed");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "发送请求主持人成功 ,请等待审批！", Toast.LENGTH_SHORT).show();
                                    room_admin.setClickable(true);
                                }
                            });
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                            EMLog.i(TAG, "request_tobe_admin failed: error=" + error + ", msg=" + errorMsg);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "发送请求主持人失败 请稍后重试!", Toast.LENGTH_SHORT).show();
                                    room_admin.setClickable(true);
                                }
                            });
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    public void onRoomSettingback(View view){
        finish();
    }
}
