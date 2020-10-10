package com.easemob.videocall.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.DemoHelper;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.ConferenceMemberInfo;
import com.easemob.videocall.utils.ConferenceSession;

import java.util.List;

import static com.superrtc.ContextUtils.getApplicationContext;

public class Desktop_share_Dialog extends BaseLiveDialogFragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private TextView btn_desktop_share;
    private TextView btn_white_board;
    private TextView btn_cancel;
    private String username;
    private ConferenceActivity  activity;

    private static int setDesktop_shareType = -1;

    public static Desktop_share_Dialog getNewInstance(String username) {
        Desktop_share_Dialog dialog = new Desktop_share_Dialog();
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static int getDesktop_shareType() {
        return setDesktop_shareType;
    }

    public static void setDesktop_shareType(int desktop_share) {
       setDesktop_shareType = desktop_share;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_desktop_share;
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
        btn_desktop_share = findViewById(R.id.btn_desktop_share);
        btn_white_board = findViewById(R.id.btn_whiteboard);
        btn_cancel = findViewById(R.id.bnt_cancel);
    }


    @Override
    public void initListener() {
        super.initListener();
        btn_desktop_share.setOnClickListener(this);
        btn_white_board.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_desktop_share:
                setDesktop_shareType = 0;
                onDeskTopShare();

                break;
            case R.id.btn_whiteboard:
                setDesktop_shareType = 1;
                onWhiteBoard();
                break;
            case R.id.bnt_cancel:
                dismiss();
                break;
        }
    }

    public void setAppCompatActivity(ConferenceActivity  activity){
        this.activity = activity;
    }

    /**
     * 开始共享桌面
     */
    private void onDeskTopShare() {
        List<ConferenceMemberInfo> memberInfos = DemoHelper.getInstance().getConferenceSession().getConferenceProfiles();
        for(int i = 0 ; i < memberInfos.size(); i++){
            ConferenceMemberInfo memberInfo = memberInfos.get(i);
            if(memberInfo.isDesktop() || memberInfo.isWhiteboard()){
                if(ConferenceInfo.whiteboardCreator){
                    Toast.makeText(getApplicationContext(), "请先结束白板再共享桌", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "当前房间已经有人在共享", Toast.LENGTH_SHORT).show();
                }

                dismiss();
                setDesktop_shareType = -1;
                return;
            }
        }
        activity.screenShare();
        dismiss();
    }

    /**
     * 开始白板
     */
    private void onWhiteBoard() {
        List<ConferenceMemberInfo> memberInfos = DemoHelper.getInstance().getConferenceSession().getConferenceProfiles();
        for(int i = 0 ; i < memberInfos.size(); i++){
            ConferenceMemberInfo memberInfo = memberInfos.get(i);
            if(memberInfo.isDesktop()){
                    dismiss();
                    return;
            }
        }
        activity.whiteboard_option(ConferenceInfo.getInstance().getRoomname(),
                ConferenceInfo.getInstance().getPassword());
        dismiss();
    }
}
