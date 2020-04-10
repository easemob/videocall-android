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


public class HangUpDialog extends BaseLiveDialogFragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private TextView btn_leave;
    private TextView btn_end;
    private TextView btn_cancel;
    private String username;
    private ConferenceActivity  activity;

    public static HangUpDialog getNewInstance(String username) {
        HangUpDialog dialog = new HangUpDialog();
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_hangup;
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
        btn_leave = findViewById(R.id.btn_leave_meeting);
        btn_end = findViewById(R.id.btn_destory_meeting);
        btn_cancel = findViewById(R.id.bnt_cancel_option);
    }


    @Override
    public void initListener() {
        super.initListener();
        btn_leave.setOnClickListener(this);
        btn_end.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_leave_meeting:
                onLeaveConference();
                break;
            case R.id.btn_destory_meeting:
                onDestoryConference();
                break;
            case R.id.bnt_cancel_option:
                dismiss();
                break;
        }
    }

    public void setAppCompatActivity(ConferenceActivity  activity){
        this.activity = activity;
    }

    /**
     * 离开会议
     */
    private void onLeaveConference() {
        activity.exitConference();
        dismiss();
    }

    /**
     * 结束会议
     */
    private void onDestoryConference() {
        activity.destoryConference();
        dismiss();
    }
}

