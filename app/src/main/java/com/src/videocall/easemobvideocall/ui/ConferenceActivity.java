package com.src.videocall.easemobvideocall.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceAttribute;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.chat.EMStreamStatistics;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;
import com.src.videocall.easemobvideocall.Constant;
import com.src.videocall.easemobvideocall.DemoHelper;
import com.src.videocall.easemobvideocall.R;
import com.src.videocall.easemobvideocall.adapter.ChooseTalkerItemAdapter;
import com.src.videocall.easemobvideocall.adapter.EaseBaseRecyclerViewAdapter;
import com.src.videocall.easemobvideocall.adapter.MemberAvatarAdapter;
import com.src.videocall.easemobvideocall.adapter.OnItemClickListener;
import com.src.videocall.easemobvideocall.adapter.OnItemGetSurfaceView;
import com.src.videocall.easemobvideocall.adapter.TalkerItemAdapter;
import com.src.videocall.easemobvideocall.runtimepermissions.PermissionsManager;
import com.src.videocall.easemobvideocall.runtimepermissions.PermissionsResultAction;
import com.src.videocall.easemobvideocall.utils.ConferenceAttributeOption;
import com.src.videocall.easemobvideocall.utils.ConferenceInfo;
import com.src.videocall.easemobvideocall.utils.PhoneStateManager;
import com.src.videocall.easemobvideocall.utils.PreferenceManager;
import com.superrtc.mediamanager.ScreenCaptureManager;
import com.superrtc.sdk.VideoView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class ConferenceActivity extends AppCompatActivity implements EMConferenceListener {

    private final String TAG = this.getClass().getSimpleName();

    private static final int STATE_AUDIENCE = 0;
    private static final int STATE_TALKER = 1;
    private ConferenceActivity activity;

    private TextView meeting_roomID;
    private TextView meeting_duration;
    private Button btn_mic;
    private Button btn_video;
    private Button btn_hangup;
    private Button btn_switch_camera;
    private Button btn_talker_list;
    private Button btn_even_wheat;

    private RecyclerView horizontalRecyclerView;
    private ImageView avatarView;
    private ImageView netInfoView;

    private RelativeLayout rootContainer;
    private LinearLayout bottomContainer;
    private LinearLayout topContainer;
    private RelativeLayout bottomContainer11;
    private LinearLayout bottomContainerView;

    private AlertDialog dialog;
    private AlertDialog dialog2;

    private static EMConferenceStream windowStream;

    protected EMCallSurfaceView oppositeSurface;
    private EMConferenceListener conferenceListener;

    private TimeHandler timeHandler;
    private AudioManager audioManager;
    private EMConference conference;
    private EMStreamParam normalParam;

    private List<EMConferenceStream> streamList;
    private MemberAvatarAdapter avatarAdapter;
    private EMCallSurfaceView newSurfaceView;
    private EMConferenceStream newConferenceStream;
    private EMConferenceStream localStream = null;


    private String choose_userId;
    private RadioButton choosed_checkbox  = null;

    private String inviter;
    // 如果该值不为null，则证明为群组入口的直播
    private String groupId;
    // 标识当前用户角色
    private EMConferenceManager.EMConferenceRole currentRole;
    // 标识当前上麦按钮状态
    private int btnState = STATE_AUDIENCE;

    private int currentIndex = -1;
    private boolean changeflag = false;
    private EMCallSurfaceView itemSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        streamList = ConferenceInfo.getInstance().getConferenceStreamList();
        localStream = ConferenceInfo.getInstance().getLocalStream();

        rootContainer = (RelativeLayout)findViewById(R.id.root_layout);

        topContainer = (LinearLayout) findViewById(R.id.ll_top_container);

        bottomContainer11 = (RelativeLayout) findViewById(R.id.ll_bottom);

        bottomContainerView = (LinearLayout) findViewById(R.id.surface_baseline);
        bottomContainer = (LinearLayout) findViewById(R.id.ll_surface_baseline);

        horizontalRecyclerView = (RecyclerView) findViewById(R.id.horizontalRecyclerView);

        LinearLayoutManager layout = new LinearLayoutManager(ConferenceActivity.this);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);
        horizontalRecyclerView.setLayoutManager(layout);

        avatarAdapter = new MemberAvatarAdapter();
        streamList.clear();
        avatarAdapter.setData(streamList);
        horizontalRecyclerView.setAdapter(avatarAdapter);

        avatarAdapter.setOnItemClickListener(new OnItemClickListener() {
                                                 @Override
                                                 public void onItemClick(View view, int position) {
                                                     itemSurfaceView = view.findViewById(R.id.item_surface_view);
               /* if(currentIndex == -1) {
                    currentIndex = position;
                    EMClient.getInstance().conferenceManager().setLocalSurfaceView(itemSurfaceView);
                    EMClient.getInstance().conferenceManager().subscribe(streamList.get(currentIndex), oppositeSurface, new EMValueCallBack<String>() {
                        @Override
                        public void onSuccess(String value) {
                        }
                        @Override
                        public void onError(int error, String errorMsg) {

                        }
                    });
                    changeflag = true;
                }else{
                    if(currentIndex == position){
                        if(!changeflag){
                            EMClient.getInstance().conferenceManager().setLocalSurfaceView(itemSurfaceView);
                            EMClient.getInstance().conferenceManager().subscribe(streamList.get(currentIndex), oppositeSurface, new EMValueCallBack<String>() {
                                @Override
                                public void onSuccess(String value) {
                                }
                                @Override
                                public void onError(int error, String errorMsg) {

                                }
                            });
                            changeflag = !changeflag;
                        }else{
                            EMClient.getInstance().conferenceManager().setLocalSurfaceView(oppositeSurface);
                            EMClient.getInstance().conferenceManager().subscribe(streamList.get(currentIndex), itemSurfaceView, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                            }
                            @Override
                            public void onError(int error, String errorMsg) {

                            }
                        });
                        changeflag = !changeflag;
                       }
                    }else{
                        EMClient.getInstance().conferenceManager().subscribe(streamList.get(currentIndex), itemSurfaceView, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                            }
                            @Override
                            public void onError(int error, String errorMsg) {

                            }
                        });

                        currentIndex = position;
                        EMClient.getInstance().conferenceManager().setLocalSurfaceView(itemSurfaceView);
                        EMClient.getInstance().conferenceManager().subscribe(streamList.get(currentIndex), oppositeSurface, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                            }
                            @Override
                            public void onError(int error, String errorMsg) {

                            }
                        });
                        changeflag = true;
                    }
                }
            }*/
                                                 }
        });

        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        horizontalRecyclerView.addItemDecoration(decoration);

        //申请权限
        requestPermissions();

        init();
        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);
    }
    /*
     初始化
     */
    private void init() {
        activity = this;
        meeting_roomID = (TextView) findViewById(R.id.Meeting_roomID);
        meeting_duration = (TextView) findViewById(R.id.Meeting_duration);
        netInfoView = (ImageView)findViewById(R.id.netInfo);
        oppositeSurface = (EMCallSurfaceView) findViewById(R.id.opposite_surface);
        oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);

        avatarView = (ImageView) findViewById(R.id.img_call_avatar);
        EMClient.getInstance().conferenceManager().setLocalSurfaceView(oppositeSurface);

        btn_mic = (Button) findViewById(R.id.btn_call_mic);
        btn_video = (Button) findViewById(R.id.btn_call_video);
        btn_hangup = (Button) findViewById(R.id.btn_call_hangup);
        btn_switch_camera = (Button) findViewById(R.id.btn_switch_camera);
        btn_talker_list = (Button) findViewById(R.id.btn_talker_list);
        btn_even_wheat = (Button) findViewById(R.id.btn_even_wheat);

        meeting_roomID.setText(ConferenceInfo.getInstance().getRoomname());
        conference = ConferenceInfo.getInstance().getConference();
        currentRole = conference.getConferenceRole();
        timeHandler = new TimeHandler();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        btn_mic.setOnClickListener(listener);
        btn_video.setOnClickListener(listener);
        btn_hangup.setOnClickListener(listener);
        btn_switch_camera.setOnClickListener(listener);
        btn_talker_list.setOnClickListener(listener);
        btn_even_wheat.setOnClickListener(listener);
        rootContainer.setOnClickListener(listener);

        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);

        conferenceListener = this;

        //根据设置配置是否开关
        if(PreferenceManager.getInstance().isCallAudio()){
            normalParam.setAudioOff(false);
            localStream.setAudioOff(false);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
        }else{
            normalParam.setAudioOff(true);
            localStream.setAudioOff(true);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
        }
        if(PreferenceManager.getInstance().isCallVideo()){
            normalParam.setVideoOff(false);
            localStream.setVideoOff(false);
            btn_video.setBackgroundResource(R.drawable.em_call_video_on);
        }else{
            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);
        }
        avatarView.setVisibility(View.GONE);
        oppositeSurface.setVisibility(View.VISIBLE);

        btn_mic.setActivated(normalParam.isAudioOff());
        btn_video.setActivated(normalParam.isVideoOff());

        openSpeaker();
        startAudioTalkingMonitor();
        // 加入会议的成员身份为主播
        if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Talker || conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin) {
            publish();

            // 设置连麦按钮为‘申请下麦’
            setRequestBtnState(STATE_TALKER);
        }else if(conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience){
            //设置连麦按钮为上麦按钮
            setRequestBtnState(STATE_AUDIENCE);
        }
        timeHandler.startTime();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_call_mic:
                    voiceSwitch();
                    break;
                case R.id.btn_call_video:
                    videoSwitch();
                    break;
                case R.id.btn_switch_camera:
                    changeCamera();
                    break;
                case R.id.btn_call_hangup:
                    exitConference();
                    break;
                case R.id.btn_talker_list:
                    opentalkerlist();
                    break;
                case R.id.btn_even_wheat:
                     requesteven_wheat();
                     break;
                case R.id.root_layout:
                    if (bottomContainer.getVisibility() == View.VISIBLE) {
                        topContainer.setVisibility(View.GONE);

                        if(streamList.size() > 0){
                            RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(),100));
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            bottomContainer11.setLayoutParams(params);
                            bottomContainer11.setVisibility(View.VISIBLE);
                            bottomContainer.setVisibility(View.GONE);
                            bottomContainerView.setVisibility(View.VISIBLE);
                            oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                        }else{
                            RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(),0));
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            bottomContainer11.setLayoutParams(params);
                            bottomContainer11.setVisibility(View.GONE);
                            bottomContainer.setVisibility(View.GONE);
                            bottomContainerView.setVisibility(View.GONE);
                            oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                        }
                    } else {
                        if(streamList.size() > 0) {
                            topContainer.setVisibility(View.VISIBLE);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 160));
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            bottomContainer11.setLayoutParams(params);
                            bottomContainer11.setVisibility(View.VISIBLE);
                            bottomContainer.setVisibility(View.VISIBLE);
                            bottomContainerView.setVisibility(View.VISIBLE);
                            oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                        }else{
                            topContainer.setVisibility(View.VISIBLE);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 60));
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            bottomContainer11.setLayoutParams(params);
                            bottomContainer11.setVisibility(View.VISIBLE);
                            bottomContainer.setVisibility(View.VISIBLE);
                            bottomContainerView.setVisibility(View.GONE);
                            oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                        }
                    }
                    break;
                  default:
                      break;
            }
        }
    };

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 开始推自己的数据
     */
    private void publish() {
        EMLog.i(TAG, "publish start, params: " + normalParam.toString());

        setIcons(EMConferenceManager.EMConferenceRole.Talker);
        // 确保streamList中的stream跟viewGroup中的view位置对应。
        addOrUpdateStreamList(null, "local-stream");

        EMClient.getInstance().conferenceManager().publish(normalParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                conference.setPubStreamId(value, EMConferenceStream.StreamType.NORMAL);
                //localView.setStreamId(value);
                addOrUpdateStreamList("local-stream", value);

                // Start to watch the phone call state.
                PhoneStateManager.get(ConferenceActivity.this).addStateCallback(phoneStateCallback);
            }
            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "publish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }

    /**
     * 停止推自己的数据
     */
    private void unpublish(final String publishId) {
        EMClient.getInstance().conferenceManager().unpublish(publishId, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                    EMLog.e(TAG, "unpublish scuessed ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setIcons(EMConferenceManager.EMConferenceRole.Audience);
                        }
                    });
                }
            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "unpublish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }

    private void setIcons(EMConferenceManager.EMConferenceRole role) {
        if (role == EMConferenceManager.EMConferenceRole.Audience) {
            btn_switch_camera.setVisibility(View.GONE);
            avatarView.setVisibility(View.VISIBLE);
            oppositeSurface.setVisibility(View.GONE);
        } else {
            btn_switch_camera.setVisibility(View.VISIBLE);
            oppositeSurface.setVisibility(View.VISIBLE);
            avatarView.setVisibility(View.GONE);
        }
    }

    /**
     * 语音开关
     */
    private void voiceSwitch() {
        if (normalParam.isAudioOff()) {
            normalParam.setAudioOff(false);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
            localStream.setAudioOff(false);
            EMClient.getInstance().conferenceManager().openVoiceTransfer();
        } else {
            normalParam.setAudioOff(true);
            localStream.setAudioOff(true);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
        }
    }

    /**
     * 视频开关
     */
    private void videoSwitch() {
        if (normalParam.isVideoOff()) {
            normalParam.setVideoOff(false);
            localStream.setVideoOff(false);
            btn_video.setBackgroundResource(R.drawable.em_call_video_on);
            avatarView.setVisibility(View.GONE);
            oppositeSurface.setVisibility(View.VISIBLE);
            btn_switch_camera.setVisibility(View.VISIBLE);
            EMClient.getInstance().conferenceManager().openVideoTransfer();
        } else {
            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);
            avatarView.setVisibility(View.VISIBLE);
            oppositeSurface.setVisibility(View.INVISIBLE);
            btn_switch_camera.setVisibility(View.GONE);
            EMClient.getInstance().conferenceManager().closeVideoTransfer();
        }
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        EMClient.getInstance().conferenceManager().switchCamera();
    }


    /**
     * 打开主播列表
     */
    private void opentalkerlist() {
        getConferenceInfo();
    }

    /**
     * 申请连麦 下麦
     */
    private void requesteven_wheat() {
        if (currentRole == EMConferenceManager.EMConferenceRole.Admin) {
            takerFullDialogDisplay();
           // takerListChooseDispaly();
            return;
        }

        if (btnState == STATE_AUDIENCE) { // 当前按钮状态是观众，需要变成主播
            if (currentRole == EMConferenceManager.EMConferenceRole.Audience) { // 发送消息，申请上麦
                EMClient.getInstance().conferenceManager().setConferenceAttribute(EMClient.getInstance().getCurrentUser(),
                                              ConferenceAttributeOption.REQUEST_TOBE_SPEAKER, new  EMValueCallBack<Void>(){
                    @Override
                    public void onSuccess(Void value) {
                        EMLog.e(TAG, "request_tobe_speaker scuessed");
                        //publish();
                        //setRequestBtnState(STATE_TALKER);
                    }
                    @Override
                    public void onError(int error, String errorMsg) {

                        EMLog.e(TAG, "request_tobe_speaker failed: error=" + error + ", msg=" + errorMsg);
                    }
                });
            } else { // 已经是主播，直接推流
                publish();
                setRequestBtnState(STATE_TALKER);
            }
        } else if (btnState == STATE_TALKER) { // 当前按钮状态是主播，需要下麦
            if (currentRole == EMConferenceManager.EMConferenceRole.Talker){ // 申请下麦
                EMClient.getInstance().conferenceManager().setConferenceAttribute(EMClient.getInstance().getCurrentUser()
                           ,ConferenceAttributeOption.REQUEST_TOBE_AUDIENCE, new  EMValueCallBack<Void>(){
                    @Override
                    public void onSuccess(Void value) {
                        EMLog.e(TAG, "request_tobe_audience scuessed");
                        //unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.NORMAL));
                        //setRequestBtnState(STATE_AUDIENCE);
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.e(TAG, "request_tobe_audience failed: error=" + error + ", msg=" + errorMsg);
                    }
                });
            }
        }
    }

    private void addOrUpdateStreamList(String originStreamId, String targetStreamId) {
        if (originStreamId != null) {
            localStream.setStreamId(targetStreamId);
        } else {
            localStream.setUsername(EMClient.getInstance().getCurrentUser());
            localStream.setStreamId(targetStreamId);
        }
    }

    private void setRequestBtnState(int state) {
        btnState = state;
        if (state == STATE_AUDIENCE) {
            btn_even_wheat.setBackgroundResource(R.drawable.em_call_request_connect);
        } else if (state == STATE_TALKER) {
            btn_even_wheat.setBackgroundResource(R.drawable.em_call_request_disconnect);
        }
    }
    /**
     * 退出会议
     */
    private void exitConference() {
        stopAudioTalkingMonitor();
        timeHandler.stopTime();

        // Stop to watch the phone call state.
        PhoneStateManager.get(ConferenceActivity.this).removeStateCallback(phoneStateCallback);
        if (currentRole == EMConferenceManager.EMConferenceRole.Admin) { // 管理员退出时销毁会议
            EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
                @Override
                public void onSuccess(Object value) {
                    EMLog.i(TAG, "destroyConference success");
                    finish();
                }
                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.e(TAG, "destroyConference failed " + error + ", " + errorMsg);
                    finish();
                }
            });
        }else{
            EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
                @Override
                public void onSuccess(Object value) {
                    finish();
                }
                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.e(TAG, "exit conference failed " + error + ", " + errorMsg);
                    finish();
                }
            });
        }
    }


    private void startAudioTalkingMonitor() {
        EMClient.getInstance().conferenceManager().startMonitorSpeaker(300);
    }

    private void stopAudioTalkingMonitor() {
        EMClient.getInstance().conferenceManager().stopMonitorSpeaker();
    }

    /**
     * 打开扬声器
     * 主要是通过扬声器的开关以及设置音频播放模式来实现
     * 1、MODE_NORMAL：是正常模式，一般用于外放音频
     * 2、MODE_IN_CALL：
     * 3、MODE_IN_COMMUNICATION：这个和 CALL 都表示通讯模式，不过 CALL 在华为上不好使，故使用 COMMUNICATION
     * 4、MODE_RINGTONE：铃声模式
     */
    public void openSpeaker() {
        // 检查是否已经开启扬声器
        if (!audioManager.isSpeakerphoneOn()) {
            // 打开扬声器
            audioManager.setSpeakerphoneOn(true);
        }
        // 开启了扬声器之后，因为是进行通话，声音的模式也要设置成通讯模式
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }


    /**
     * --------------------------------------------------------------------
     * 多人音视频会议回调方法
     */

    @Override
    public void onMemberJoined(final EMConferenceMember member) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), member.memberName + " joined conference!", Toast.LENGTH_SHORT).show();
                //updateConferenceMembers();
            }
        });
    }

    @Override
    public void onMemberExited(final EMConferenceMember member) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), member.memberName + " removed conference!", Toast.LENGTH_SHORT).show();
                if (EMClient.getInstance().getCurrentUser().equals(member.memberName)) {
                    setRequestBtnState(STATE_AUDIENCE);
                }
            }
        });
    }

    @Override
    public void onStreamAdded(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), stream.getUsername() + " stream add!", Toast.LENGTH_SHORT)
                        .show();
                addConferenceView(stream);
                if(streamList.size() > 0){
                    RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(),160));
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    bottomContainer11.setLayoutParams(params);
                    bottomContainer11.setVisibility(View.VISIBLE);
                    bottomContainer.setVisibility(View.VISIBLE);
                    bottomContainerView.setVisibility(View.VISIBLE);
                    oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                }
            }
        });
    }

    @Override
    public void onStreamRemoved(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), stream.getUsername() + " stream removed!", Toast.LENGTH_SHORT).show();
                if (streamList.contains(stream)) {
                    removeConferenceView(stream);
                    if(streamList.size() == 0){
                        RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(),60));
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        bottomContainer11.setLayoutParams(params);
                        bottomContainer11.setVisibility(View.VISIBLE);
                        bottomContainer.setVisibility(View.VISIBLE);
                        bottomContainerView.setVisibility(View.GONE);
                        oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                    }
                }
            }
        });
    }

    @Override
    public void onStreamUpdate(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), stream.getUsername() + " stream update!", Toast.LENGTH_SHORT).show();
                updateConferenceMemberView(stream);
            }
        });
    }

    @Override
    public void onPassiveLeave(final int error, final String message) { // 当前用户被踢出会议
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Passive exit " + error + ", message" + message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onConferenceState(final ConferenceState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "State=" + state, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStreamStatistics(EMStreamStatistics statistics) {
        EMLog.i(TAG, "onStreamStatistics" + statistics.toString());
    }

    @Override
    public void onStreamSetup(final String streamId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (streamId.equals(conference.getPubStreamId(EMConferenceStream.StreamType.NORMAL))
                        || streamId.equals(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP))) {
                    Toast.makeText(getApplicationContext(), "Publish setup streamId=" + streamId, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Subscribe setup streamId=" + streamId, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onSpeakers(final List<String> speakers) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //currSpeakers(speakers);
            }
        });
    }

    /**
     * 添加一个展示远端画面的 view
     */
    private void addConferenceView(EMConferenceStream stream) {
        EMLog.d(TAG, "add conference view -start- " + stream.getMemberName());
        avatarAdapter.addData(stream);

        //subscribe(stream,  holder.itemView.findViewById(R.id.item_surface_view));

        EMLog.d(TAG, "add conference view -end-" + stream.getMemberName());
    }

    /**
     * 移除指定位置的 View，移除时如果已经订阅需要取消订阅
     */
    private void removeConferenceView(EMConferenceStream stream) {
        avatarAdapter.removeData(stream);
    }

    /**
     * 订阅指定成员 stream
     */
    private void subscribe(EMConferenceStream stream, EMCallSurfaceView surfaceView) {
        EMClient.getInstance().conferenceManager().subscribe(stream, surfaceView, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
            }
            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    /**
     * 取消订阅指定成员 stream
     */
    private void unsubscribe(EMConferenceStream stream) {
        EMClient.getInstance().conferenceManager().unsubscribe(stream, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    private boolean isPublishing() {
        return  oppositeSurface!= null;
    }

    /**
     * 更新指定 View
     */
    private void updateConferenceMemberView(EMConferenceStream stream) {
        avatarAdapter.updataData();
    }


    /**
     * 收到其他人的会议邀请
     */
    @Override
    public void onReceiveInvite(final String confId, String password, String extension) {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Receive invite " + confId, Toast.LENGTH_LONG).show();
            }
        });*/
    }



    @Override
    public void onRoleChanged(EMConferenceManager.EMConferenceRole role) {
        EMLog.i(TAG, "onRoleChanged, role: " + role);
        currentRole = role;
        conference.setConferenceRole(role);
        if (role == EMConferenceManager.EMConferenceRole.Talker) {
            // 管理员把当前用户角色更改为主播或管理员。
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EMLog.i(TAG, "onRoleChanged, start publish, params: " + normalParam.toString());

                    publish();
                    setRequestBtnState(STATE_TALKER);
                }
            });
        } else if (role == EMConferenceManager.EMConferenceRole.Audience) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 下麦
                    unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.NORMAL));
                    setRequestBtnState(STATE_AUDIENCE);
                }
            });
        }else if(role == EMConferenceManager.EMConferenceRole.Admin){  //主播变更为管理员
            currentRole = role;
            EMClient.getInstance().conferenceManager().setConferenceAttribute(EMClient.getInstance().getCurrentUser(),
                    ConferenceAttributeOption.REQUEST_BECOME_ADMIN, new  EMValueCallBack<Void>(){
                        @Override
                        public void onSuccess(Void value) {
                            EMLog.e(TAG, "request_tobe_speaker scuessed");
                            //publish();
                            //setRequestBtnState(STATE_TALKER);
                        }
                        @Override
                        public void onError(int error, String errorMsg) {
                            EMLog.e(TAG, "request_tobe_speaker failed: error=" + error + ", msg=" + errorMsg);
                        }
                    });
        }

    }

    @Override
    public void onAttributesUpdated(EMConferenceAttribute[] attributes) {
        EMConferenceAttribute  conferenceAttribute;
        int size = attributes.length;
        for(int i =0; i< size;i++){
            conferenceAttribute = attributes[i];
            if(ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin){
                String usreId = conferenceAttribute.key;
                String option = conferenceAttribute.value;

                //申请上麦
                if(option.equals(ConferenceAttributeOption.REQUEST_TOBE_SPEAKER)){
                    if(streamList.size() >= 8){ //大于9个主播
                        //展示主播界面
                        takerFullDialogDisplay();
                    }else {
                        String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                        EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                                , new EMConferenceMember(memName, null, null)
                                , EMConferenceManager.EMConferenceRole.Talker, new EMValueCallBack<String>() {
                                    @Override
                                    public void onSuccess(String value) {
                                        EMLog.i(TAG, "changeRole success, result: " + value);
                                        EMClient.getInstance().conferenceManager().deleteConferenceAttribute(usreId, new EMValueCallBack<Void>(){
                                            @Override
                                            public void onSuccess(Void value) {
                                                EMLog.i(TAG, "delete role success, result: " + value);
                                            }
                                            @Override
                                            public void onError(int error, String errorMsg) {
                                                EMLog.i(TAG, "changeRole failed, error: " + error + " - " + errorMsg);
                                            }
                                        });
                                    }
                                    @Override
                                    public void onError(int error, String errorMsg) {
                                        EMLog.i(TAG, "changeRole failed, error: " + error + " - " + errorMsg);
                                    }
                                });
                    }
                }else if(option.equals(ConferenceAttributeOption.REQUEST_TOBE_AUDIENCE)){  //申请下麦
                    String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                    EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                            , new EMConferenceMember(memName, null, null)
                            , EMConferenceManager.EMConferenceRole.Audience, new EMValueCallBack<String>() {
                                @Override
                                public void onSuccess(String value) {
                                    EMLog.i(TAG, "changeRole success, result: " + value);
                                    EMClient.getInstance().conferenceManager().deleteConferenceAttribute(usreId, new EMValueCallBack<Void>(){
                                        @Override
                                        public void onSuccess(Void value) {
                                            EMLog.i(TAG, "delete role success, result: " + value);
                                        }
                                        @Override
                                        public void onError(int error, String errorMsg) {
                                            EMLog.i(TAG, "changeRole failed, error: " + error + " - " + errorMsg);
                                        }
                                    });
                                }
                                @Override
                                public void onError(int error, String errorMsg) {
                                    EMLog.i(TAG, "changeRole failed, error: " + error + " - " + errorMsg);
                                }
                            });
                }
            }
        }

    }

    /**
     * 主播已满 踢人下麦 提示对话框
     */
    private void takerFullDialogDisplay() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConferenceActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(ConferenceActivity.this, R.layout.activity_talker_full_kick, null);
        dialog.setView(dialogView);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        dialog.show();

        final Button btn_ok = dialogView.findViewById(R.id.btn_kick_ok);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_kick_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                dialog.dismiss();
                takerListChooseDispaly();
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
     * 连麦请求提示
     */


    /**
     * 展示管理员踢主播列表
     *
     */
    private void takerListChooseDispaly(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ConferenceActivity.this);
        final AlertDialog dialog2 = builder.create();
        View dialogView = View.inflate(ConferenceActivity.this, R.layout.activity_choose_talker, null);
        dialog2.setView(dialogView);
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams wmlp = dialog2.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.BOTTOM;

        dialog2.show();

        final Button btn_ok = dialogView.findViewById(R.id.choose_ok);
        final Button btn_cancel = dialogView.findViewById(R.id.choose_cancel);
        final RecyclerView recyclerView = dialogView.findViewById(R.id.chose_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(dialogView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        ChooseTalkerItemAdapter adapter = new ChooseTalkerItemAdapter();
        adapter.setData(streamList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView userId_view = view.findViewById(R.id.chooseId_view);
                RadioButton id_checkbox = view.findViewById(R.id.id_checkbox);
                if(choosed_checkbox != null){
                    choosed_checkbox.setChecked(false);
                }
                id_checkbox.setChecked(true);
                choose_userId = userId_view.getText().toString();
                choosed_checkbox = view.findViewById(R.id.id_checkbox);
            }
        });

        DividerItemDecoration decoration = new DividerItemDecoration(dialogView.getContext(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        recyclerView.addItemDecoration(decoration);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), choose_userId);
                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                        , new EMConferenceMember(memName, null, null)
                        , EMConferenceManager.EMConferenceRole.Audience, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                                EMLog.i(TAG, "changeRole success, result: " + value);
                            }
                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.i(TAG, "changeRole failed, error: " + error + " - " + errorMsg);
                            }
                        });
                dialog2.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();
            }
        });
    }

    private String getMembersStr(List<EMConferenceMember> members) {
        String result = "";
        for (int i = 0; i < members.size(); i++) {
            result += EasyUtils.useridFromJid(members.get(i).memberName);
            if (i < members.size() - 1) {
                result += ", ";
            }
        }
        return result;
    }

    // 当前设备通话状态监听器
    PhoneStateManager.PhoneStateCallback phoneStateCallback = new PhoneStateManager.PhoneStateCallback() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:   // 电话响铃
                    break;
                case TelephonyManager.CALL_STATE_IDLE:      // 电话挂断
                    // resume current voice conference.
                    if (normalParam.isAudioOff()) {
                        try {
                            EMClient.getInstance().callManager().resumeVoiceTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    if (normalParam.isVideoOff()) {
                        try {
                            EMClient.getInstance().callManager().resumeVideoTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:   // 来电接通 或者 去电，去电接通  但是没法区分
                    // pause current voice conference.
                    if (!normalParam.isAudioOff()) {
                        try {
                            EMClient.getInstance().callManager().pauseVoiceTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!normalParam.isVideoOff()) {
                        try {
                            EMClient.getInstance().callManager().pauseVideoTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };


    /**
     * 会议房间设置
     */
    public void onSettingRoom(View view){
        EMClient.getInstance().conferenceManager().getConferenceInfo(ConferenceInfo.getInstance().getConference().getConferenceId(),ConferenceInfo.getInstance().getPassword(),
                new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(EMConference value) {
                        ConferenceInfo.getInstance().getConference().setTalkers(value.getTalkers());
                        ConferenceInfo.getInstance().getConference().setAudienceTotal(value.getAudienceTotal());
                        ConferenceInfo.getInstance().getConference().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().getConference().setMemberNum(value.getMemberNum());
                        Intent intent = new Intent(ConferenceActivity.this, RoomSettingActivity.class);
                        startActivity(intent);
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.e(TAG, "getConferenceInfo failed: error=" + error + ", msg=" + errorMsg);
                    }
                });
    }


    /**
     * 获取会议信息
     */
    private void getConferenceInfo(){
        EMClient.getInstance().conferenceManager().getConferenceInfo(ConferenceInfo.getInstance().getConference().getConferenceId(),ConferenceInfo.getInstance().getPassword(),
                new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(EMConference value) {
                        ConferenceInfo.getInstance().getConference().setTalkers(value.getTalkers());
                        ConferenceInfo.getInstance().getConference().setAudienceTotal(value.getAudienceTotal());
                        ConferenceInfo.getInstance().getConference().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().getConference().setMemberNum(value.getMemberNum());
                        Intent intent = new Intent(ConferenceActivity.this, TalkerListActivity.class);
                        startActivity(intent);
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.e(TAG, "getConferenceInfo failed: error=" + error + ", msg=" + errorMsg);
                    }
                });
    }


    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    public void checkWifiState() {
        if (isWifiConnect()) {
            WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int wifi = mWifiInfo.getRssi();//获取wifi信号强度
            if (wifi > -50 && wifi < 0) {//最强
                //Log.e(TAG, "最强");
                netInfoView.setBackgroundResource(R.drawable.networkinfo);
            } else if (wifi > -70 && wifi < -50) {//较强
                //Log.e(TAG, "较强");
                netInfoView.setBackgroundResource(R.drawable.networkinfo);
            } else if (wifi > -80 && wifi < -70) {//较弱
                //Log.e(TAG, "较弱");
                netInfoView.setBackgroundResource(R.drawable.networkinfo);
            } else if (wifi > -100 && wifi < -80) {//微弱
                //Log.e(TAG, "微弱");
                netInfoView.setBackgroundResource(R.drawable.networkinfo);
            }
        } else {
            //无连接
            //Log.e(TAG, "无wifi连接");
            netInfoView.setBackgroundResource(R.drawable.networkinfo);
        }
    }

    private void updateConferenceTime(String time) {
        meeting_duration.setText(time);
        checkWifiState();
    }

    private class TimeHandler extends Handler {
        private final int MSG_TIMER = 0;
        private DateFormat dateFormat = null;
        private int timePassed = 0;

        public TimeHandler() {
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public void startTime() {
            sendEmptyMessageDelayed(MSG_TIMER, 1000);
        }

        public void stopTime() {
            removeMessages(MSG_TIMER);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TIMER) {
                // TODO: update calling time.
                timePassed++;
                String time = dateFormat.format(timePassed * 1000);
                updateConferenceTime(time);
                sendEmptyMessageDelayed(MSG_TIMER, 1000);
                return;
            }
            super.handleMessage(msg);
        }
    }

    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(String permission) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        EMClient.getInstance().conferenceManager().removeConferenceListener(conferenceListener);

        DemoHelper.getInstance().logout(true,new EMCallBack(){
            @Override
            public void onSuccess() {
                EMLog.e(TAG, "im logout scuessfull");
            }

            @Override
            public void onProgress(int progress, String status){

            }
            @Override
            public void onError(int code, String message) {
                EMLog.e(TAG, "im logout failed" + code + ", msg " + message);

            }
        });
        super.onDestroy();
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setMicrophoneMute(false);
    }
}
