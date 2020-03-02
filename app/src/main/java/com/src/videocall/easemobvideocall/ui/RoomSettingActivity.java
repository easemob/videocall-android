package com.src.videocall.easemobvideocall.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.model.EaseCompat;
import com.hyphenate.util.EasyUtils;
import com.src.videocall.easemobvideocall.R;
import com.src.videocall.easemobvideocall.utils.ConferenceInfo;

import java.io.File;
import java.util.ArrayList;

import static com.superrtc.mediamanager.EMediaManager.getContext;

public class RoomSettingActivity extends AppCompatActivity implements View.OnClickListener {

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

        room_admin.setText(ConferenceInfo.getInstance().getAdmin());

        //upload button
        Button uploadlog = (Button)findViewById(R.id.btn_upload_roomlog);
        uploadlog.setOnClickListener(this);
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
                File temp = File.createTempFile("hyphenate", ".log.gz", storage);
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
