package com.easemob.videocall.ui;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.R;
import com.easemob.videocall.adapter.EaseBaseAdapter;
import com.easemob.videocall.adapter.TalkerItemAdapter;
import com.easemob.videocall.utils.ConferenceAttributeOption;
import com.easemob.videocall.utils.ConferenceInfo;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */


public class SetTalkerItemDialog extends BaseLiveDialogFragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private TextView tvMute;
    private TextView tvSetAdmin;
    private TextView tvCancel;
    private TextView tvRemoveMeeting;
    private String username;
    private int position;
    private EMConferenceStream conferenceStream;
    private TalkerItemAdapter adapter;

    public static SetTalkerItemDialog getNewInstance(String username) {
        SetTalkerItemDialog dialog = new SetTalkerItemDialog();
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_set_talker_item;
    }

    @Override
    public void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            username = bundle.getString("username");
            position = bundle.getInt("position");
            conferenceStream = ConferenceInfo.getInstance().getTalkerList().get(position);
        }
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        tvMute = findViewById(R.id.cancel_mute);
        tvSetAdmin = findViewById(R.id.set_admin);
        tvRemoveMeeting = findViewById(R.id.remove_meeting);
        tvCancel = findViewById(R.id.tv_cancel);
        if(conferenceStream.isAudioOff()){
            tvMute.setText("解除静音");
        }else {
            tvMute.setText("静音");
        }
        //已经是管理员
        if(ConferenceInfo.getInstance().getAdmins().contains(username) || username.equals(EMClient.getInstance().getCurrentUser())){
            tvRemoveMeeting.setVisibility(View.GONE);
            tvSetAdmin.setVisibility(View.GONE);
        }
    }


    @Override
    public void initListener() {
        super.initListener();
        tvMute.setOnClickListener(this);
        tvSetAdmin.setOnClickListener(this);
        tvRemoveMeeting.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_mute :
                onCancel_mute();
                break;
            case R.id.set_admin :
                setAdmin();
                break;
            case R.id.remove_meeting:
                onRemove_meeting();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }

    public void setAdapter(TalkerItemAdapter adapter){
        this.adapter = adapter;
    }

    /**
     * 解除静音
     */
    private void onCancel_mute() {
        if(EMClient.getInstance().getCurrentUser().equals(username)){
            dismiss();
            return;
        }
        tvMute.setClickable(false);
        EMConferenceMember reqInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(username);
        if (conferenceStream.isAudioOff()) {
            EMClient.getInstance().conferenceManager().unmuteMember(reqInfo.memberId);
        } else {
            EMClient.getInstance().conferenceManager().muteMember(reqInfo.memberId);
        }
        tvMute.setClickable(true);
        dismiss();
    }

    /**
     * 移除会议
     */
    private void onRemove_meeting(){
        List<String> members = new ArrayList<>();
        members.add(conferenceStream.getMemberName());
        EMClient.getInstance().conferenceManager().kickMember(ConferenceInfo.getInstance().getConference().getConferenceId()
                , members, new EMValueCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                        EMLog.i(TAG, "kickMember success, result: " + value);
                        if(ConferenceInfo.getInstance().getConferenceStreamList().contains(conferenceStream)){
                            ConferenceInfo.getInstance().getConferenceStreamList().remove(conferenceStream);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.updataData();
                            }
                        });
                        dismiss();
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.i(TAG, "kickMember failed, error: " + error + " - " + errorMsg);
                        dismiss();
                    }
                });
    }

    /**
     * 设置管理员
     */
    private void setAdmin(){
        if(ConferenceInfo.getInstance().getAdmins() != null && ConferenceInfo.getInstance().getAdmins().size() > 0){
            if(!ConferenceInfo.getInstance().getAdmins().contains(username)){
                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), username);
                EMClient.getInstance().conferenceManager().grantRole(ConferenceInfo.getInstance().getConference().getConferenceId()
                        , new EMConferenceMember(memName, null, null, null)
                        , EMConferenceManager.EMConferenceRole.Admin, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                                EMLog.i(TAG, "onAttributesUpdated  request_tobe_audience changeRole success, result: " + value);
                                if(!ConferenceInfo.getInstance().getAdmins().contains(conferenceStream.getUsername())){
                                    ConferenceInfo.getInstance().getAdmins().add(conferenceStream.getUsername());
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyItemChanged(position);
                                    }
                                });
                                dismiss();
                            }

                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.i(TAG, "onAttributesUpdated  request_tobe_audience failed, error: " + error + " - " + errorMsg);
                                dismiss();
                            }
                        });
            }
        }
    }
}
