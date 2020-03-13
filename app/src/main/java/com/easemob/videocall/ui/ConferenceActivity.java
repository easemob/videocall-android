package com.easemob.videocall.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.utils.ConferenceMemberInfo;
import com.easemob.videocall.utils.ConferenceSession;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceAttribute;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.chat.EMStreamStatistics;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;

import com.easemob.videocall.DemoHelper;
import com.easemob.videocall.R;
import com.easemob.videocall.adapter.ChooseTalkerItemAdapter;
import com.easemob.videocall.adapter.MemberAvatarAdapter;
import com.easemob.videocall.adapter.OnItemClickListener;
import com.easemob.videocall.adapter.OnItemGetSurfaceView;
import com.easemob.videocall.runtimepermissions.PermissionsManager;
import com.easemob.videocall.runtimepermissions.PermissionsResultAction;
import com.easemob.videocall.utils.ConferenceAttributeOption;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.PhoneStateManager;
import com.easemob.videocall.utils.PreferenceManager;
import com.jaouan.compoundlayout.CompoundLayout;
import com.jaouan.compoundlayout.RadioLayoutGroup;

import com.superrtc.sdk.VideoView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static android.icu.lang.UCharacter.JoiningType.TRANSPARENT;

public class ConferenceActivity extends Activity implements EMConferenceListener {

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
    private Button btn_speaker_setting;

    private RecyclerView horizontalRecyclerView;
    private ImageView avatarView;
    private ImageView netInfoView;

    private ImageView  video_show_view;
    private ImageView  speak_show_view;

    private RelativeLayout rootContainer;
    private LinearLayout bottomContainer;
    private LinearLayout topContainer;
    private RelativeLayout bottomContainer11;
    private HorizontalScrollView bottomContainerView;

    public static EMCallSurfaceView oppositeSurface;
    private EMConferenceListener conferenceListener;

    private TimeHandler timeHandler;
    private AudioManager audioManager;
    private EMConference conference;
    private EMStreamParam normalParam;
    private int FitSate = 0;

    private List<EMConferenceStream> streamList;
    private MemberAvatarAdapter avatarAdapter;
    private EMConferenceStream localStream = null;

    private boolean firstSubscribestream = false;

    private String choose_userId;
    private RadioButton choosed_checkbox  = null;

    private String inviter;
    // 如果该值不为null，则证明为群组入口的直播
    private String groupId;

    // 标识当前上麦按钮状态
    private int btnState = STATE_AUDIENCE;

    private EMCallSurfaceView itemSurfaceView;
    private EMCallSurfaceView oldSurfaceView;
    private EMCallSurfaceView firstSurfaceView;

    private MultiMemberView localViewContainer;
    private RadioLayoutGroup memberContainer;
    private FrameLayout largeSurfacePreview;
    private Map<String, Integer> mMemberViewIds = new HashMap<>();
    private Set<String> imMembers = new HashSet<>();
    private ConferenceSession conferenceSession;

    private boolean oldflag = false;

    private boolean Initflag = false;
    public static int updateIndex = -2;


    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EMLog.i(TAG,"oncreate  ConferenceActivity  Main threadID: " + Thread.currentThread().getName());
        if (savedInstanceState != null) {
            EMLog.d(TAG, "onCreate savedInstanceState");
            finish();
            return;
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        streamList = ConferenceInfo.getInstance().getConferenceStreamList();
        localStream = ConferenceInfo.getInstance().getLocalStream();

        meeting_roomID = (TextView) findViewById(R.id.Meeting_roomID);
        meeting_roomID.setText(ConferenceInfo.getInstance().getRoomname());
        conference = ConferenceInfo.getInstance().getConference();

        rootContainer = (RelativeLayout)findViewById(R.id.root_layout);
        topContainer = (LinearLayout) findViewById(R.id.ll_top_container);
        bottomContainer11 = (RelativeLayout) findViewById(R.id.ll_bottom);

        bottomContainerView = (HorizontalScrollView)findViewById(R.id.surface_baseline);
        bottomContainer = (LinearLayout) findViewById(R.id.ll_surface_baseline);

        memberContainer = findViewById(R.id.member_container);
        largeSurfacePreview = findViewById(R.id.large_preview);

        //申请权限
        requestPermissions();

        init();

        DemoHelper.getInstance().removeGlobalListeners();

        //增加监听
        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);

        //开启统计功能
       //EMClient.getInstance().conferenceManager().enableStatistics(true);

