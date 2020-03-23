package com.easemob.videocall.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

        //upload button
        Button uploadlog = (Button)findViewById(R.id.btn_upload_log);
        uploadlog.setOnClickListener(this);

        Button myInfo = (Button)findViewById(R.id.btn_myInfo);
        myInfo.setOnClickListener(this);

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
        Camera mCameraDevice = null;
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
            if (mCameraDevice != null) {
                mCameraDevice.release();
            }
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
            case R.id.btn_upload_log:
                 sendLogThroughMail();
                 break;
            case R.id.btn_myInfo:
                Intent intent = new Intent(SettingActivity.this, InfoActivity.class);
                startActivityForResult(intent, 1);
                startActivity(intent);
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
                    imageView.setImageBitmap(bitmap);
                }
            }
        }.execute(url);
    }
}
