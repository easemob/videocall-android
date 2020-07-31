package com.easemob.videocall.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.model.EaseCompat;
import com.hyphenate.chat.EMClient;
import com.easemob.videocall.ui.widget.EaseSwitchButton;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.PreferenceManager;
import com.hyphenate.util.EMLog;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.superrtc.mediamanager.EMediaManager.getContext;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class SettingActivity extends Activity implements View.OnClickListener{

    ImageView imageView;
    String url;
    TextView  IDView;
    String urlparm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        IDView = (TextView)findViewById(R.id.nickname_edit);
        initCameraResolutionSpinner(R.id.spinner_video_resolution);

        initRecordFormatSpinner(R.id.spinner_record_format);

        imageView = (ImageView)findViewById(R.id.headImage);

        String nickName = PreferenceManager.getInstance().getCurrentUserNick();
        if(nickName != null){
            IDView.setText(nickName);
        }else{
            IDView.setText("未设置昵称");
        }

        // video
        RelativeLayout rlSwitcheVideo = (RelativeLayout)findViewById(R.id.rl_switch_video);
        rlSwitcheVideo.setOnClickListener(this);
        EaseSwitchButton swOnVideo = (EaseSwitchButton)findViewById(R.id.switch_video);
        if (PreferenceManager.getInstance().isCallVideo()) {
            swOnVideo.openSwitch();
        } else {
            swOnVideo.closeSwitch();
        }

        // audio
        RelativeLayout rlSwitcheAudio= (RelativeLayout)findViewById(R.id.rl_switch_audio);
        rlSwitcheAudio.setOnClickListener(this);
        EaseSwitchButton swOnAudio = (EaseSwitchButton)findViewById(R.id.switch_audio);
        if (PreferenceManager.getInstance().isCallAudio()) {
            swOnAudio.openSwitch();
        } else {
            swOnAudio.closeSwitch();
        }

        //record
        RelativeLayout rlSwitcheRecrod= (RelativeLayout)findViewById(R.id.rl_switch_record);
        rlSwitcheRecrod.setOnClickListener(this);
        EaseSwitchButton swOnRecord = (EaseSwitchButton)findViewById(R.id.switch_record);
        if (PreferenceManager.getInstance().isRecordOnServer()) {
            swOnRecord.openSwitch();
        } else {
            swOnRecord.closeSwitch();
        }


        //merge stream
        RelativeLayout rlSwitchMergeStream= (RelativeLayout)findViewById(R.id.rl_switch_merge_stream);
        rlSwitchMergeStream.setOnClickListener(this);
        EaseSwitchButton swOnMergeStream = (EaseSwitchButton)findViewById(R.id.switch_merge_stream);
        if (PreferenceManager.getInstance().isMergeStream()) {
            swOnMergeStream.openSwitch();
        } else {
            swOnMergeStream.closeSwitch();
        }


        // push cdn
        RelativeLayout rlSwitchePushcdn= (RelativeLayout)findViewById(R.id.rl_switch_push_cdn);
        rlSwitchePushcdn.setOnClickListener(this);
        EaseSwitchButton swOnPushcdn = (EaseSwitchButton)findViewById(R.id.switch_push_cdn);
        if (PreferenceManager.getInstance().isPushCDN()) {
            swOnPushcdn.openSwitch();
        } else {
            swOnPushcdn.closeSwitch();
        }

        //push audio stream
        RelativeLayout rlSwitchePushAudio= (RelativeLayout)findViewById(R.id.rl_switch_push_audio_stream);
        rlSwitchePushAudio.setOnClickListener(this);
        EaseSwitchButton swOnPushAudioStream = (EaseSwitchButton)findViewById(R.id.switch_push_audio_stream);
        if (PreferenceManager.getInstance().isPushAudioStream()) {
            swOnPushAudioStream.openSwitch();
        } else {
            swOnPushAudioStream.closeSwitch();
        }

        //upload button
        Button uploadlog = (Button)findViewById(R.id.btn_upload_log);
        uploadlog.setOnClickListener(this);

        RelativeLayout myInfo = (RelativeLayout)findViewById(R.id.btn_myInfo);
        myInfo.setOnClickListener(this);

        RelativeLayout setcdnUrl = (RelativeLayout)findViewById(R.id.btn_set_cdn_url);
        setcdnUrl.setOnClickListener(this);

        //load image
        urlparm = PreferenceManager.getInstance().getCurrentUserAvatar();
        url = DemoApplication.baseurl;
        url = url + urlparm;
        loadImage();
    }

    public void onSettingback(View view){
        this.finish();
    }

    void initCameraResolutionSpinner(final int spinnerId) {
        // for simulator which doesn't has camera, open will fail
        try {
            List<String> strSizes = new ArrayList<String>();
            String str = "360P";
            strSizes.add(str);
            str = "(Auto)480P";
            strSizes.add(str);
            str = "720P";
            strSizes.add(str);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strSizes);
            adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
            final Spinner spinnerVideoResolution = (Spinner) findViewById(spinnerId);
            spinnerVideoResolution.setAdapter(adapter);

            // update selection
            int selection = 0;
            String resolution = PreferenceManager.getInstance().getCallFrontCameraResolution();
            if (resolution.equals("")) {
                selection = 1;
                PreferenceManager.getInstance().setCallFrontCameraResolution(strSizes.get(1));
            } else {
                for (int i = 0; i < strSizes.size(); i++) {
                    if (resolution.equals(strSizes.get(i))) {
                        selection = i;
                        break;
                    }
                }
            }
            if (selection < strSizes.size()) {
                spinnerVideoResolution.setSelection(selection);
            }

            AtomicBoolean disableOnce = new AtomicBoolean(false);
            spinnerVideoResolution.setTag(disableOnce);
            spinnerVideoResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    AtomicBoolean disable = (AtomicBoolean)spinnerVideoResolution.getTag();
                    if (disable.get() == true) {
                        disable.set(false);
                        return;
                    }
                    if (position == 0) {
                        PreferenceManager.getInstance().setCallFrontCameraResolution(strSizes.get(0));
                        return;
                    }
                    String size = strSizes.get(position);
                    if (size != null) {
                        PreferenceManager.getInstance().setCallFrontCameraResolution(size);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    void initRecordFormatSpinner(final int formatSpinner){
        try {
            List<String> strSizes = new ArrayList<String>();
            String str = "(Auto)MP4";
            strSizes.add(str);
            str = "MP3";
            strSizes.add(str);
            str = "M4A";
            strSizes.add(str);
            str = "WAV";
            strSizes.add(str);


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strSizes);
            adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
            final Spinner spinnerVideoResolution = (Spinner) findViewById(formatSpinner);
            spinnerVideoResolution.setAdapter(adapter);

            // update selection
            int selection = 0;
            String resolution = PreferenceManager.getInstance().getPushStreamRecordFormat();
            if (resolution == null) {
                selection = 0;
                PreferenceManager.getInstance().setPushStreamRecordFormat(strSizes.get(0));
            } else {
                for (int i = 0; i < strSizes.size(); i++) {
                    if (resolution.equals(strSizes.get(i))) {
                        selection = i;
                        break;
                    }
                }
            }
            if (selection < strSizes.size()) {
                spinnerVideoResolution.setSelection(selection);
            }

            AtomicBoolean disableOnce = new AtomicBoolean(false);
            spinnerVideoResolution.setTag(disableOnce);
            spinnerVideoResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    AtomicBoolean disable = (AtomicBoolean)spinnerVideoResolution.getTag();
                    if (disable.get() == true) {
                        disable.set(false);
                        return;
                    }
                    if (position == 0) {
                        PreferenceManager.getInstance().setPushStreamRecordFormat(strSizes.get(0));
                        return;
                    }
                    String size = strSizes.get(position);
                    if (size != null) {
                        PreferenceManager.getInstance().setPushStreamRecordFormat(size);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_switch_video:
                EaseSwitchButton swideoResolution = (EaseSwitchButton)findViewById(R.id.switch_video);
                if (swideoResolution.isSwitchOpen()) {
                    PreferenceManager.getInstance().setCallVideo(false);
                    swideoResolution.closeSwitch();
                } else {
                    PreferenceManager.getInstance().setCallVideo(true);
                    swideoResolution.openSwitch();
                }
                break;
            case R.id.rl_switch_audio:
                EaseSwitchButton swOfflineCallPush = (EaseSwitchButton)findViewById(R.id.switch_audio);
                if (swOfflineCallPush.isSwitchOpen()) {
                    PreferenceManager.getInstance().setCallAudio(false);
                    swOfflineCallPush.closeSwitch();
                } else {
                    PreferenceManager.getInstance().setCallAudio(true);
                    swOfflineCallPush.openSwitch();
                }
                break;
            case R.id.rl_switch_record:
                EaseSwitchButton swRecord = (EaseSwitchButton)findViewById(R.id.switch_record);
                if (swRecord.isSwitchOpen()) {
                    PreferenceManager.getInstance().setRecordOnServer(false);
                    swRecord.closeSwitch();
                } else {
                    PreferenceManager.getInstance().setRecordOnServer(true);
                    swRecord.openSwitch();
                }
                break;
            case R.id.rl_switch_merge_stream:
                EaseSwitchButton swMergeStream = (EaseSwitchButton)findViewById(R.id.switch_merge_stream);
                if (swMergeStream.isSwitchOpen()) {
                    PreferenceManager.getInstance().setMergeStream(false);
                    swMergeStream.closeSwitch();
                } else {
                    PreferenceManager.getInstance().setMergeStream(true);
                    swMergeStream.openSwitch();
                }
                break;
            case R.id.rl_switch_push_cdn:
                EaseSwitchButton swOfflinePushcdn = (EaseSwitchButton)findViewById(R.id.switch_push_cdn);
                if (swOfflinePushcdn.isSwitchOpen()) {
                    PreferenceManager.getInstance().setPushCDN(false);
                    swOfflinePushcdn.closeSwitch();

                    //关闭纯音频推流
                    EaseSwitchButton swOnPushAudioStream = (EaseSwitchButton)findViewById(R.id.switch_push_audio_stream);
                    PreferenceManager.getInstance().setPushAudioStream(false);
                    swOnPushAudioStream.closeSwitch();
                } else {
                    PreferenceManager.getInstance().setPushCDN(true);
                    swOfflinePushcdn.openSwitch();
                }
                break;
            case R.id.rl_switch_push_audio_stream:
                if(!PreferenceManager.getInstance().isPushCDN()){
                    Toast.makeText(getApplicationContext(), "请先开启CDN推流!",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
                EaseSwitchButton swOnPushAudioStream = (EaseSwitchButton)findViewById(R.id.switch_push_audio_stream);
                if (swOnPushAudioStream.isSwitchOpen()) {
                    PreferenceManager.getInstance().setPushAudioStream(false);
                    swOnPushAudioStream.closeSwitch();
                } else {
                    PreferenceManager.getInstance().setPushAudioStream(true);
                    swOnPushAudioStream.openSwitch();
                }
                break;
            case R.id.btn_upload_log:
                 sendLogThroughMail();
                 break;
            case R.id.btn_myInfo:
                Intent intent = new Intent(SettingActivity.this, MyInfoActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.btn_set_cdn_url:
                if(PreferenceManager.getInstance().isPushCDN()){
                    show_set_cdnUrl_dialog();
                }else{
                    Toast.makeText(getApplicationContext(), "请先设开启推流到CDN", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            if(data != null) {
                String name = data.getStringExtra("nickName");
                IDView.setText(name);
                String headImage = data.getStringExtra("headImage");
                url = DemoApplication.baseurl;
                url = url + headImage;
                loadImage();
            }
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

    private void show_set_cdnUrl_dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(SettingActivity.this, R.layout.activity_nickname_editshow, null);
        dialog.setView(dialogView);
        TextView textView = dialogView.findViewById(R.id.info_view);
        textView.setText("输入推流CDN地址");
        EditText editText = dialogView.findViewById(R.id.nickname_text);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        dialog.show();

        final Button btn_ok = dialogView.findViewById(R.id.btn_ok_nickname);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel_nickname);
        if(PreferenceManager.getInstance().getCDNUrl().length()>0){
            editText.setText(PreferenceManager.getInstance().getCDNUrl());
        }else{
            editText.setHint("请输入推流cdn url");
        }

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
               String cdnurl = editText.getText().toString().trim();
                if(cdnurl.length() == 0){
                    Toast.makeText(getApplicationContext(), "推流地址不允许为空!",
                            Toast.LENGTH_SHORT).show();
                }else {
                    dialog.dismiss();
                    PreferenceManager.getInstance().setCDNUrl(cdnurl);
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 获取网络图片资源
     * @return图片资源
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
                    imageView.setImageBitmap(bitmap);
                }
            }
        }.execute(url);
    }
}
