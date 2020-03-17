package com.easemob.videocall.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.model.EaseCompat;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.util.EMLog;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import java.io.File;
import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_setting);

        room_name = (TextView)findViewById(R.id.room_name_edit);
        room_password = (TextView)findViewById(R.id.room_password_edit);
        room_admin = (TextView)findViewById(R.id.room_admin_edit);

        room_name.setText(ConferenceInfo.getInstance().getRoomname());
        room_password.setText(ConferenceInfo.getInstance().getPassword());

        InitRoomInfo();

        //upload button
        Button uploadlog = (Button)findViewById(R.id.btn_upload_roomlog);
        uploadlog.setOnClickListener(this);
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
                                if(ConferenceInfo.getInstance().getAdmin() != null){
                                    room_admin.setText(ConferenceInfo.getInstance().getAdmin());
                                }else {
                                    room_admin.setText("本房间还未指定管理员");
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


    public void onRoomSettingback(View view){
        finish();
    }
}
