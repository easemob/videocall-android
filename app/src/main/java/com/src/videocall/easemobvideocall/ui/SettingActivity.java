package com.src.videocall.easemobvideocall.ui;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.model.EaseCompat;
import com.hyphenate.easeui.widget.EaseSwitchButton;
import com.src.videocall.easemobvideocall.R;
import com.src.videocall.easemobvideocall.utils.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.superrtc.mediamanager.EMediaManager.getContext;

public class SettingActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TextView  IDView = (TextView)findViewById(R.id.nickname_edit);
        initCameraResolutionSpinner(R.id.spinner_video_resolution);

        String username = PreferenceManager.getInstance().getCurrentUsername();
        if(username != null){
            IDView.setText(username);
        }else{
            IDView.setText("未注册");
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

            /**
             * Spinner onItemSelected is obscure
             * setSelection will trigger onItemSelected
             * if spinner.setSelection(newValue)'s newValue == spinner.getSelectionPosition(), it will not trigger onItemSelected
             *
             * The target we want to archive are:
             * 1. select one spinner, clear another
             * 2. set common text
             * 3. if another spinner is already at position 0, ignore it
             * 4. Use disableOnce AtomicBoolean to record whether is spinner.setSelected(x) triggered action, which should be ignored
             */
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
}
