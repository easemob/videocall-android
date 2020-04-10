package com.easemob.videocall.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.easemob.videocall.R;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 04/10/2020
 */


public class SwitchAudioDialog extends BaseLiveDialogFragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private TextView btn_loudspeaker;  //扬声器
    private TextView btn_builtIn;    //内置
    private TextView btn_bluetooth;  //蓝牙耳机
    private TextView btn_cancel;
    private String username;
    private ConferenceActivity  activity;

    public static SwitchAudioDialog getNewInstance(String username) {
        SwitchAudioDialog dialog = new SwitchAudioDialog();
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_switchdevice;
    }

    @Override
    public void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if (bundle != null) {
            username = bundle.getString("username");
        }
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        btn_loudspeaker = findViewById(R.id.btn_loudspeaker);
        btn_builtIn = findViewById(R.id.btn_builtIn);
        btn_cancel = findViewById(R.id.bnt_cancel_switch_device);
    }


    @Override
    public void initListener() {
        super.initListener();
        btn_loudspeaker.setOnClickListener(this);
        btn_builtIn.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_loudspeaker:
                onLoudspeaker();
                break;
            case R.id.btn_builtIn:
                 onBuiltIn();
                 break;
            case R.id.bnt_cancel_switch_device:
                 dismiss();
                 break;
        }
    }

    public void setAppCompatActivity(ConferenceActivity  activity){
        this.activity = activity;
    }

    /**
     * 音频外放
     */
    private void onLoudspeaker() {
        activity.speakSwitch(0);
        dismiss();
    }

    /**
     * 音频内置播放
     */
    private void onBuiltIn() {
        activity.speakSwitch(1);
        dismiss();
    }

    /**
     * 蓝牙耳机
     */
    private void onBluetooth() {
        activity.speakSwitch(2);
        dismiss();
    }
}


