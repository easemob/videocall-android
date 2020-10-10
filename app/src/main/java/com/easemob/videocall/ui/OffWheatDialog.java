package com.easemob.videocall.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.easemob.videocall.R;

public class OffWheatDialog  extends BaseLiveDialogFragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    private TextView btn_off_wheat;
    private TextView btn_cancel;
    private String username;
    private ConferenceActivity  activity;

    public static OffWheatDialog getNewInstance(String username) {
        OffWheatDialog dialog = new OffWheatDialog();
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_off_wheat;
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
        btn_off_wheat = findViewById(R.id.btn_off_wheat);
        btn_cancel = findViewById(R.id.bnt_cancel_option);
    }


    @Override
    public void initListener() {
        super.initListener();
        btn_off_wheat.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_off_wheat:
                setBtn_off_wheat();
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
     * 确认下麦
     */
    private void setBtn_off_wheat() {
        activity.set_off_wheat();
        dismiss();
    }

}