        ConferenceInfo.Initflag = true;
        Initflag = false;
    }

    /*
     初始化
     */
    private void init() {
        activity = this;
        meeting_duration = (TextView) findViewById(R.id.Meeting_duration);
        netInfoView = (ImageView)findViewById(R.id.netInfo);
        avatarView = (ImageView) findViewById(R.id.img_call_avatar);

        btn_mic = (Button) findViewById(R.id.btn_call_mic);
        btn_video = (Button) findViewById(R.id.btn_call_video);
        btn_hangup = (Button) findViewById(R.id.btn_call_hangup);
        btn_switch_camera = (Button) findViewById(R.id.btn_switch_camera);
        btn_talker_list = (Button) findViewById(R.id.btn_talker_list);
        btn_even_wheat = (Button) findViewById(R.id.btn_even_wheat);

        video_show_view = (ImageView)findViewById(R.id.icon_video_show);
        speak_show_view = (ImageView)findViewById(R.id.icon_speak_show);

        //btn_speaker_setting = (Button)findViewById(R.id.btn_speak_setting);
        timeHandler = new TimeHandler();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        btn_mic.setOnClickListener(listener);
        btn_video.setOnClickListener(listener);
        btn_hangup.setOnClickListener(listener);
        btn_switch_camera.setOnClickListener(listener);
        btn_talker_list.setOnClickListener(listener);
        btn_even_wheat.setOnClickListener(listener);
        rootContainer.setOnClickListener(listener);
        //btn_speaker_setting.setOnClickListener(listener);

        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);

        conferenceListener = this;

        conferenceSession = DemoHelper.getInstance().getConferenceSession();

        //设置视频分辨率
        String CameraResolution  =  PreferenceManager.getInstance().getCallFrontCameraResolution();
        if(CameraResolution.equals("360P")){
            EMClient.getInstance().callManager().getCallOptions().setVideoResolution(480 ,360);
        }else if(CameraResolution.equals("(Auto)480P")){
            EMClient.getInstance().callManager().getCallOptions().setVideoResolution(720,480);
        }else if(CameraResolution.equals("720P")){
            EMClient.getInstance().callManager().getCallOptions().setVideoResolution(1280,720);
        }

        //根据设置配置是否开关
        btn_mic.setEnabled(true);
        btn_video.setEnabled(true);
        avatarView.setVisibility(View.GONE);
        //oppositeSurface.setVisibility(View.VISIBLE);

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

        btn_mic.setActivated(normalParam.isAudioOff());
        btn_video.setActivated(normalParam.isVideoOff());

        initLocalConferenceView();

        openSpeaker();
        startAudioTalkingMonitor();
        // 加入会议的成员身份为主播
        //EMLog.i(TAG, "Get ConferenceId:"+ ConferenceInfo.getInstance().getConference().getConferenceId() + "conferenceRole :"+  ConferenceInfo.getInstance().getConference().getConferenceRole() );
        if (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Talker || ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin) {
            avatarView.setVisibility(View.GONE);
            EMCallSurfaceView localView = new EMCallSurfaceView(DemoHelper.getInstance().getContext());
            //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(largeSurfacePreview.getWidth(), dip2px(getApplicationContext(),0));
            //params.addRule(RelativeLayout.CENTER_IN_PARENT);
            //localView.setLayoutParams(params);
            ConferenceMemberInfo userProfile = new ConferenceMemberInfo();
            userProfile.setUserId(EMClient.getInstance().getCurrentUser());
            userProfile.setAudioOff(localStream.isAudioOff());
            userProfile.setAudioOff(localStream.isVideoOff());
            userProfile.setVideoView(localView);
            List<ConferenceMemberInfo> conferenceUserProfiles = new ArrayList<>();
            conferenceUserProfiles.add(userProfile);
            conferenceSession.setConferenceProfiles(conferenceUserProfiles);
            EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView);
            largeSurfacePreview.addView(localView);

            publish();
            if(!PreferenceManager.getInstance().isCallVideo()){
                avatarView.setVisibility(View.VISIBLE);
            }
            // 设置连麦按钮为‘申请下麦’
            setRequestBtnState(STATE_TALKER);
        }else if(ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience){
            //设置连麦按钮为上麦按钮
            setBtn_micAndBtn_vedio(EMConferenceManager.EMConferenceRole.Audience);

            setRequestBtnState(STATE_AUDIENCE);
            avatarView.setVisibility(View.VISIBLE);
            //oppositeSurface.setVisibility(View.GONE);
            if(streamList.size() > 0){
                ConferenceInfo.currentStream = streamList.get(0);
            }
        }

        timeHandler.startTime();
    }

    private int selfRadioButtonId;

    /**
     * 初始化多人音视频画面管理控件
     */
    private void initLocalConferenceView(){
        selfRadioButtonId = View.generateViewId();
        localViewContainer = new MultiMemberView(this);
        localViewContainer.setId(selfRadioButtonId);
        localViewContainer.setChecked(true);
        localViewContainer.setVideoOff(normalParam.isVideoOff());
        localViewContainer.setAudioOff(normalParam.isAudioOff());
        localViewContainer.setUsername(EMClient.getInstance().getCurrentUser());
        localViewContainer.setOnCheckedChangeListener(mOnCheckedChangeListener);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        memberContainer.addView(localViewContainer, params);

        if (localViewContainer.isVideoOff()){
            setBigImageView(localViewContainer);
            avatarView.setVisibility(View.VISIBLE);
            largeSurfacePreview.setVisibility(View.GONE);
        } else {
            avatarView.setVisibility(View.GONE);
            largeSurfacePreview.setVisibility(View.VISIBLE);
        }
        lastSelectedId =  selfRadioButtonId;
        imMembers.add(EMClient.getInstance().getCurrentUser());
    }

    private int lastSelectedId;
    private com.jaouan.compoundlayout.CompoundLayout.OnCheckedChangeListener mOnCheckedChangeListener = new com.jaouan.compoundlayout.CompoundLayout.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundLayout compoundLayout, boolean isChecked) {
            MultiMemberView view = (MultiMemberView) compoundLayout;
            boolean isSelf = view.getId() == selfRadioButtonId;
            boolean lastIsSelf = lastSelectedId == selfRadioButtonId;

            if (isChecked) {
                MultiMemberView lastCheckedMemberView = findViewById(lastSelectedId);

                if (lastIsSelf){
                    EMClient.getInstance().conferenceManager().updateLocalSurfaceView(null);
                } else {
                    EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(lastCheckedMemberView.getStreamId(), null);
                }

                if (isSelf){
                    EMClient.getInstance().conferenceManager().updateLocalSurfaceView(null);
                }else{
                    EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(view.getStreamId(), null);
                }

                EMCallSurfaceView lastSurfaceView = (EMCallSurfaceView) largeSurfacePreview.getChildAt(0);
                EMCallSurfaceView videoView = (EMCallSurfaceView) view.getSurfaceViewContainer().getChildAt(0);
                lastSurfaceView.getRenderer().dispose();
                videoView.getRenderer().dispose();
                view.getSurfaceViewContainer().removeAllViews();

                largeSurfacePreview.removeAllViews();
                lastCheckedMemberView.getSurfaceViewContainer().removeAllViews();
                lastSurfaceView = new EMCallSurfaceView(getApplicationContext());
                videoView = new EMCallSurfaceView(getApplicationContext());
                lastSurfaceView.setZOrderMediaOverlay(true);
                lastSurfaceView.setZOrderOnTop(true);
                lastSurfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                lastCheckedMemberView.getSurfaceViewContainer().addView(lastSurfaceView);


                videoView.setZOrderOnTop(false);
                videoView.setZOrderMediaOverlay(false);
                videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                largeSurfacePreview.addView(videoView, layoutParams);


                if (view.isVideoOff()){
                    avatarView.setVisibility(View.VISIBLE);
                    setBigImageView(view);
                    largeSurfacePreview.setVisibility(View.GONE);
                } else {
                    avatarView.setVisibility(View.GONE);
                    largeSurfacePreview.setVisibility(View.VISIBLE);
                    if (isSelf){
                        EMClient.getInstance().conferenceManager().updateLocalSurfaceView(videoView);
                    } else {
                        EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(view.getStreamId(), videoView);
                    }
                }
                view.getSurfaceViewContainer().setVisibility(View.GONE);

                if (lastCheckedMemberView.isVideoOff()){
                    lastCheckedMemberView.getSurfaceViewContainer().setVisibility(View.GONE);
//					lastCheckedMemberView.getAvatarImageView().setVisibility(View.VISIBLE);
                } else {
                    lastCheckedMemberView.getSurfaceViewContainer().setVisibility(View.VISIBLE);
//					lastCheckedMemberView.getAvatarImageView().setVisibility(View.GONE);
                    if (lastIsSelf){
                        EMClient.getInstance().conferenceManager().updateLocalSurfaceView(lastSurfaceView);
                    } else {
                        EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(lastCheckedMemberView.getStreamId(), lastSurfaceView);
                    }
                }
                lastSelectedId = view.getId();
            }

        }
    };

    private void setBigImageView(MultiMemberView memberView){
        if(memberView == null){
            return;
        }
        String username = memberView.getUsername();
        if (TextUtils.isEmpty(username)){
            return;
        }
       //AvatarUtils.setAvatarContent(this, username, avatarView);
    }

    /**
     * 增加View
     * @param info
     */
    private void addConferenceView(ConferenceMemberInfo info){
        MultiMemberView memberView = new MultiMemberView(ConferenceActivity.this);
        memberView.setId(getViewIdByStreamId(info.getUserId()));
        memberView.setUsername(info.getUserId());
        memberView.setStreamId(info.getStreamId());
        memberView.setAudioOff(info.isAudioOff());
        memberView.setVideoOff(info.isVideoOff());
        memberView.setOnCheckedChangeListener(mOnCheckedChangeListener);
        memberView.setChecked(false);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        memberContainer.addView(memberView, params);
        EMCallSurfaceView videoView = info.getVideoView();
        videoView.setZOrderMediaOverlay(true);
        //videoView.setZOrderOnTop(true);
        memberView.getSurfaceViewContainer().addView(videoView);
        EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(info.getStreamId(), videoView);
        imMembers.add(info.getUserId());
    }

    /**
     * 删除View
     * @param userId
     */
    private void removeConferenceView(String userId){
        imMembers.remove(userId);
        int viewId = getViewIdByStreamId(userId);
        if (viewId == memberContainer.getCheckedRadioLayoutId()){
            memberContainer.check(selfRadioButtonId);
        }
        memberContainer.removeView(findViewById(viewId));
    }

    /**
     * 更新指定View
     * @param info
     */
    private void updateConferenceMemberView(ConferenceMemberInfo info){
        int viewId = getViewIdByStreamId(info.getUserId());
        MultiMemberView memberView = findViewById(viewId);
        memberView.setAudioOff(info.isAudioOff());
        memberView.setVideoOff(info.isVideoOff());
        EMCallSurfaceView videoView = info.getVideoView();
        if (memberView.isChecked()){
                if (info.isVideoOff()){
                    avatarView.setVisibility(View.VISIBLE);
                    setBigImageView(memberView);
                    largeSurfacePreview.setVisibility(View.GONE);
                } else {
                    avatarView.setVisibility(View.GONE);
                    largeSurfacePreview.setVisibility(View.VISIBLE);
                    videoView.setZOrderMediaOverlay(false);
                    videoView.setZOrderOnTop(false);
                }
            }else{
                if (info.isVideoOff()){
                    videoView.setZOrderMediaOverlay(false);
                    videoView.setZOrderOnTop(false);
                    memberView.getSurfaceViewContainer().setVisibility(View.GONE);
                }else {
                    videoView.setZOrderMediaOverlay(true);
                    videoView.setZOrderOnTop(true);
                    memberView.getSurfaceViewContainer().setVisibility(View.VISIBLE);
                }

            }
            boolean isSelf = info.getUserId().equals(EMClient.getInstance().getCurrentUser());
            if (info.isVideoOff()){
                if (isSelf){
                    EMClient.getInstance().conferenceManager().updateLocalSurfaceView(null);
                }else{
                    EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(info.getStreamId(), null);
                }
            } else {
                if (isSelf){
                    EMClient.getInstance().conferenceManager().updateLocalSurfaceView(info.getVideoView());
                }else{
                    EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(info.getStreamId(), info.getVideoView());
                }
        }
    }

    private int getViewIdByStreamId(String userId){
        if (mMemberViewIds.containsKey(userId)){
            return mMemberViewIds.get(userId).intValue();
        }
        int viewId = View.generateViewId();
        mMemberViewIds.put(userId, viewId);
        return viewId;
    }


    /**
     * 根据角色判断摄像头麦克风是否禁用
     * @param role
     */
    private void setBtn_micAndBtn_vedio(EMConferenceManager.EMConferenceRole role){
        if(role == EMConferenceManager.EMConferenceRole.Audience){
            //设置麦克风和摄像头关闭  按钮不可操作

            EMClient.getInstance().conferenceManager().closeVideoTransfer();
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();

            normalParam.setAudioOff(true);
            localStream.setAudioOff(true);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);

            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);

            btn_mic.setActivated(false);
            btn_video.setActivated(false);

            avatarView.setVisibility(View.VISIBLE);

            btn_mic.setEnabled(false);
            btn_video.setEnabled(false);
        }else{
            btn_mic.setEnabled(true);
            btn_video.setEnabled(true);

            normalParam.setAudioOff(false);
            localStream.setAudioOff(false);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);

            normalParam.setVideoOff(false);
            localStream.setVideoOff(false);
            btn_video.setBackgroundResource(R.drawable.em_call_video_on);

            EMClient.getInstance().conferenceManager().openVideoTransfer();
            EMClient.getInstance().conferenceManager().openVoiceTransfer();
            avatarView.setVisibility(View.GONE);

            btn_mic.setActivated(true);
            btn_video.setActivated(true);
        }
    }


    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                /*case R.id.btn_speak_setting:
                      speakSwitch();
                      break;*/
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

                        if(conferenceSession.getConferenceProfiles().size() > 0){
                            RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(),100));
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            bottomContainer11.setLayoutParams(params);
                            bottomContainer11.setVisibility(View.VISIBLE);
                            bottomContainer.setVisibility(View.GONE);
                            bottomContainerView.setVisibility(View.VISIBLE);
                        }else{
                            RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(),0));
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            bottomContainer11.setLayoutParams(params);
                            bottomContainer11.setVisibility(View.GONE);
                            bottomContainer.setVisibility(View.GONE);
                            bottomContainerView.setVisibility(View.GONE);
                        }
                    } else {
                        if(conferenceSession.getConferenceProfiles().size() > 0) {
                            topContainer.setVisibility(View.VISIBLE);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 160));
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            bottomContainer11.setLayoutParams(params);
                            bottomContainer11.setVisibility(View.VISIBLE);
                            bottomContainer.setVisibility(View.VISIBLE);
                            bottomContainerView.setVisibility(View.VISIBLE);
                        }else{
                            topContainer.setVisibility(View.VISIBLE);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 60));
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            bottomContainer11.setLayoutParams(params);
                            bottomContainer11.setVisibility(View.VISIBLE);
                            bottomContainer.setVisibility(View.VISIBLE);
                            bottomContainerView.setVisibility(View.GONE);
                            //oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
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
                EMLog.i(TAG, "publish failed: error=" + error + ", msg=" + errorMsg);
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
                    EMLog.i(TAG, "unpublish scuessed ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setIcons(EMConferenceManager.EMConferenceRole.Audience);
                        }
                    });
                }
            @Override
            public void onError(int error, String errorMsg) {
                EMLog.i(TAG, "unpublish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }

    private void setIcons(EMConferenceManager.EMConferenceRole role) {
        if (role == EMConferenceManager.EMConferenceRole.Audience) {  //下麦
            EMLog.i(TAG, "start to be  Audience");
            EMLog.i(TAG, "start to be  Audience notifyItemChanged  1 end");
        }else{
            EMLog.i(TAG, "start to be  Audience changeflag" + ConferenceInfo.changeflag);
            EMLog.i(TAG, "start to be  talker end");
        }
    }

    /**
     * 扬声器开关
     */
     private void speakSwitch(){
        /* if (btn_speaker_setting.isActivated()) {
             closeSpeaker();
             btn_speaker_setting.setBackgroundResource(R.drawable.em_call_speaker_off);
         } else {
             openSpeaker();
             btn_speaker_setting.setBackgroundResource(R.drawable.em_call_speaker_on);
         }*/
     }

    /**
     * 语音开关
     */
    private void voiceSwitch() {
        EMLog.i(TAG, "voiceSwitch: " + normalParam.isAudioOff());
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
        if(ConferenceInfo.changeflag){
            if(ConferenceInfo.currentStream != null){
                avatarAdapter.notifyItemChanged(streamList.indexOf(ConferenceInfo.currentStream) , 0);
            }
        }
    }

    /**
     * 视频开关
     */
    private void videoSwitch() {
        EMLog.i(TAG,"videoSwitch  State:"+ normalParam.isVideoOff());
        if (normalParam.isVideoOff()) {
            normalParam.setVideoOff(false);
            localStream.setVideoOff(false);
            btn_video.setBackgroundResource(R.drawable.em_call_video_on);
            EMClient.getInstance().conferenceManager().openVideoTransfer();
        }else{
            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);
            EMClient.getInstance().conferenceManager().closeVideoTransfer();
        }
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        EMLog.i(TAG,"videoSwitch  changeCamera");
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
        if (btnState == STATE_AUDIENCE) { // 当前按钮状态是观众，需要变成主播
            if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience) { // 发送消息，申请上麦
                EMClient.getInstance().conferenceManager().setConferenceAttribute(EMClient.getInstance().getCurrentUser(),
                                              ConferenceAttributeOption.REQUEST_TOBE_SPEAKER, new  EMValueCallBack<Void>(){
                    @Override
                    public void onSuccess(Void value) {
                        EMLog.i(TAG, "request_tobe_speaker scuessed");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "您发出连麦申请 请等待管理员审核!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.i(TAG, "request_tobe_speaker failed: error=" + error + ", msg=" + errorMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "发送连麦请求失败 请稍后重试!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else { // 已经是主播，直接推流
                publish();
                setRequestBtnState(STATE_TALKER);
            }
        } else if (btnState == STATE_TALKER )  { // 当前按钮状态是主播，需要下麦

            if(streamList.size()  == 0){  //当前只有一个管理员不允许下麦
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "当前只有您一个管理员，不允许下麦!", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Talker || conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin){ // 申请下麦
                EMClient.getInstance().conferenceManager().setConferenceAttribute(EMClient.getInstance().getCurrentUser()
                           ,ConferenceAttributeOption.REQUEST_TOBE_AUDIENCE, new  EMValueCallBack<Void>(){
                    @Override
                    public void onSuccess(Void value) {
                        EMLog.i(TAG, "request_tobe_audience scuessed");
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.i(TAG, "request_tobe_audience failed: error=" + error + ", msg=" + errorMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "发送下麦请求失败 请稍后重试!", Toast.LENGTH_SHORT).show();
                            }
                        });
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
        EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
                @Override
                public void onSuccess(Object value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "您已成功退出当前会议！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    EMLog.i(TAG,"start  MainActivity");
                    Intent intent = new Intent(ConferenceActivity.this, MainActivity.class);
                    startActivity(intent);
                    EMLog.i(TAG,"finish ConferenceActivity");
                    finish();
                }
                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.i(TAG, "exit conference failed " + error + ", " + errorMsg);
                    Intent intent = new Intent(ConferenceActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
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
        //btn_speaker_setting.setActivated(true);
    }


    /**
     * 关闭扬声器，即开启听筒播放模式
     * 更多内容看{@link #openSpeaker()}
     */
    public void closeSpeaker() {
        // 检查是否已经开启扬声器
        /*if (audioManager.isSpeakerphoneOn()) {
            // 关闭扬声器
            audioManager.setSpeakerphoneOn(false);
        }
        // 设置声音模式为通讯模式
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        btn_speaker_setting.setActivated(false);*/
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
            }
        });
    }

    @Override
    public void onMemberExited(final EMConferenceMember member) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                EMLog.i(TAG, "onStreamAdded  start userID: " + stream.getUsername());
                if (conferenceSession == null){
                    EMLog.e(TAG, "onStreamAdd callSession is null");
                    return;
                }

                List<ConferenceMemberInfo> userProfiles = conferenceSession.getConferenceProfiles();
                if (userProfiles == null){
                    userProfiles = new ArrayList<>();
                }
                String appKey = EMClient.getInstance().getOptions().getAppKey();
                String memberName = stream.getMemberName();
                if (appKey != null && appKey.length() < memberName.length()){
                    String username = memberName.substring(appKey.length() + 1);
                    if (username.equals(EMClient.getInstance().getCurrentUser())){
                        if (userProfiles.isEmpty()){
                            throw new RuntimeException("userProfile isEmpty");

                        }
                    }else{
                        ConferenceMemberInfo userProfile = new ConferenceMemberInfo();
                        userProfile.setStreamId(stream.getStreamId());
                        userProfile.setUserId(username);
                        userProfile.setAudioOff(stream.isAudioOff());
                        userProfile.setVideoOff(stream.isVideoOff());

                        EMCallSurfaceView videoView = new EMCallSurfaceView(DemoHelper.getInstance().getContext());
                        videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                        userProfile.setVideoView(videoView);
                        userProfiles.add(userProfile);
                        subscribe(stream, videoView);
                        addConferenceView(userProfile);
                    }
                }
            }
        });
    }

    @Override
    public void onStreamRemoved(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMLog.i(TAG, "onStreamRemoved  start userID: " + stream.getUsername());

                List<ConferenceMemberInfo> userProfiles = conferenceSession.getConferenceProfiles();

                try {
                    if (userProfiles != null && !userProfiles.isEmpty()){
                        int index = -1;
                        for (int i = 0; i < userProfiles.size(); i++) {
                            ConferenceMemberInfo userProfile = userProfiles.get(i);
                            if (userProfile != null && userProfile.getStreamId() != null && userProfile.getStreamId().equals(stream.getStreamId())){
                                index = i;
                                break;
                            }
                        }
                        ConferenceMemberInfo removedUserProfile = userProfiles.remove(index);

                        removeConferenceView(removedUserProfile.getUserId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }

    @Override
    public void onStreamUpdate(final EMConferenceStream stream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),  "用户 " + EasyUtils.useridFromJid(stream.getUsername()) + " 更新了音视频流!", Toast.LENGTH_SHORT).show();

                String appKey = EMClient.getInstance().getOptions().getAppKey();
                String memberName = stream.getMemberName();
                if (appKey != null && appKey.length() < memberName.length()){
                    String username = memberName.substring(appKey.length() + 1);
                    ConferenceMemberInfo userProfile = conferenceSession.getConferenceMemberInfo(username);
                    if(userProfile != null){
                        userProfile.setVideoOff(stream.isVideoOff());
                        userProfile.setAudioOff(stream.isAudioOff());
                        updateConferenceMemberView(userProfile) ;
                    }
                }
            }
        });
    }

    @Override
    public void onPassiveLeave(final int error, final String message) { // 当前用户被踢出会议
            }

    @Override
    public void onConferenceState(final ConferenceState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void onStreamStatistics(EMStreamStatistics statistics) {
        EMLog.i(TAG, "onStreamStatistics getLocalAudioBps:" + statistics.getLocalAudioBps());
        EMLog.i(TAG, "onStreamStatistics getLocalAudioBps:" + statistics.getLocalCaptureWidth());
        EMLog.i(TAG, "onStreamStatistics getLocalAudioBps:" + statistics.getLocalCaptureWidth());
    }

    @Override
    public void onStreamSetup(final String streamId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        Log.d(TAG, "add conference view -start- " + stream.getMemberName());
        avatarAdapter.addData(stream);
        Log.d(TAG, "add conference view -end-" + stream.getMemberName());
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

        updateIndex = streamList.indexOf(stream);
        avatarAdapter.notifyItemChanged(updateIndex ,0);
    }


    /**
     * 收到其他人的会议邀请
     */
    @Override
    public void onReceiveInvite(final String confId, String password, String extension) { }


    @Override
    public void onRoleChanged(EMConferenceManager.EMConferenceRole role) {
        EMLog.i(TAG, "onRoleChanged, role: " + role);
        //currentRole = role;
        conference.setConferenceRole(role);
        if (role == EMConferenceManager.EMConferenceRole.Talker) {
            // 管理员把当前用户角色更改为主播或管理员。
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EMLog.i(TAG, "onRoleChanged, start publish, params: " + normalParam.toString());

                    publish();
                    setBtn_micAndBtn_vedio(role);
                    setRequestBtnState(STATE_TALKER);
                }
            });
        } else if (role == EMConferenceManager.EMConferenceRole.Audience) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 下麦
                    unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.NORMAL));
                    setBtn_micAndBtn_vedio(role);
                    setRequestBtnState(STATE_AUDIENCE);
                }
            });
        }else if(role == EMConferenceManager.EMConferenceRole.Admin){  //主播变更为管理员
            //currentRole = role;
            EMClient.getInstance().conferenceManager().setConferenceAttribute(EMClient.getInstance().getCurrentUser(),
                    ConferenceAttributeOption.REQUEST_BECOME_ADMIN, new  EMValueCallBack<Void>(){
                        @Override
                        public void onSuccess(Void value) {
                            EMLog.i(TAG, "request_tobe_speaker scuessed");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "您已变更为管理员!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        @Override
                        public void onError(int error, String errorMsg) {
                            EMLog.i(TAG, "request_tobe_speaker failed: error=" + error + ", msg=" + errorMsg);
                        }
                    });
        }
    }

    @Override
    public void onAttributesUpdated(EMConferenceAttribute[] attributes) {
        EMLog.i(TAG, " onAttributesUpdated started ");
        EMConferenceAttribute conferenceAttribute;
        int size = attributes.length;
        for (int i = 0; i < size; i++) {
            conferenceAttribute = attributes[i];
            String usreId = conferenceAttribute.key;
            String option = conferenceAttribute.value;
            EMLog.i(TAG, " onAttributesUpdated： usreId: " + usreId + ", option: " + option);

            EMClient.getInstance().conferenceManager().deleteConferenceAttribute(usreId, new EMValueCallBack<Void>() {
                @Override
                public void onSuccess(Void value) {
                    EMLog.i(TAG, "onAttributesUpdated   delete role success, result: " + value);
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.i(TAG, "onAttributesUpdated   delete role failed, error: " + error + " - " + errorMsg);
                }
            });

            if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin) {
                //申请上麦
                EMLog.i(TAG, " onAttributesUpdated： talker request_tobe_speaker");
                if (option.equals(ConferenceAttributeOption.REQUEST_TOBE_SPEAKER)) {
                    if (streamList.size() >= 8) { //大于9个主播
                        //展示主播是否踢人界面
                        EMLog.i(TAG, " onAttributesUpdated： talker is full");
                        takerFullDialogDisplay(usreId);
                        return;
                    } else {
                        //申请上麦是否同意提示框
                        EMLog.i(TAG, " onAttributesUpdated： talker request_tobe_speaker start");
                        requestTalkerDisplay(usreId);
                        return;
                    }
                } else if (option.equals(ConferenceAttributeOption.REQUEST_TOBE_AUDIENCE)) {  //申请下麦
                    EMLog.i(TAG, " onAttributesUpdated： talker request_tobe_audience");
                    String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                    EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                            , new EMConferenceMember(memName, null, null)
                            , EMConferenceManager.EMConferenceRole.Audience, new EMValueCallBack<String>() {
                                @Override
                                public void onSuccess(String value) {
                                    EMLog.i(TAG, "onAttributesUpdated  request_tobe_audience changeRole success, result: " + value);
                                }

                                @Override
                                public void onError(int error, String errorMsg) {
                                    EMLog.i(TAG, "onAttributesUpdated  request_tobe_audience failed, error: " + error + " - " + errorMsg);
                                }
                            });
                } else if (option.equals(ConferenceAttributeOption.REQUEST_BECOME_ADMIN)) { //变更管理员通知
                    EMClient.getInstance().conferenceManager().deleteConferenceAttribute(usreId, new EMValueCallBack<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            EMLog.i(TAG, "onAttributesUpdated  become_admin delete role success, result: " + value);
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                            EMLog.i(TAG, "onAttributesUpdated  become_admin delete role failed, error: " + error + " - " + errorMsg);
                        }
                    });
                }
            }else{
                if (option.equals(ConferenceAttributeOption.REQUEST_BECOME_ADMIN)) { //变更管理员通知
                    if(usreId != ConferenceInfo.getInstance().getLocalStream().getUsername()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EMLog.i(TAG, "onAttributesUpdated  become_admin  " + usreId +  "已变更为管理员");
                                Toast.makeText(getApplicationContext(), "用户 " + usreId + " 已变更为管理员!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * 主播已满 踢人下麦 提示对话框
     */
    private void takerFullDialogDisplay(String usreId) {
        EMLog.i(TAG, "takerFullDialogDisplay diplay userId " + choose_userId);
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
                EMLog.i(TAG, "takerFullDialogDisplay diplay ok id " +usreId);
                takerListChooseDispaly(usreId);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EMLog.i(TAG, "takerFullDialogDisplay diplay cancel id " +usreId);
                dialog.dismiss();
            }
        });
    }

    /**
     * 申请上麦提示框
     */
    private void requestTalkerDisplay(String usreId) {
        EMLog.i(TAG, " onAttributesUpdated： requestTalkerDisplay start");
        AlertDialog.Builder builder = new AlertDialog.Builder(ConferenceActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(ConferenceActivity.this, R.layout.activity_talker_full_kick, null);
        TextView infoView = dialogView.findViewById(R.id.info_view);
        Button cancelbtn = dialogView.findViewById(R.id.btn_kick_cancel);
        Button okbtn = dialogView.findViewById(R.id.btn_kick_ok);
        infoView.setText("用户 " +usreId+"申请上麦 是否同意？");
        cancelbtn.setText("不同意");
        okbtn.setText("同意");
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
                EMLog.i(TAG, " onAttributesUpdated： requestTalkerDisplay  request_tobe_speaker start"+ usreId);
                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                        , new EMConferenceMember(memName, null, null)
                        , EMConferenceManager.EMConferenceRole.Talker, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                                EMLog.i(TAG, " requestTalkerDisplay  request_tobe_speaker changeRole success, result: " + value);
                            }
                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.i(TAG, " requestTalkerDisplay  request_tobe_speaker changeRole failed, error: " + error + " - " + errorMsg);
                            }
                        });
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EMClient.getInstance().conferenceManager().deleteConferenceAttribute(usreId, new EMValueCallBack<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        EMLog.i(TAG, " requestTalkerDisplay cancel request_tobe_speaker delete role success, result: " + value);
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.i(TAG, " requestTalkerDisplay cancel request_tobe_speaker changeRole failed, error: " + error + " - " + errorMsg);
                    }
                });
                dialog.dismiss();
            }
        });
    }


    /**
     * 展示管理员踢主播列表
     *
     */
    private void takerListChooseDispaly(String usreId){
        EMLog.i(TAG, "takerFullDialogDisplay show  id " +usreId);
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
                EMLog.i(TAG, "takerFullDialogDisplay choose position: " + position);
                ChooseTalkerItemAdapter.chooseIndex = position;
                choose_userId = streamList.get(position).getUsername();
                EMLog.i(TAG, "takerFullDialogDisplay choose userId: " + choose_userId);
                adapter.notifyDataSetChanged();
                adapter.updataData();
            }
        });

        DividerItemDecoration decoration = new DividerItemDecoration(dialogView.getContext(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        recyclerView.addItemDecoration(decoration);

        btn_ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //选择主播下线
                EMLog.i(TAG, "takerListChooseDispaly ok choose to offline userId " + choose_userId);
                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), choose_userId);
                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                        , new EMConferenceMember(memName, null, null)
                        , EMConferenceManager.EMConferenceRole.Audience, new EMValueCallBack<String>(){
                            @Override
                            public void onSuccess(String value) {
                                EMLog.i(TAG, "takerListChooseDispaly ok choose to offline userId " + choose_userId +"  success, result: " + value);
                                //让申请的主播上线
                                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                                        , new EMConferenceMember(memName, null, null)
                                        , EMConferenceManager.EMConferenceRole.Talker, new EMValueCallBack<String>() {
                                            @Override
                                            public void onSuccess(String value) {
                                                EMLog.i(TAG, "takerListChooseDispaly ok choose to online userId " + usreId +"  success, result: " + value);
                                            }
                                            @Override
                                            public void onError(int error, String errorMsg) {
                                                EMLog.i(TAG, "takerListChooseDispaly ok choose to online userId " + usreId +"  error: " + error + " - " + errorMsg);
                                            }
                                        });
                            }
                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.i(TAG, "takerListChooseDispaly ok choose to offline userId " + choose_userId +" failed, error: " + error + " - " + errorMsg);
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


    /**
     * 当前设备通话状态监听器
     */
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
                        try{
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
        Intent intent = new Intent(ConferenceActivity.this, RoomSettingActivity.class);
        startActivity(intent);
    }


    /**
     * 获取会议信息
     */
    private void getConferenceInfo(){
        Intent intent = new Intent(ConferenceActivity.this, TalkerListActivity.class);
        startActivity(intent);
    }


    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    /**
     * 检测网路信号
     */
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
                netInfoView.setBackgroundResource(R.drawable.networkinfo4);
            } else if (wifi > -80 && wifi < -70) {//较弱
                //Log.e(TAG, "较弱");
                netInfoView.setBackgroundResource(R.drawable.networkinfo3);
            } else if (wifi > -100 && wifi < -80) {//微弱
                //Log.e(TAG, "微弱");
                netInfoView.setBackgroundResource(R.drawable.networkinfo2);
            }
        } else {
            //无连接
            //Log.e(TAG, "无wifi连接");
            netInfoView.setBackgroundResource(R.drawable.networkinfo0);
        }
    }

    /**
     * 定时更新通话时间
     * @param time
     */
    private void updateConferenceTime(String time) {
        meeting_duration.setText(time);
        checkWifiState();
    }

    /**
     * 切换小窗口后本地  摄像头 麦克风状态
     */
    private void setLocalState(int index){
        if(streamList.get(index).isAudioOff()){
            speak_show_view.setBackgroundResource(R.drawable.call_mic_off);
        }else{
            speak_show_view.setBackgroundResource(R.drawable.call_mic_on);
        }
        if (streamList.get(index).isVideoOff()) {
            avatarView.setVisibility(View.VISIBLE);
            video_show_view.setBackgroundResource(R.drawable.call_video_off);
        } else {
            avatarView.setVisibility(View.GONE);
            video_show_view.setBackgroundResource(R.drawable.call_video_on);
        }
    }

    /**
     * 交换Surfaceview方法封装
     */
    private void changeSurface(int index, EMCallSurfaceView localSurface, EMCallSurfaceView oppositeSurface) {
        if (conference.getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience) {
            if (!ConferenceInfo.changeflag) {
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(localSurface);
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(streamList.get(index).getStreamId(),oppositeSurface);
                avatarAdapter.notifyItemChanged(index, 0);
            } else {
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(localSurface);
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(streamList.get(index).getStreamId(),oppositeSurface);
                localSurface.release();
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(localSurface);
                avatarAdapter.notifyItemChanged(index, 0);
            }
        } else {
            if (!ConferenceInfo.changeflag) {
                avatarView.setVisibility(View.GONE);
                speak_show_view.setVisibility(View.VISIBLE);
                video_show_view.setVisibility(View.VISIBLE);
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(localSurface);
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(streamList.get(index).getStreamId(),oppositeSurface);
                avatarAdapter.notifyItemChanged(index, 0);
            } else{
                avatarView.setVisibility(View.VISIBLE);
                speak_show_view.setVisibility(View.GONE);
                video_show_view.setVisibility(View.GONE);
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(localSurface);
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(streamList.get(index).getStreamId(),oppositeSurface);
                localSurface.release();
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(localSurface);
                avatarAdapter.notifyItemChanged(index, 0);
            }
        }
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
        EMLog.i(TAG,"onDestroy start  ConferenceActivity  Main threadID: " + Thread.currentThread().getName());
        EMClient.getInstance().conferenceManager().removeConferenceListener(conferenceListener);

        DemoHelper.getInstance().logout(true,new EMCallBack(){
            @Override
            public void onSuccess() {
                EMLog.i(TAG, "im logout scuessfull");
            }

            @Override
            public void onProgress(int progress, String status){

            }
            @Override
            public void onError(int code, String message) {
                EMLog.i(TAG, "im logout failed" + code + ", msg " + message);

            }
        });
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setMicrophoneMute(false);
        EMLog.i(TAG,"onDestroy end   ConferenceActivity  Main threadID: " + Thread.currentThread().getName());
        imMembers.clear();
        super.onDestroy();
        EMLog.i(TAG,"onDestroy over   ConferenceActivity  Main threadID: " + Thread.currentThread().getName());
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            exitConference();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            x1 = event.getX();
            y1 = event.getY();
        }
        if(event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            x2 = event.getX();
            y2 = event.getY();
            if(y1 - y2 > 50) {
                //Toast.makeText(MainActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
            } else if(y2 - y1 > 50) {
                //Toast.makeText(MainActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
            } else if(x1 - x2 > 50) {
                // Toast.makeText(MainActivity.this, "向左滑", Toast.LENGTH_SHORT).show();
            } else if(x2 - x1 > 50) {
                streamList.remove(ConferenceInfo.getInstance().getLocalStream());
                exitConference();
            }
        }
        return super.onTouchEvent(event);
    }
}
