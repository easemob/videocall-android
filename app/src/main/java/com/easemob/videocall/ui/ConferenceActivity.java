package com.easemob.videocall.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.utils.ConferenceMemberInfo;
import com.easemob.videocall.utils.ConferenceSession;
import com.easemob.videocall.utils.ConfigManager;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceAttribute;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMLiveRegion;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.chat.EMStreamStatistics;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;

import com.easemob.videocall.DemoHelper;
import com.easemob.videocall.R;
import com.easemob.videocall.adapter.ChooseTalkerItemAdapter;
import com.easemob.videocall.adapter.OnItemClickListener;
import com.easemob.videocall.utils.ConferenceAttributeOption;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.PhoneStateManager;
import com.easemob.videocall.utils.PreferenceManager;
import com.jaouan.compoundlayout.CompoundLayout;
import com.jaouan.compoundlayout.RadioLayoutGroup;

import com.superrtc.mediamanager.ScreenCaptureManager;
import com.superrtc.sdk.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import static com.easemob.videocall.utils.ConferenceAttributeOption.REQUEST_TOBE_MUTE_ALL;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

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
    private Button btn_talker_list;
    private Button btn_even_wheat;

    private RelativeLayout btn_mic_layout;
    private RelativeLayout btn_video_layout;
    private RelativeLayout btn_hangup_layout;
    private RelativeLayout btn_talker_list_layout;
    private RelativeLayout btn_even_wheat_layout;

    private Button btn_switch_camera;
    private Button btn_speaker_setting;
    private Button btn_expansion;
    // 屏幕分享开关
    private Button btn_screenShare;

    private TextView video_view;
    private TextView mic_view;
    private TextView even_wheat_view;

    private ImageView avatarView;
    private ImageView netInfoView;

    private EMStreamParam desktopParam;

    private ImageView video_show_view;
    private ImageView speak_show_view;
    private ImageView admin_show_view;

    private RelativeLayout rootContainer;
    private LinearLayout bottomContainer;
    private RelativeLayout topContainer;
    private RelativeLayout bottomContainer11;
    private HorizontalScrollView bottomContainerView;
    private EMConferenceListener conferenceListener;

    private TimeHandler timeHandler;
    private AudioManager audioManager;
    private EMConference conference;
    private EMStreamParam normalParam;

    private List<EMConferenceStream> talkerList;
    private List<EMConferenceStream> streamList;
    private List<EMConferenceMember> memberList;
    private List<String> adminList;
    private EMConferenceStream localStream = null;

    private String choose_userId;

    // 标识当前上麦按钮状态
    private int btnState = STATE_AUDIENCE;

    private MultiMemberView localViewContainer;
    private RadioLayoutGroup memberContainer;
    private RelativeLayout largeSurfacePreview;
    private Map<String, Integer> mMemberViewIds = new HashMap<>();
    private Set<String> imMembers = new HashSet<>();
    private ConferenceSession conferenceSession;

    private ConferenceMemberInfo localuserProfile = null;
    private int lastSelectedId;
    private boolean expansionflag = true;

    private String headImageurl = null;
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    public static final String KEY_ID = "ID";
    private int mId;
    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EMLog.i(TAG, "oncreate  ConferenceActivity  Main threadID: " + Thread.currentThread().getName());
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

        //getWindow().addFlags(View.SYSTEM_UI_FLAG_FULLSCREEN);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        streamList = ConferenceInfo.getInstance().getConferenceStreamList();
        talkerList = ConferenceInfo.getInstance().getTalkerList();
        memberList = ConferenceInfo.getInstance().getConferenceMemberList();
        adminList = ConferenceInfo.getInstance().getAdmins();
        localStream = ConferenceInfo.getInstance().getLocalStream();

        meeting_roomID = (TextView) findViewById(R.id.Meeting_roomID);
        meeting_roomID.setText(ConferenceInfo.getInstance().getRoomname());
        conference = ConferenceInfo.getInstance().getConference();

        rootContainer = (RelativeLayout) findViewById(R.id.root_layout);
        topContainer = (RelativeLayout) findViewById(R.id.ll_top_container);
        bottomContainer11 = (RelativeLayout) findViewById(R.id.ll_bottom);

        bottomContainerView = (HorizontalScrollView) findViewById(R.id.surface_baseline);
        bottomContainer = (LinearLayout) findViewById(R.id.ll_surface_baseline);

        memberContainer = findViewById(R.id.member_container);
        largeSurfacePreview = findViewById(R.id.large_preview);

        init();
        DemoHelper.getInstance().removeGlobalListeners();

        //增加监听
        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);

        registerBluetoothBroadCast();
    }

    /*
     初始化
     */
    private void init() {
        activity = this;
        meeting_duration = (TextView) findViewById(R.id.Meeting_duration);
        netInfoView = (ImageView) findViewById(R.id.netInfo);
        avatarView = (ImageView) findViewById(R.id.img_call_avatar);
        admin_show_view = (ImageView)findViewById(R.id.admin_show_view);

        btn_mic = (Button) findViewById(R.id.btn_call_mic);
        btn_video = (Button) findViewById(R.id.btn_call_video);
        btn_hangup = (Button) findViewById(R.id.btn_call_hangup);
        btn_talker_list = (Button) findViewById(R.id.btn_talker_list);
        btn_even_wheat = (Button) findViewById(R.id.btn_even_wheat);
        btn_screenShare = (Button)findViewById(R.id.btn_screenShare);
        btn_mic.setClickable(false);
        btn_video.setClickable(false);
        btn_hangup.setClickable(false);
        btn_talker_list.setClickable(false);
        btn_even_wheat.setClickable(false);

        btn_mic_layout = (RelativeLayout)findViewById(R.id.btn_call_mic_layout);
        btn_video_layout = (RelativeLayout)findViewById(R.id.btn_call_video_layout);
        btn_hangup_layout = (RelativeLayout)findViewById(R.id.btn_call_hangup_layout);
        btn_talker_list_layout = (RelativeLayout)findViewById(R.id.btn_talker_list_layout);
        btn_even_wheat_layout = (RelativeLayout)findViewById(R.id.btn_even_wheat_layout);

        btn_switch_camera = (Button)findViewById(R.id.btn_switch_camera);

        btn_expansion = (Button)findViewById(R.id.btn_expansion);
        mic_view= (TextView) findViewById(R.id.text_call_mic);
        video_view =(TextView)findViewById(R.id.text_call_video);
        even_wheat_view =(TextView)findViewById(R.id.text_even_wheat);
        speak_show_view = (ImageView) findViewById(R.id.icon_speak_show);

        btn_speaker_setting = (Button)findViewById(R.id.btn_speak_setting);
        timeHandler = new TimeHandler();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        btn_switch_camera.setOnClickListener(listener);
        rootContainer.setOnClickListener(listener);
        btn_speaker_setting.setOnClickListener(listener);
        btn_expansion.setOnClickListener(listener);
        btn_screenShare.setOnClickListener(listener);

        btn_mic_layout.setOnClickListener(listener);
        btn_video_layout .setOnClickListener(listener);
        btn_hangup_layout.setOnClickListener(listener);
        btn_talker_list_layout.setOnClickListener(listener);
        btn_even_wheat_layout.setOnClickListener(listener);

        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);

        desktopParam = new EMStreamParam();
        desktopParam.setAudioOff(true);
        desktopParam.setVideoOff(true);
        desktopParam.setStreamType(EMConferenceStream.StreamType.DESKTOP);

        conferenceListener = this;
        conferenceSession = DemoHelper.getInstance().getConferenceSession();

        //根据设置配置是否开关
        btn_mic_layout.setEnabled(true);
        btn_video_layout.setEnabled(true);
        avatarView.setVisibility(View.GONE);

        if (PreferenceManager.getInstance().isCallAudio()) {
            normalParam.setAudioOff(false);
            localStream.setAudioOff(false);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
            speak_show_view.setVisibility(View.GONE);
        } else {
            normalParam.setAudioOff(true);
            localStream.setAudioOff(true);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);

            speak_show_view.setVisibility(View.VISIBLE);
            speak_show_view.setBackgroundResource(R.drawable.call_mute_big);
        }
        if (PreferenceManager.getInstance().isCallVideo()) {
            normalParam.setVideoOff(false);
            localStream.setVideoOff(false);
            btn_video.setBackgroundResource(R.drawable.em_call_video_on);
        } else {
            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);
        }

        btn_mic_layout.setActivated(normalParam.isAudioOff());
        btn_video_layout.setActivated(normalParam.isVideoOff());

        openSpeaker();
        startAudioTalkingMonitor();
        // 加入会议的成员身份为主播
        EMLog.i(TAG, "Get ConferenceId:" + ConferenceInfo.getInstance().getConference().getConferenceId() + "conferenceRole :" + ConferenceInfo.getInstance().getConference().getConferenceRole());
        if (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Talker || ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin) {
            setRequestBtnState(STATE_TALKER);
        } else if (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience) {

            setBtn_micAndBtn_vedio(EMConferenceManager.EMConferenceRole.Audience);
            setRequestBtnState(STATE_AUDIENCE);
        }
        timeHandler.startTime();
    }

    private int selfRadioButtonId;
    private com.jaouan.compoundlayout.CompoundLayout.OnCheckedChangeListener mOnCheckedChangeListener = new com.jaouan.compoundlayout.CompoundLayout.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundLayout compoundLayout, boolean isChecked) {
            MultiMemberView view = (MultiMemberView) compoundLayout;
            boolean isSelf = view.getId() == selfRadioButtonId;
            boolean lastIsSelf = lastSelectedId == selfRadioButtonId;

            if (isChecked) {
                MultiMemberView lastCheckedMemberView = findViewById(lastSelectedId);

                if (lastIsSelf) {
                    EMClient.getInstance().conferenceManager().updateLocalSurfaceView(null);
                } else {
                    EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(lastCheckedMemberView.getStreamId(), null);
                }

                if (isSelf) {
                    EMClient.getInstance().conferenceManager().updateLocalSurfaceView(null);
                } else {
                    EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(view.getStreamId(), null);
                }

                EMCallSurfaceView lastSurfaceView = (EMCallSurfaceView) largeSurfacePreview.getChildAt(0);
                EMCallSurfaceView videoView = (EMCallSurfaceView) view.getSurfaceViewContainer().getChildAt(0);
                lastSurfaceView.getRenderer().dispose();
                videoView.getRenderer().dispose();
                view.getSurfaceViewContainer().removeAllViews();

                largeSurfacePreview.removeAllViews();
                lastCheckedMemberView.getSurfaceViewContainer().removeAllViews();
                lastSurfaceView = new EMCallSurfaceView(ConferenceActivity.this);
                videoView = new EMCallSurfaceView(ConferenceActivity.this);
                lastSurfaceView.setZOrderMediaOverlay(true);
                lastSurfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                lastCheckedMemberView.getSurfaceViewContainer().addView(lastSurfaceView);
                List<ConferenceMemberInfo> userProfiles = conferenceSession.getConferenceProfiles();
                if (userProfiles != null && !userProfiles.isEmpty()) {
                    int index = -1;
                    for (int i = 0; i < userProfiles.size(); i++) {
                        ConferenceMemberInfo userProfile = userProfiles.get(i);
                        if (userProfile != null && userProfile.getStreamId() != null && userProfile.getStreamId().equals(lastCheckedMemberView.getStreamId())) {
                            userProfile.setVideoView(lastSurfaceView);
                            break;
                        }
                    }
                }
                videoView.setZOrderMediaOverlay(false);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                largeSurfacePreview.addView(videoView, lp);
                if (userProfiles != null && !userProfiles.isEmpty()) {
                    int index = -1;
                    for (int i = 0; i < userProfiles.size(); i++) {
                        ConferenceMemberInfo userProfile = userProfiles.get(i);
                        if(adminList.contains(userProfile.getUserId())){
                            admin_show_view.setVisibility(View.VISIBLE);
                        }else {
                            admin_show_view.setVisibility(View.GONE);
                        }
                        if (userProfile != null && userProfile.getStreamId() != null && userProfile.getStreamId().equals(view.getStreamId())) {
                            userProfile.setVideoView(videoView);
                            if(userProfile.getUserId().equals(EMClient.getInstance().getCurrentUser())){
                                videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                            }else{
                                videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
                            }
                            //切换时候更新小图标状态
                            setLocalAudioVideoIcons(userProfile);
                            break;
                        }
                    }
                }
                if (view.isVideoOff()) {
                    avatarView.setVisibility(View.VISIBLE);
                    headImageurl = DemoApplication.baseurl;
                    headImageurl = headImageurl + view.getHeadImage();
                    loadImage();
                    setBigImageView(view);
                    largeSurfacePreview.setVisibility(View.GONE);
                } else {
                    avatarView.setVisibility(View.GONE);
                    largeSurfacePreview.setVisibility(View.VISIBLE);
                    if (isSelf) {
                        EMClient.getInstance().conferenceManager().updateLocalSurfaceView(videoView);
                    } else {
                        EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(view.getStreamId(), videoView);
                    }
                }
                view.getSurfaceViewContainer().setVisibility(View.GONE);

                if (lastCheckedMemberView.isVideoOff()) {
                    lastCheckedMemberView.getSurfaceViewContainer().setVisibility(View.GONE);
                } else {
                    lastCheckedMemberView.getSurfaceViewContainer().setVisibility(View.VISIBLE);
                    if (lastIsSelf) {
                        EMClient.getInstance().conferenceManager().updateLocalSurfaceView(lastSurfaceView);
                    } else {
                        EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(lastCheckedMemberView.getStreamId(), lastSurfaceView);
                    }
                }
                lastSelectedId = view.getId();
            }
        }
    };

    private void setBigImageView(MultiMemberView memberView) {
        if (memberView == null) {
            return;
        }
        String username = memberView.getUsername();
        if (TextUtils.isEmpty(username)) {
            return;
        }
    }

    /**
     * 增加View
     *
     * @param info
     */
    private void addConferenceView(ConferenceMemberInfo info) {
        MultiMemberView memberView = new MultiMemberView(ConferenceActivity.this);
        memberView.setId(getViewIdByStreamId(info.getStreamId()));
        EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(info.getUserId());
        memberView.setUsername(info.getUserId());
        if(info.isVideoOff()){
            memberView.setNickname(memberInfo.nickName);
        }else {
            memberView.setNickname(null);
        }
        memberView.setHeadImage(memberInfo.extension);
        memberView.setStreamId(info.getStreamId());
        memberView.setAudioOff(info.isAudioOff());
        memberView.setVideoOff(info.isVideoOff());
        memberView.setDesktop(info.isDesktop());

        //角色为观众第一个主播进来的时候 显示在大屏
        if (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience
                && conferenceSession.getConferenceProfiles().size() == 1) {
            memberView.setChecked(true);
            memberView.setOnCheckedChangeListener(mOnCheckedChangeListener);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            memberContainer.addView(memberView, params);
            EMCallSurfaceView videoView = info.getVideoView();
            imMembers.add(info.getStreamId());
            if(info.isAudioOff()){
                speak_show_view.setVisibility(View.VISIBLE);
                speak_show_view.setBackgroundResource(R.drawable.call_mute_big);
            } else {
                speak_show_view.setVisibility(View.GONE);
            }

            if (memberView.isVideoOff()) {
                setBigImageView(memberView);
                avatarView.setVisibility(View.VISIBLE);
                headImageurl = DemoApplication.baseurl;
                headImageurl = headImageurl + memberView.getHeadImage();
                loadImage();
                largeSurfacePreview.setVisibility(View.GONE);
            } else {
                avatarView.setVisibility(View.GONE);
                largeSurfacePreview.setVisibility(View.VISIBLE);
            }
            lastSelectedId = getViewIdByStreamId(info.getStreamId());
            imMembers.add(info.getStreamId());

            videoView.setZOrderOnTop(false);
            videoView.setZOrderMediaOverlay(false);
            videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
            EMClient.getInstance().conferenceManager().setLocalSurfaceView(videoView);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            largeSurfacePreview.addView(videoView, lp);

            memberView.getAvatarImageView().setVisibility(View.VISIBLE);

            //打开小窗口的 麦克风 摄像头小图标
            setLocalAudioVideoIcons(info);

            //显示下边的x小窗口列表
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 180));
            params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            bottomContainer11.setLayoutParams(params2);
            bottomContainer11.setVisibility(View.VISIBLE);
            bottomContainer.setVisibility(View.VISIBLE);
            bottomContainerView.setVisibility(View.VISIBLE);
        } else {
            memberView.setOnCheckedChangeListener(mOnCheckedChangeListener);
            memberView.setChecked(false);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            memberContainer.addView(memberView, params);
            EMCallSurfaceView videoView = info.getVideoView();
            videoView.setZOrderMediaOverlay(true);
            memberView.getSurfaceViewContainer().addView(videoView);
            EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(info.getStreamId(), videoView);
            imMembers.add(info.getStreamId());
        }

        //第二个主播进入
        if (ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience
                && conferenceSession.getConferenceProfiles().size() == 2) {
            //显示下边的x小窗口列表
            //topContainer.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 180));
            params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            bottomContainer11.setLayoutParams(params2);
            bottomContainer11.setVisibility(View.VISIBLE);
            bottomContainer.setVisibility(View.VISIBLE);
            bottomContainerView.setVisibility(View.VISIBLE);
            bottomContainer11.bringToFront();
        }
    }

    /**
     * 删除View
     *
     * @param userId
     */
    private void removeConferenceView(String userId) {
        imMembers.remove(userId);
        int viewId = getViewIdByStreamId(userId);
        if (viewId == memberContainer.getCheckedRadioLayoutId()) {
            if (ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience) {
                if (viewId != selfRadioButtonId) {
                    memberContainer.check(selfRadioButtonId);
                } else {
                    if (conferenceSession.getConferenceProfiles().size() > 0) {
                        int showviewId = getViewIdByStreamId(conferenceSession.getConferenceProfiles().get(0).getStreamId());
                        memberContainer.check(showviewId);
                    }
                }
            } else {
                if (conferenceSession.getConferenceProfiles().size() > 0) {
                    int showviewId = getViewIdByStreamId(conferenceSession.getConferenceProfiles().get(0).getStreamId());
                    memberContainer.check(showviewId);
                } else {
                    speak_show_view.setVisibility(View.GONE);
                }
            }
        }
        memberContainer.removeView(findViewById(viewId));

        if ((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() == 1) ||
                (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() == 0)) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 80));
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            bottomContainer11.setLayoutParams(params);
            bottomContainer11.setVisibility(View.VISIBLE);
            bottomContainer.setVisibility(View.VISIBLE);
            bottomContainerView.setVisibility(View.GONE);
        }
    }

    /**
     * 更新指定View
     *
     * @param info
     */
    private void updateConferenceMemberView(ConferenceMemberInfo info) {
        int viewId = getViewIdByStreamId(info.getStreamId());
        MultiMemberView memberView = findViewById(viewId);
        memberView.setAudioOff(info.isAudioOff());
        memberView.setVideoOff(info.isVideoOff());
        EMCallSurfaceView videoView = info.getVideoView();
        EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(info.getUserId());
        if (memberView.isChecked()) {
            if (info.isVideoOff()) {
                avatarView.setVisibility(View.VISIBLE);
                headImageurl = DemoApplication.baseurl;
                headImageurl = headImageurl + memberView.getHeadImage();
                loadImage();
                memberView.getAvatarImageView().setVisibility(View.VISIBLE);

                if(memberInfo != null){
                    memberView.setNickname(memberInfo.nickName);
                }else{
                    if(EMClient.getInstance().getCurrentUser().equals(info.getUserId())){
                        memberView.setNickname(PreferenceManager.getInstance().getCurrentUserNick());
                    }
                }
            } else {
                memberView.setNickname(null);
                avatarView.setVisibility(View.GONE);
                largeSurfacePreview.setVisibility(View.VISIBLE);
                videoView.setZOrderMediaOverlay(true);
                videoView.setZOrderOnTop(false);
                memberView.getAvatarImageView().setVisibility(View.VISIBLE);
            }
            //更新麦克风 摄像头小图标
            setLocalAudioVideoIcons(info);
        } else {
            if (info.isVideoOff()) {
                videoView.setZOrderMediaOverlay(true);
                memberView.getSurfaceViewContainer().setVisibility(View.VISIBLE);
                memberView.getAvatarImageView().setVisibility(View.VISIBLE);
                if(memberInfo != null){
                    memberView.setNickname(memberInfo.nickName);
                }else{
                    if(EMClient.getInstance().getCurrentUser().equals(info.getUserId())){
                        memberView.setNickname(PreferenceManager.getInstance().getCurrentUserNick());
                    }
                }
            } else {
                videoView.setZOrderMediaOverlay(true);
                memberView.getAvatarImageView().setVisibility(View.GONE);
                memberView.getSurfaceViewContainer().setVisibility(View.VISIBLE);
                memberView.setNickname(null);
            }
        }
        boolean isSelf = info.getUserId().equals(EMClient.getInstance().getCurrentUser());
        if (info.isVideoOff()) {
            if (isSelf) {
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(null);
            } else {
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(info.getStreamId(), null);
            }
        } else {
            if (isSelf) {
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(info.getVideoView());
            } else {
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(info.getStreamId(), info.getVideoView());
            }
        }
    }

    private int getViewIdByStreamId(String userId) {
        if (mMemberViewIds.containsKey(userId)) {
            return mMemberViewIds.get(userId).intValue();
        }
        int viewId = View.generateViewId();
        mMemberViewIds.put(userId, viewId);
        return viewId;
    }

    /**
     * 根据角色判断摄像头麦克风是否禁用
     *
     * @param role
     */
    private void setBtn_micAndBtn_vedio(EMConferenceManager.EMConferenceRole role) {
        if (role == EMConferenceManager.EMConferenceRole.Audience) {
            //设置麦克风和摄像头关闭  按钮不可操作
            EMClient.getInstance().conferenceManager().closeVideoTransfer();
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();

            normalParam.setAudioOff(true);
            localStream.setAudioOff(true);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
            mic_view= (TextView) findViewById(R.id.text_call_mic);
            video_view =(TextView)findViewById(R.id.text_call_video);
            mic_view.setText("解除静音");
            video_view.setText("打开视频");

            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);

            btn_screenShare.setClickable(false);
            btn_screenShare.setVisibility(View.GONE);

            btn_mic_layout.setActivated(false);
            btn_video_layout.setActivated(false);

            avatarView.setVisibility(View.VISIBLE);

            headImageurl = DemoApplication.baseurl;
            headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
            loadImage();

            btn_mic_layout.setEnabled(false);
            btn_video_layout.setEnabled(false);
        } else {
            btn_mic_layout.setEnabled(true);
            btn_video_layout.setEnabled(true);

            btn_screenShare.setClickable(true);
            btn_screenShare.setVisibility(View.VISIBLE);

            if (PreferenceManager.getInstance().isCallAudio()) {
                normalParam.setAudioOff(false);
                localStream.setAudioOff(false);
                btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
                speak_show_view.setVisibility(View.GONE);
                EMClient.getInstance().conferenceManager().openVoiceTransfer();
                mic_view.setText("静音");
            } else {
                normalParam.setAudioOff(true);
                localStream.setAudioOff(true);
                btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
                speak_show_view.setVisibility(View.VISIBLE);
                speak_show_view.setBackgroundResource(R.drawable.call_mute_big);
                EMClient.getInstance().conferenceManager().closeVoiceTransfer();
                mic_view.setText("解除静音");
            }
            if (PreferenceManager.getInstance().isCallVideo()) {
                normalParam.setVideoOff(false);
                localStream.setVideoOff(false);
                btn_video.setBackgroundResource(R.drawable.em_call_video_on);
                avatarView.setVisibility(View.GONE);
                video_view.setText("关闭视频");
                EMClient.getInstance().conferenceManager().openVideoTransfer();
            } else {
                normalParam.setVideoOff(true);
                localStream.setVideoOff(true);
                btn_video.setBackgroundResource(R.drawable.em_call_video_off);
                avatarView.setVisibility(View.VISIBLE);
                video_view.setText("打开视频");
                EMClient.getInstance().conferenceManager().closeVideoTransfer();

                headImageurl = DemoApplication.baseurl;
                headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                loadImage();
            }
            btn_mic_layout.setActivated(normalParam.isAudioOff());
            btn_video_layout.setActivated(normalParam.isVideoOff());
        }
    }

    /**
     * 上麦方法封装
     */
    private void onwheat() {
        if (conferenceSession.getConferenceProfiles() != null) {
            if (conferenceSession.getConferenceProfiles().size() > 0) {
                MultiMemberView lastCheckedMemberView = findViewById(lastSelectedId);
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(lastCheckedMemberView.getStreamId(), null);
                EMClient.getInstance().conferenceManager().updateLocalSurfaceView(null);
                EMCallSurfaceView lastSurfaceView = (EMCallSurfaceView) largeSurfacePreview.getChildAt(0);
                lastSurfaceView.getRenderer().dispose();

                largeSurfacePreview.removeAllViews();
                lastCheckedMemberView.getSurfaceViewContainer().removeAllViews();
                lastSurfaceView = new EMCallSurfaceView(getApplicationContext());
                lastSurfaceView.setZOrderMediaOverlay(true);
                lastSurfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                lastCheckedMemberView.getSurfaceViewContainer().addView(lastSurfaceView);
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(lastCheckedMemberView.getStreamId(), lastSurfaceView);
            }
        }
        //开始推流
        publish();
    }

    /**
     * 下麦方法封装
     */
    private void offwheat() {
        if (localuserProfile != null) {
            //停止推流
            unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.NORMAL));
            List<ConferenceMemberInfo> userProfiles = conferenceSession.getConferenceProfiles();
            try {
                if (userProfiles != null && !userProfiles.isEmpty()) {
                    int index = -1;
                    for (int i = 0; i < userProfiles.size(); i++) {
                        ConferenceMemberInfo userProfile = userProfiles.get(i);
                        if (userProfile != null && userProfile.getStreamId() != null && userProfile.getStreamId().equals(localuserProfile.getStreamId())) {
                            index = i;
                            break;
                        }
                    }
                    ConferenceMemberInfo removedUserProfile = userProfiles.remove(index);
                    //removeConferenceView(removedUserProfile.getUserId());
                    removeConferenceView(removedUserProfile.getStreamId());
                    localuserProfile = null;
                    if (userProfiles.size() == 0) {
                        avatarView.setVisibility(View.VISIBLE);
                        headImageurl = DemoApplication.baseurl;
                        headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                        loadImage();
                    } else {
                        avatarView.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //显示主播
        if (!ConferenceInfo.Initflag) {
            if (streamList.size() > 0) {
                avatarView.setVisibility(View.GONE);
                for (int i = 0; i < streamList.size(); i++) {
                    onStreamAdded(streamList.get(i));
                }
            }
            ConferenceInfo.Initflag = true;
        }
    }

    /**
     * 大屏上麦克风 摄像头小图标状态切换
     */
    private void setLocalAudioVideoIcons(ConferenceMemberInfo info) {
        if (info.isAudioOff()) {
            speak_show_view.setVisibility(View.VISIBLE);
            speak_show_view.setBackgroundResource(R.drawable.call_mute_big);
        } else {
           speak_show_view.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_speak_setting:
                      voiceDeviceSwitch();
                      break;
                case R.id.btn_call_mic_layout:
                    voiceSwitch();
                    break;
                case R.id.btn_call_video_layout:
                    videoSwitch();
                    break;
                case R.id.btn_switch_camera:
                    changeCamera();
                    break;
                case R.id.btn_call_hangup_layout:
                    hangup();
                    break;
                case R.id.btn_talker_list_layout:
                    opentalkerlist();
                    break;
                case R.id.btn_screenShare:
                    screenShare();
                    break;
                case R.id.btn_expansion:
                    setbtnexpansion();
                    break;
                case R.id.btn_even_wheat_layout:
                    requesteven_wheat();
                    break;
                case R.id.root_layout:
                    if (bottomContainer.getVisibility() == View.VISIBLE) {
                        if(conferenceSession.getConferenceProfiles() != null){
                            if ((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 1) ||
                                    (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 0)) {
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 100));
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                bottomContainer11.setLayoutParams(params);
                                bottomContainer11.setVisibility(View.VISIBLE);
                                bottomContainer.setVisibility(View.GONE);
                                bottomContainerView.setVisibility(View.VISIBLE);
                            } else {
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 0));
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                bottomContainer11.setLayoutParams(params);
                                bottomContainer11.setVisibility(View.GONE);
                                bottomContainer.setVisibility(View.GONE);
                                bottomContainerView.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        if(conferenceSession.getConferenceProfiles() != null){
                            if ((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 1) ||
                                    (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 0)) {
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 180));
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                bottomContainer11.setLayoutParams(params);
                                bottomContainer11.setVisibility(View.VISIBLE);
                                bottomContainer.setVisibility(View.VISIBLE);
                                bottomContainerView.setVisibility(View.VISIBLE);
                            } else {
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 80));
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                bottomContainer11.setLayoutParams(params);
                                bottomContainer11.setVisibility(View.VISIBLE);
                                bottomContainer.setVisibility(View.VISIBLE);
                                bottomContainerView.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void setbtnexpansion(){
        if(expansionflag){
            topContainer.setVisibility(View.GONE);
            btn_expansion.setBackgroundResource(R.drawable.ic_comments_down);
        }else {
            topContainer.setVisibility(View.VISIBLE);
            btn_expansion.setBackgroundResource(R.drawable.ic_comments_up);
        }
        expansionflag = !expansionflag;
    }

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
        addOrUpdateStreamList(null, "local-stream");
        //设置视频分辨率
        String CameraResolution = PreferenceManager.getInstance().getCallFrontCameraResolution();
        if (CameraResolution.equals("360P")) {
            normalParam.setVideoWidth(480);
            normalParam.setVideoHeight(360);
            normalParam.setMinVideoKbps(400);
        } else if (CameraResolution.equals("(Auto)480P")) {
            normalParam.setVideoWidth(720);
            normalParam.setVideoHeight(480);
            normalParam.setMinVideoKbps(600);
        } else if (CameraResolution.equals("720P")) {
            normalParam.setVideoWidth(1280);
            normalParam.setVideoHeight(720);
            normalParam.setMinVideoKbps(1000);
        }
        EMClient.getInstance().conferenceManager().publish(normalParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        conference.setPubStreamId(value, EMConferenceStream.StreamType.NORMAL);
                        addOrUpdateStreamList("local-stream", value);
                        PhoneStateManager.get(ConferenceActivity.this).addStateCallback(phoneStateCallback);

                        selfRadioButtonId = getViewIdByStreamId(localStream.getStreamId());
                        localViewContainer = new MultiMemberView(ConferenceActivity.this);
                        localViewContainer.setId(selfRadioButtonId);
                        localViewContainer.setChecked(true);
                        localViewContainer.setUsername(EMClient.getInstance().getCurrentUser());
                        localViewContainer.setVideoOff(localStream.isVideoOff());
                        localViewContainer.setAudioOff(localStream.isAudioOff());

                        localViewContainer.setHeadImage(PreferenceManager.getInstance().getCurrentUserAvatar());
                        localViewContainer.setOnCheckedChangeListener(mOnCheckedChangeListener);
                        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        memberContainer.addView(localViewContainer, params);

                        if (localViewContainer.isVideoOff()) {
                            setBigImageView(localViewContainer);
                            avatarView.setVisibility(View.VISIBLE);
                            headImageurl = DemoApplication.baseurl;
                            headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                            loadImage();
                            largeSurfacePreview.setVisibility(View.GONE);
                        } else {
                            avatarView.setVisibility(View.GONE);
                            largeSurfacePreview.setVisibility(View.VISIBLE);
                        }
                        lastSelectedId = selfRadioButtonId;
                        //imMembers.add(EMClient.getInstance().getCurrentUser());


                        avatarView.setVisibility(View.GONE);
                        EMCallSurfaceView localView = new EMCallSurfaceView(ConferenceActivity.this);
                        localView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                        localView.setZOrderOnTop(false);
                        localView.setZOrderMediaOverlay(false);
                        localuserProfile = new ConferenceMemberInfo();
                        localuserProfile.setUserId(EMClient.getInstance().getCurrentUser());
                        localuserProfile.setAudioOff(localStream.isAudioOff());
                        localuserProfile.setAudioOff(localStream.isVideoOff());
                        localuserProfile.setVideoView(localView);
                        localuserProfile.setStreamId(localStream.getStreamId());

                        if(localStream.isVideoOff()){
                            localViewContainer.setNickname(PreferenceManager.getInstance().getCurrentUserNick());
                        }else{
                            localViewContainer.setNickname("");
                        }

                        if (conferenceSession.getConferenceProfiles() == null) {
                            List<ConferenceMemberInfo> conferenceUserProfiles = new ArrayList<>();
                            conferenceUserProfiles.add(0, localuserProfile);
                            conferenceSession.setConferenceProfiles(conferenceUserProfiles);
                        } else {
                            if(conferenceSession.getConferenceMemberInfo(EMClient.getInstance().getCurrentUser()) != null){
                                //当前已经是主播
                                return;
                            }
                            conferenceSession.getConferenceProfiles().add(0, localuserProfile);
                        }
                        EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView);
                        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params1.addRule(RelativeLayout.CENTER_IN_PARENT);
                        largeSurfacePreview.addView(localView, params1);
                        localViewContainer.getAvatarImageView().setVisibility(View.VISIBLE);
                        //打开小窗口的 麦克风 摄像头小图标
                        setLocalAudioVideoIcons(localuserProfile);

                        if(ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin){
                            admin_show_view.setVisibility(View.VISIBLE);
                        }else {
                            admin_show_view.setVisibility(View.GONE);
                        }

                        localViewContainer.setStreamId(localStream.getStreamId());
                        localuserProfile.setStreamId(localStream.getStreamId());
                        imMembers.add(localStream.getStreamId());
                        if (!PreferenceManager.getInstance().isCallVideo()) {
                            avatarView.setVisibility(View.VISIBLE);

                            headImageurl = DemoApplication.baseurl;
                            headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                            loadImage();
                        }

                        if (!ConferenceInfo.Initflag) {
                            if (streamList.size() > 0) {
                                for (int i = 0; i < streamList.size(); i++) {
                                    onStreamAdded(streamList.get(i));
                                }
                            }
                            ConferenceInfo.Initflag = true;
                        }
                    }
                });
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
        if (ScreenCaptureManager.getInstance().state == ScreenCaptureManager.State.RUNNING) {
            if (!TextUtils.isEmpty(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP))
                    && publishId.equals(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP))) {
                ScreenCaptureManager.getInstance().stop();
            }
        }
        EMClient.getInstance().conferenceManager().unpublish(publishId, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                EMLog.i(TAG, "unpublish scuessed ");
                if(EMClient.getInstance().conferenceManager().isCreator() && PreferenceManager.getInstance().isPushCDN()){
                    updatelayout();
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.i(TAG, "unpublish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }


    /**
     * 音频播放设备切换
     */
    private void voiceDeviceSwitch(){
        SwitchAudioDialog  dialog =  SwitchAudioDialog.getNewInstance(EMClient.getInstance().getCurrentUser());
        dialog.setAppCompatActivity(this);
        dialog .show(this.getSupportFragmentManager(), "SwitchAudioDialog");
    }

    /**
     *切换音频设备
     */
    public void speakSwitch(int type) {
        if (type == 0) {
            openSpeaker();
         }else{
            closeSpeaker();
         }
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
            mic_view.setText("静音");
            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "indexUpdate",EMClient.getInstance().getCurrentUser());
        } else {
            normalParam.setAudioOff(true);
            localStream.setAudioOff(true);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
            mic_view.setText("解除静音");
            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "indexUpdate",EMClient.getInstance().getCurrentUser());
        }
        localuserProfile.setVideoOff(localStream.isVideoOff());
        localuserProfile.setAudioOff(localStream.isAudioOff());
        updateConferenceMemberView(localuserProfile);
    }

    /**
     * 视频开关
     */
    private void videoSwitch() {
        EMLog.i(TAG, "videoSwitch  State:" + normalParam.isVideoOff());
        if (normalParam.isVideoOff()) {
            normalParam.setVideoOff(false);
            localStream.setVideoOff(false);
            btn_video.setBackgroundResource(R.drawable.em_call_video_on);
            video_view.setText("关闭视频");
            EMClient.getInstance().conferenceManager().openVideoTransfer();
        } else {
            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);
            video_view.setText("打开视频");
            EMClient.getInstance().conferenceManager().closeVideoTransfer();
        }
        localuserProfile.setVideoOff(localStream.isVideoOff());
        localuserProfile.setAudioOff(localStream.isAudioOff());
        updateConferenceMemberView(localuserProfile);
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        EMLog.i(TAG, "videoSwitch  changeCamera");
        EMClient.getInstance().conferenceManager().switchCamera();
    }

    /**
     * 更新cdn推流画布
     */
    private void updatelayout(){
        int streamcount = streamList.size();
        if(ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience){
            streamcount++;
            if(btn_screenShare.isActivated()){
                streamcount++;
            }
        }
        int column = new Double(Math.sqrt(streamcount)).intValue();
        if(column * column < streamcount)
            column += 1;
        int row = (streamcount + column -1)/column;
        int index = 0;
        int cellWidth = ConferenceInfo.CanvasWidth/column;
        int cellHeight = ConferenceInfo.CanvasHeight/row;

        List<EMLiveRegion> regionsList = new LinkedList<>();
        for(EMConferenceStream streamInfo : streamList){
            if(streamInfo != null){
                int curRow = index/column;
                int curColumn = index - curRow * column;
                EMLiveRegion region = new EMLiveRegion();
                region.setStreamId(streamInfo.getStreamId());
                region.setZorder(ConferenceInfo.rzorderTop);
                if(streamInfo.getStreamType() == EMConferenceStream.StreamType.DESKTOP)
                    region.setStyle(EMLiveRegion.EMRegionStyle.FIT);
                else
                    region.setStyle(EMLiveRegion.EMRegionStyle.FILL);
                region.setX(curColumn * cellWidth);
                region.setY(curRow * cellHeight);
                region.setWidth((new Double(cellWidth)).intValue());
                region.setHeight((new Double(cellHeight)).intValue());
                region.setZorder(1);
                regionsList.add(region);
            }
            index++;
        }

        if(ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience){
            int curRow = index/column;
            int curColumn = index - curRow * column;
            EMLiveRegion region = new EMLiveRegion();
            region.setStreamId(ConferenceInfo.localNomalStreamId);
            region.setStyle(EMLiveRegion.EMRegionStyle.FILL);
            region.setX(curColumn * cellWidth);
            region.setY(curRow * cellHeight);
            region.setZorder(ConferenceInfo.rzorderTop);
            region.setWidth((new Double(cellWidth)).intValue());
            region.setHeight((new Double(cellHeight)).intValue());
            region.setZorder(1);
            regionsList.add(region);
            index++;
            if(btn_screenShare.isActivated()){
                curRow = index/column;
                curColumn = index - curRow * column;
                EMLiveRegion screenShare = new EMLiveRegion();
                screenShare.setStreamId(ConferenceInfo.localDeskStreamId);
                screenShare.setStyle(EMLiveRegion.EMRegionStyle.FILL);
                screenShare.setX(curColumn * cellWidth);
                screenShare.setY(curRow * cellHeight);
                screenShare.setZorder(ConferenceInfo.rzorderTop);
                screenShare.setWidth((new Double(cellWidth)).intValue());
                screenShare.setHeight((new Double(cellHeight)).intValue());
                screenShare.setZorder(1);
                regionsList.add(screenShare);
                index++;
            }
        }
        EMClient.getInstance().conferenceManager().updateLiveLayout(regionsList,new EMValueCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                        EMLog.i(TAG, "updateLiveLayout  success, result: " + value);
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.i(TAG, "updateLiveLayout  failed, error: " + error + " - " + errorMsg);
                    }
                });
        ConferenceInfo.rzorderTop++;
    }

    /**
     * 共享桌面
     */
    private void screenShare(){
        if (btn_screenShare.isActivated()) {
            btn_screenShare.setActivated(false);
            btn_screenShare.setBackgroundResource(R.drawable.call_screenshare);
            unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP));
        } else {
            btn_screenShare.setActivated(true);
            btn_screenShare.setBackgroundResource(R.drawable.call_screenshare_cancel);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                desktopParam.setShareView(null);
            } else {
                desktopParam.setShareView(activity.getWindow().getDecorView());
            }

            DisplayMetrics dm = getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            EMClient.getInstance().callManager().getCallOptions().setVideoResolution(screenWidth, screenHeight);
            EMClient.getInstance().conferenceManager().publish(desktopParam, new EMValueCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    conference.setPubStreamId(value, EMConferenceStream.StreamType.DESKTOP);
                    ScreenCaptureManager.State state = ScreenCaptureManager.getInstance().state;
                    startScreenCapture();
                }
                @Override
                public void onError(int error, String errorMsg) {

                }
            });
        }
    }

    private void startScreenCapture() {
        ScreenCaptureManager.getInstance().init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ScreenCaptureManager.State state = ScreenCaptureManager.getInstance().state;
            if(ScreenCaptureManager.getInstance().state == ScreenCaptureManager.State.IDLE) {
                ScreenCaptureManager.getInstance().init(activity);
                ScreenCaptureManager.getInstance().setScreenCaptureCallback(new ScreenCaptureManager.ScreenCaptureCallback() {
                    @Override
                    public void onBitmap(Bitmap bitmap) {
                        EMClient.getInstance().conferenceManager().inputExternalVideoData(bitmap);
                    }
                });
            }
        }
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
                if(adminList.size() > 0){
                    EMConferenceMember  adminMemberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(adminList.get(0));
                    EMClient.getInstance().conferenceManager().applyTobeSpeaker(adminMemberInfo.memberId);
                }else{
                    Toast.makeText(getApplicationContext(), "本房间还未指定主持人，不允许上麦!", Toast.LENGTH_SHORT).show();
                }
            } else { // 已经是主播，直接推流
                //publish();
                setRequestBtnState(STATE_TALKER);
            }
        } else if (btnState == STATE_TALKER) { // 当前按钮状态是主播，需要下麦
            if (streamList.size() == 0) {  //当前只有一个主持人不允许下麦
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "当前只有您一个主持人，不允许下麦!", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Talker || conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin) { // 申请下麦
                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), EMClient.getInstance().getCurrentUser());
                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                        , new EMConferenceMember(memName, null, null, null)
                        , EMConferenceManager.EMConferenceRole.Audience, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                                EMLog.i(TAG, "requesteven_wheat  request_tobe_audience changeRole success, result: " + value);
                            }

                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.i(TAG, "requesteven_wheat  request_tobe_audience failed, error: " + error + " - " + errorMsg);
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
            offwheat();
            btn_screenShare.setVisibility(View.GONE);
            if(btn_screenShare.isActivated()){
                ScreenCaptureManager.getInstance().stop();
            }
            btn_even_wheat.setBackgroundResource(R.drawable.em_call_request_connect);
            even_wheat_view.setText("上麦");
            if(adminList != null){
                if(adminList.contains(EMClient.getInstance().getCurrentUser())) {
                    adminList.remove(EMClient.getInstance().getCurrentUser());
                }
            }
        } else if (state == STATE_TALKER) {
            onwheat();
            btn_screenShare.setVisibility(View.VISIBLE);
            btn_screenShare.setBackgroundResource(R.drawable.call_screenshare);
            btn_screenShare.setActivated(false);

            btn_even_wheat.setBackgroundResource(R.drawable.em_call_request_disconnect);
            even_wheat_view.setText("下麦");
        }
    }

    /**
     * 挂断会议
     */
     private void hangup(){
         //当前用户是主持人
         if(ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience){
             if(EMClient.getInstance().conferenceManager().isCreator() && PreferenceManager.getInstance().isPushCDN()){
                ConferenceInfo.rzorderTop = 1;
             }
             if(adminList.contains(EMClient.getInstance().getCurrentUser())){
                 HangUpDialog  dialog =  HangUpDialog.getNewInstance(EMClient.getInstance().getCurrentUser());
                 dialog.setAppCompatActivity(this);
                 dialog .show(this.getSupportFragmentManager(), "HangUpDialog");
             }else{
                 exitConference();
             }
         }else{
             exitConference();
         }
     }

    /**
     * 退出会议
     */
    public void exitConference() {
        ScreenCaptureManager.getInstance().stop();
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
                EMLog.i(TAG, "start  MainActivity");
                Intent intent = new Intent(ConferenceActivity.this, MainActivity.class);
                startActivity(intent);
                EMLog.i(TAG, "finish ConferenceActivity");
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

    /**
     * 销毁会议
     */

    public void destoryConference() {
        ScreenCaptureManager.getInstance().stop();
        stopAudioTalkingMonitor();
        timeHandler.stopTime();

        // Stop to watch the phone call state.
        PhoneStateManager.get(ConferenceActivity.this).removeStateCallback(phoneStateCallback);
        EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
            @Override
            public void onSuccess(Object value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "您已成功销毁当前会议！", Toast.LENGTH_SHORT).show();
                    }
                });
                EMLog.i(TAG, "start  MainActivity");
                Intent intent = new Intent(ConferenceActivity.this, MainActivity.class);
                startActivity(intent);
                EMLog.i(TAG, "finish ConferenceActivity");
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

    BroadcastReceiver bluetoothReceiver;
    /**
     * 监听Sco变化广播
     */
    private void registerBluetoothBroadCast() {
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -100);
                EMLog.i(TAG, "registerBluetoothBroadCast :EXTRA_SCO_AUDIO_STATE:" + state);
            }
        };
        registerReceiver(bluetoothReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
    }


    /**
     * 判断蓝牙耳机是否连接
     * @return
     */
    private boolean isBluetoothHeadsetConnected() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
            return true;
        }
        return false;
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

        if (isBluetoothHeadsetConnected()) {
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
        }
    }


    /**
     * 关闭扬声器，即开启听筒播放模式
     * 更多内容看{@link #openSpeaker()}
     */
    public void closeSpeaker() {
        // 检查是否已经开启扬声器
        if (audioManager.isSpeakerphoneOn()) {
            // 关闭扬声器
            audioManager.setSpeakerphoneOn(false);
        }
        // 设置声音模式为通讯模式
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (isBluetoothHeadsetConnected()) {
            EMLog.i("zxg", "need start BluetoothSco");
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
        }
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
                EMLog.i(TAG, "onMemberJoined  nickName:" + member.nickName + " " + member.extension);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), member.nickName+ "已进入房间",
                                Toast.LENGTH_SHORT).show();
                        if(memberList != null){
                            if (!memberList.contains(member)) {
                                memberList.add(member);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onMemberExited(final EMConferenceMember member) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMLog.i(TAG, "onMemberJoined  nickName:" + member.nickName + " " + member.extension);
                String memName = EasyUtils.useridFromJid(member.memberName);
                if (EMClient.getInstance().getCurrentUser().equals(memName)) {
                    setRequestBtnState(STATE_AUDIENCE);
                }
                Toast.makeText(getApplicationContext(), member.nickName+ "已退出房间",
                        Toast.LENGTH_SHORT).show();
                if(adminList != null){
                    if(adminList.contains(memName)){
                        adminList.remove(memName);
                    }
                }
                EMConferenceMember currentMember = ConferenceInfo.getInstance().getConferenceStream(member.memberId);
                if(currentMember != null) {
                    memberList.remove(currentMember);
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
                if (conferenceSession == null) {
                    EMLog.e(TAG, "onStreamAdd callSession is null");
                    return;
                }

                List<ConferenceMemberInfo> userProfiles = conferenceSession.getConferenceProfiles();
                if (userProfiles == null) {
                    userProfiles = new ArrayList<>();
                    conferenceSession.setConferenceProfiles(userProfiles);
                }
                String appKey = EMClient.getInstance().getOptions().getAppKey();
                String memberName = stream.getMemberName();
                if (appKey != null && appKey.length() < memberName.length()) {
                    String username = memberName.substring(appKey.length() + 1);
                    if (username.equals(EMClient.getInstance().getCurrentUser())) {
                        if (userProfiles.isEmpty()) {
                            throw new RuntimeException("userProfile isEmpty");

                        }
                    } else {
                        if (!streamList.contains(stream)) {
                            streamList.add(stream);
                            if(stream.getStreamType() != EMConferenceStream.StreamType.DESKTOP){
                                talkerList.add(stream);
                            }
                            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "streamListUpdate", stream.getUsername());
                        }
                        ConferenceMemberInfo userProfile = new ConferenceMemberInfo();
                        userProfile.setStreamId(stream.getStreamId());
                        userProfile.setUserId(username);
                        userProfile.setAudioOff(stream.isAudioOff());
                        userProfile.setVideoOff(stream.isVideoOff());
                        userProfile.setDesktop(stream.getStreamType() == EMConferenceStream.StreamType.DESKTOP);

                        EMCallSurfaceView videoView = new EMCallSurfaceView(DemoHelper.getInstance().getContext());
                        videoView.setZOrderMediaOverlay(true);
                        videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                        userProfile.setVideoView(videoView);
                        userProfiles.add(userProfile);
                        subscribe(stream, videoView);
                        addConferenceView(userProfile);
                        if(EMClient.getInstance().conferenceManager().isCreator() && PreferenceManager.getInstance().isPushCDN()){
                            updatelayout();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onStreamRemoved(final EMConferenceStream stream){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String username = stream.getUsername();
                EMLog.i(TAG, "onStreamRemoved  start userID: " + username);
                List<ConferenceMemberInfo> userProfiles = conferenceSession.getConferenceProfiles();
                try {
                    if (userProfiles != null && !userProfiles.isEmpty()) {
                        int index = -1;
                        for (int i = 0; i < userProfiles.size(); i++) {
                            ConferenceMemberInfo userProfile = userProfiles.get(i);
                            if (userProfile != null && userProfile.getStreamId() != null && userProfile.getStreamId().equals(stream.getStreamId())) {
                                index = i;
                                break;
                            }
                        }
                        streamList.remove(stream);
                        if(talkerList.contains(stream)){
                            talkerList.remove(stream);
                        }
                        ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "streamListUpdate",username);
                        ConferenceMemberInfo removedUserProfile = userProfiles.remove(index);
                        removeConferenceView(removedUserProfile.getStreamId());
                        if(EMClient.getInstance().conferenceManager().isCreator() && PreferenceManager.getInstance().isPushCDN()){
                            updatelayout();
                        }
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
                String appKey = EMClient.getInstance().getOptions().getAppKey();
                String memberName = stream.getMemberName();
                if (appKey != null && appKey.length() < memberName.length()) {
                    String username = memberName.substring(appKey.length() + 1);
                    ConferenceMemberInfo userProfile = conferenceSession.getConferenceMemberInfo(username);
                    if (userProfile != null) {
                        ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "indexUpdate",username);
                        userProfile.setVideoOff(stream.isVideoOff());
                        userProfile.setAudioOff(stream.isAudioOff());
                        updateConferenceMemberView(userProfile);
                    }
                    EMConferenceMember conferenceMember = ConferenceInfo.getInstance().getConferenceMemberInfo(username);
                    if (conferenceMember != null ) {
                        Toast.makeText(getApplicationContext(), conferenceMember.nickName  + " 更新了音视频流!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * 当前用户被踢出会议回调
     *
     * @param error
     * @param message
     */
    @Override
    public void onPassiveLeave(final int error, final String message) {
        EMLog.i(TAG, "onPassiveLeave  error :" + error + " message:" + message);
        if(!this.isFinishing()){
            //被踢后马上退出会议
            exitConference();
        }
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
                if (speakers != null) {
                    if (speakers.size() > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(speakers.contains(localStream.getStreamId())){
                                    setSpeakIcon(localStream.getStreamId(),true);
                                }else{
                                    setSpeakIcon(localStream.getStreamId(),false);
                                }
                                for (EMConferenceStream streamInfo : streamList){
                                    if (speakers.contains(streamInfo.getStreamId())){
                                        setSpeakIcon(streamInfo.getStreamId(),true);
                                    }else{
                                        setSpeakIcon(streamInfo.getStreamId(),false);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void setSpeakIcon(String username , boolean flag) {
        if (flag){
            int viewId = getViewIdByStreamId(username);
            MultiMemberView memberView = findViewById(viewId);
            if(memberView != null){
                memberView.setAudioSpeak();
                if (viewId == lastSelectedId){
                    if (viewId != selfRadioButtonId) {
                        speak_show_view.setVisibility(View.VISIBLE);
                        speak_show_view.setBackgroundResource(R.drawable.call_unmute_big);
                    } else{
                        if (localStream.isAudioOff()) {
                            speak_show_view.setVisibility(View.VISIBLE);
                            speak_show_view.setBackgroundResource(R.drawable.call_mute_big);
                            memberView.setAudioNoSpeak();
                        }else{
                            speak_show_view.setVisibility(View.VISIBLE);
                            speak_show_view.setBackgroundResource(R.drawable.call_unmute_big);
                            memberView.setAudioSpeak();
                        }
                    }
                } else{
                    if(viewId != selfRadioButtonId) {
                        if(localStream.isAudioOff()) {
                            memberView.setAudioNoSpeak();
                        } else {
                            memberView.setAudioSpeak();
                        }
                    }
                }
            }
        }else{
            int viewId = getViewIdByStreamId(username);
            MultiMemberView memberView = findViewById(viewId);
            if(memberView != null){
                memberView.setAudioNoSpeak();
                if (viewId == lastSelectedId){
                    if (viewId == selfRadioButtonId) {
                        if (localStream.isAudioOff()) {
                            speak_show_view.setVisibility(View.VISIBLE);
                            speak_show_view.setBackgroundResource(R.drawable.call_mute_big);
                            memberView.setAudioNoSpeak();
                        } else {
                            speak_show_view.setVisibility(View.GONE);
                            memberView.setAudioSpeak();
                        }
                    }else{
                        speak_show_view.setVisibility(View.GONE);
                    }
                }
            }
        }
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


    /**
     * 收到其他人的会议邀请
     */
    @Override
    public void onReceiveInvite(final String confId, String password, String extension) {
    }

    /**
     * 主持人增加回调
     *
     * @param streamId
     */
    @Override
    public void onAdminAdded(String streamId){
        EMLog.i(TAG, "onAdminAdd :" + streamId);
        EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceStream(streamId);
        if(memberInfo != null){
            String memName = EasyUtils.useridFromJid(memberInfo.memberName);
            if(!adminList.contains(memName)){
                adminList.add(memName);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), memberInfo.nickName + " 已成为主持人",
                            Toast.LENGTH_SHORT).show();
                    int viewId = getViewIdByStreamId(streamId);
                    MultiMemberView memberView = findViewById(viewId);
                    if(memberView != null){
                        memberView.setUsername(memName);
                    }
                    if(viewId == lastSelectedId){
                        admin_show_view.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    /**
     * 主持人移除回调
     *
     * @param streamId
     */
    @Override
    public void onAdminRemoved(String streamId) {
        EMLog.i(TAG, "onAdminRemove :" + streamId);
        EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceStream(streamId);
        if(memberInfo != null) {
            String memName = EasyUtils.useridFromJid(memberInfo.memberName);
            if(adminList.contains(memName)) {
                adminList.remove(memName);
            }
            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "indexUpdate",memName);
            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "admin", memName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), memberInfo.nickName + "已放弃主持人", Toast.LENGTH_SHORT).show();
                    int viewId = getViewIdByStreamId(streamId);
                    MultiMemberView memberView = findViewById(viewId);
                    if(memberView != null){
                        memberView.setUsername(memName);
                    }
                }
            });
        }
    }

    /**
     * Pub流失败回调
     *
     * @param error
     * @param message
     */
    @Override
    public void onPubStreamFailed(int error, String message) {
        EMLog.i(TAG, "onPubStreamFailed  error :" + error + " message:" + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run(){
                Toast.makeText(getApplicationContext(), "Pub流失败  errorMessage:" + message, Toast.LENGTH_SHORT).show();
                btn_video.setBackgroundResource(R.drawable.em_call_video_off);
                video_view.setText("打开视频");
                normalParam.setVideoOff(true);
                localuserProfile.setVideoOff(true);
                updateConferenceMemberView(localuserProfile);
                EMClient.getInstance().conferenceManager().closeVideoTransfer();
                publish();
            }
        });
    }

    /**
     * Update流失败回调
     *
     * @param error
     * @param message
     */
    @Override
    public void onUpdateStreamFailed(int error, String message) {
        EMLog.i(TAG, "onUpdateStreamFailed  error :" + error + " message:" + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Update流失败  errorMessage:" + message, Toast.LENGTH_SHORT).show();
                btn_video.setBackgroundResource(R.drawable.em_call_video_off);
                video_view.setText("打开视频");
                normalParam.setVideoOff(true);
                localuserProfile.setVideoOff(true);
                updateConferenceMemberView(localuserProfile);
                EMClient.getInstance().conferenceManager().closeVideoTransfer();
            }
        });
    }


    @Override
    public void onRoleChanged(EMConferenceManager.EMConferenceRole role) {
        EMLog.i(TAG, "onRoleChanged, role: " + role);
        //currentRole = role;
        if(conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin && role == EMConferenceManager.EMConferenceRole.Talker){
            conference.setConferenceRole(role);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int viewId = getViewIdByStreamId(localStream.getStreamId());
                    MultiMemberView memberView = findViewById(viewId);
                    if(memberView != null){
                        memberView.setUsername(EMClient.getInstance().getCurrentUser());
                    }
                    if(adminList.contains(EMClient.getInstance().getCurrentUser())){
                        adminList.remove(EMClient.getInstance().getCurrentUser());
                    }
                    ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "admin", EMClient.getInstance().getCurrentUser());
                }
            });
            return;
        }
        conference.setConferenceRole(role);
        if (role == EMConferenceManager.EMConferenceRole.Talker) {
            // 主持人把当前用户角色更改为主播或主持人。
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setBtn_micAndBtn_vedio(role);
                    setRequestBtnState(STATE_TALKER);
                }
            });
        } else if (role == EMConferenceManager.EMConferenceRole.Audience) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 下麦
                    setBtn_micAndBtn_vedio(role);
                    setRequestBtnState(STATE_AUDIENCE);
                }
            });
        } else if (role == EMConferenceManager.EMConferenceRole.Admin) {  //主播变更为主持人
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "您已变更为主持人!", Toast.LENGTH_SHORT).show();
                    if(!adminList.contains(EMClient.getInstance().getCurrentUser())){
                        adminList.add(EMClient.getInstance().getCurrentUser());
                    }
                    int viewId = getViewIdByStreamId(localStream.getStreamId());
                    MultiMemberView memberView = findViewById(viewId);
                    if(memberView != null){
                        memberView.setUsername(EMClient.getInstance().getCurrentUser());
                    }
                    if(viewId == lastSelectedId){
                        admin_show_view.setVisibility(View.VISIBLE);
                    }

                    ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "admin", EMClient.getInstance().getCurrentUser());
                }
            });
        }
    }

    @Override
    public void onGetLocalStreamId(String rtcName, String streamId){
        EMLog.i(TAG, " onUpdatePubStream started  "+rtcName + "  "+ streamId);
        if(rtcName.equals(conference.getPubStreamId(EMConferenceStream.StreamType.NORMAL))){
            ConferenceInfo.localNomalStreamId = streamId;
            if(EMClient.getInstance().conferenceManager().isCreator() && PreferenceManager.getInstance().isPushCDN()){
                updatelayout();
            }
        }else if(rtcName.equals(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP))){
            ConferenceInfo.localDeskStreamId = streamId;
            if(EMClient.getInstance().conferenceManager().isCreator() && PreferenceManager.getInstance().isPushCDN()){
                updatelayout();
            }
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
            if(option == null){
                continue;
            }
            if(usreId.equals(REQUEST_TOBE_MUTE_ALL)){
                try {
                    JSONObject object = new JSONObject(option);
                    String seterId = object.optString("setter");
                    int status = object.optInt("status");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!EMClient.getInstance().getCurrentUser().equals(seterId)){
                                if(status == 0){ //解除静音
                                    normalParam.setAudioOff(false);
                                    localStream.setAudioOff(false);
                                    btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
                                    EMClient.getInstance().conferenceManager().openVoiceTransfer();
                                    localuserProfile.setAudioOff(localStream.isAudioOff());
                                    updateConferenceMemberView(localuserProfile);
                                }else if(status == 1){  //静音
                                    normalParam.setAudioOff(true);
                                    localStream.setAudioOff(true);
                                    btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
                                    EMClient.getInstance().conferenceManager().closeVoiceTransfer();
                                    localuserProfile.setAudioOff(localStream.isAudioOff());
                                    updateConferenceMemberView(localuserProfile);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                if (option.equals(ConferenceAttributeOption.REQUEST_TOBE_SPEAKER)) {
                    if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin) {
                        //申请上麦
                        EMLog.i(TAG, " onAttributesUpdated： talker request_tobe_speaker");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                requestTalkerDisplay(usreId,"","");
                            }
                        });
                    }
                } else if (option.equals(ConferenceAttributeOption.REQUEST_TOBE_AUDIENCE)) {  //申请下麦
                    if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin) {
                        //申请下麦
                    EMLog.i(TAG, " onAttributesUpdated： talker request_tobe_audience");
                    String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                    EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                            , new EMConferenceMember(memName, null, null, null)
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
                }
                }else if((option.equals(ConferenceAttributeOption.REQUEST_TOBE_ADMIN))){ //申请成为主持人
                    //申请上麦是否同意提示框
                    if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin){
                        EMLog.i(TAG, " onAttributesUpdated： talker request_tobe_speaker start");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                requestAdminDisplay(usreId,"","");
                            }
                        });
                    }
                }else if (option.contains("unmute")) { //解除静音
                        try {
                            JSONObject object = new JSONObject(option);
                            JSONArray userArray = object.getJSONArray("uids");
                            for (i = 0; i < userArray.length(); i++) {
                                if (userArray.get(i).equals(EMClient.getInstance().getCurrentUser())) {
                                    normalParam.setAudioOff(false);
                                    localStream.setAudioOff(false);
                                    String userId = userArray.get(i).toString();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
                                            EMClient.getInstance().conferenceManager().openVoiceTransfer();
                                            localuserProfile.setAudioOff(localStream.isAudioOff());
                                            updateConferenceMemberView(localuserProfile);
                                            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "indexUpdate",userId);
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }else if (option.contains("mute")){ //解除静音
                       try {
                            JSONObject object = new JSONObject(option);
                            JSONArray userArray = object.getJSONArray("uids");
                            for(i = 0 ; i< userArray.length(); i++){
                                if(userArray.get(i).equals(EMClient.getInstance().getCurrentUser())){
                                    normalParam.setAudioOff(true);
                                    localStream.setAudioOff(true);
                                    String userId = userArray.get(i).toString();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
                                            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
                                            localuserProfile.setAudioOff(localStream.isAudioOff());
                                            updateConferenceMemberView(localuserProfile);
                                            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "indexUpdate",userId);
                                        }
                                    });
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                }
            }

            //全体静音不能删除会议属性
            if(!usreId.equals(REQUEST_TOBE_MUTE_ALL)){
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
            }
        }
}


    @Override
    public void onReqSpeaker(String memId,String memName,String nickName){
        EMLog.i(TAG, "onReqSpeaker, memName: " + memId + "  nickName:" + nickName);
        requestTalkerDisplay(memName,memId,nickName);
    }

    @Override
    public void onReqAdmin(String memId,String memName,String nickName){
        EMLog.i(TAG, "onReqAdmin, memName: " + memId + "  nickName:" + nickName);
        requestAdminDisplay(memName,memId,nickName);
    }

    @Override
    public void onMute(String adminId, String memId){
        EMLog.i(TAG, "onSetMute, memName: " + memId + "  adminId:" + adminId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                normalParam.setAudioOff(true);
                localStream.setAudioOff(true);
                btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
                EMClient.getInstance().conferenceManager().closeVoiceTransfer();
                localuserProfile.setAudioOff(localStream.isAudioOff());
                updateConferenceMemberView(localuserProfile);
            }
        });
    }

    @Override
    public void onUnMute(String adminId, String memId){
        EMLog.i(TAG, "onSetUnMute, memName: " + memId + "  adminId:" + adminId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                normalParam.setAudioOff(false);
                localStream.setAudioOff(false);
                btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
                EMClient.getInstance().conferenceManager().openVoiceTransfer();
                localuserProfile.setAudioOff(localStream.isAudioOff());
                updateConferenceMemberView(localuserProfile);
            }
        });
    }

    @Override
    public void onMuteAll(boolean mute){
        EMLog.i(TAG, "onSetMuteAll, mute: " + mute);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Admin){
                    if(!mute){ //解除静音
                        normalParam.setAudioOff(false);
                        localStream.setAudioOff(false);
                        btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
                        EMClient.getInstance().conferenceManager().openVoiceTransfer();
                        localuserProfile.setAudioOff(localStream.isAudioOff());
                        updateConferenceMemberView(localuserProfile);
                    }else if(mute){  //静音
                        normalParam.setAudioOff(true);
                        localStream.setAudioOff(true);
                        btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
                        EMClient.getInstance().conferenceManager().closeVoiceTransfer();
                        localuserProfile.setAudioOff(localStream.isAudioOff());
                        updateConferenceMemberView(localuserProfile);
                    }
                }
            }
        });
    }

    @Override
    public void onApplySpeakerRefused(String memId ,String adminId){
        EMLog.i(TAG, "onApplySpeakerRefused, memId: " + memId + "  adminId:" + adminId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "申请上麦失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApplyAdminRefused(String memId ,String adminId){
        EMLog.i(TAG, "onApplyAdminRefused, memId: " + memId + "  adminId:" + adminId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "申请主持人失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 主播已满 踢人下麦 提示对话框
     */
    private void takerFullDialogDisplay(String usreId,String memId) {
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
                takerListChooseDispaly(usreId,memId);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EMLog.i(TAG, "takerFullDialogDisplay diplay cancel id " +usreId);
                EMClient.getInstance().conferenceManager().handleSpeakerApplication(memId,false);
                dialog.dismiss();
            }
        });
    }

    /**
     * 申请上麦提示框
     */
    private void requestTalkerDisplay(String usreId,String memId,String nickName) {
        EMLog.i(TAG, " onAttributesUpdated： requestTalkerDisplay start");
        AlertDialog.Builder builder = new AlertDialog.Builder(ConferenceActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(ConferenceActivity.this, R.layout.activity_talker_full_kick, null);
        TextView infoView = dialogView.findViewById(R.id.info_view);
        Button cancelbtn = dialogView.findViewById(R.id.btn_kick_cancel);
        Button okbtn = dialogView.findViewById(R.id.btn_kick_ok);
        infoView.setText("用户  "+nickName +"\n" +"申请成为主播 是否同意");
        cancelbtn.setText("拒绝");
        okbtn.setText("批准");
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
                EMClient.getInstance().conferenceManager().handleSpeakerApplication(memId,true);
                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                        , new EMConferenceMember(usreId, null, null,null)
                        , EMConferenceManager.EMConferenceRole.Talker, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                                EMLog.i(TAG, " requestTalkerDisplay  request_tobe_speaker changeRole success, result: " + value);
                                dialog.dismiss();
                            }
                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.i(TAG, " requestTalkerDisplay  request_tobe_speaker changeRole failed, error: " + error + " - " + errorMsg);
                                if(error == 823){  //主播已满,进行踢人
                                    EMLog.i(TAG, " onAttributesUpdated： talker is full");
                                    dialog.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            takerFullDialogDisplay(usreId,memId);
                                        }
                                    });
                                }else{
                                    EMClient.getInstance().conferenceManager().handleSpeakerApplication(memId,false);
                                    dialog.dismiss();
                                }
                            }
                        });
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
     * 用户申请成为 主持人提示框
     * @param usreId
     */
    private void requestAdminDisplay(String usreId,String memId,String nickName){
        EMLog.i(TAG, " onAttributesUpdated： requestAdminDisplay start");
        AlertDialog.Builder builder = new AlertDialog.Builder(ConferenceActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(ConferenceActivity.this, R.layout.activity_talker_full_kick, null);
        TextView infoView = dialogView.findViewById(R.id.info_view);
        Button cancelbtn = dialogView.findViewById(R.id.btn_kick_cancel);
        Button okbtn = dialogView.findViewById(R.id.btn_kick_ok);
        infoView.setText("用户  "  + nickName + "\n"+"申请成为主持人 是否同意");
        cancelbtn.setText("拒绝");
        okbtn.setText("批准");
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
                EMLog.i(TAG, " onAttributesUpdated： requestTalkerDisplay  request_tobe_admin start"+ usreId);
                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                EMClient.getInstance().conferenceManager().handleAdminApplication(memId,true);
                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                        , new EMConferenceMember(usreId, null, null,null)
                        , EMConferenceManager.EMConferenceRole.Admin, new EMValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                                EMLog.i(TAG, " requestTalkerDisplay  request_tobe_admin changeRole success, result: " + value);
                            }
                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.i(TAG, " requestTalkerDisplay  request_tobe_admin changeRole failed, error: " + error + " - " + errorMsg);
                            }
                        });
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EMClient.getInstance().conferenceManager().handleSpeakerApplication(memId,false);
                dialog.dismiss();
            }
        });
    }


    /**
     * 展示主持人踢主播列表
     *
     */
    private void takerListChooseDispaly(String usreId,String memId){
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
        recyclerView.setAdapter(adapter);
        adapter.setData(streamList);


        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EMLog.i(TAG, "takerFullDialogDisplay choose position: " + position);
                ChooseTalkerItemAdapter.chooseIndex = position;
                choose_userId = streamList.get(position).getUsername();
                EMLog.i(TAG, "takerFullDialogDisplay choose userId: " + choose_userId);
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
                EMClient.getInstance().conferenceManager().handleSpeakerApplication(memId,true);
                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                        , new EMConferenceMember(memName, null, null ,null)
                        , EMConferenceManager.EMConferenceRole.Audience, new EMValueCallBack<String>(){
                            @Override
                            public void onSuccess(String value) {
                                EMLog.i(TAG, "takerListChooseDispaly ok choose to offline userId " + choose_userId +"  success, result: " + value);
                                //让申请的主播上线
                                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                                        , new EMConferenceMember(usreId, null, null,null)
                                        , EMConferenceManager.EMConferenceRole.Talker, new EMValueCallBack<String>() {
                                            @Override
                                            public void onSuccess(String value) {
                                                EMLog.i(TAG, "takerListChooseDispaly ok choose to online userId " + usreId +"  success, result: " + value);
                                            }
                                            @Override
                                            public void onError(int error, String errorMsg) {
                                                EMLog.i(TAG, "takerListChooseDispaly ok choose to online userId " + usreId +"  error: " + error + " - " + errorMsg);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "申请上麦失败!" + error + "  " + errorMsg, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
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
                EMClient.getInstance().conferenceManager().handleSpeakerApplication(memId,false);
                dialog2.dismiss();
            }
        });
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
        mId = Math.abs(new Random(System.currentTimeMillis()).nextInt());
        Intent intent = new Intent(ConferenceActivity.this, RoomSettingActivity.class);
        intent.putExtra(KEY_ID, mId);
        startActivity(intent);
    }


    /**
     * 获取会议信息
     */
    private void getConferenceInfo(){
        mId = Math.abs(new Random(System.currentTimeMillis()).nextInt());
        Intent intent = new Intent(ConferenceActivity.this, TalkerListActivity.class);
        intent.putExtra(KEY_ID, mId);
        startActivity(intent);
    }

    /**
     * 获取网络图片资源
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
                if (bitmap != null) {
                    avatarView.setImageBitmap(bitmap);
                }
            }
        }.execute(headImageurl);
    }

    /**
     * 检测wifi 连接状态
     * @return
     */
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    /**
     * 检测网络信号
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
    int count = 0;
    private void updateConferenceTime(String time) {
        meeting_duration.setText(time);
        checkWifiState();
        if(count == 0){
            updataSmall();
            getConferenceInfoAdmins();
        }
        count++;
    }

    private void updataSmall(){
        //第二个主播进入
        if(conferenceSession.getConferenceProfiles() != null){
            if((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience
                    && conferenceSession.getConferenceProfiles().size() >= 2) ||
                    (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience
                            && conferenceSession.getConferenceProfiles().size() > 0) ){

                //显示下边的x小窗口列表
                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(rootContainer.getWidth(), dip2px(getApplicationContext(), 180));
                params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                bottomContainer11.setLayoutParams(params2);
                bottomContainer11.setVisibility(View.VISIBLE);
                bottomContainer.setVisibility(View.VISIBLE);
                bottomContainerView.setVisibility(View.VISIBLE);
                bottomContainer11.bringToFront();
            }
        }
    }

    private void getConferenceInfoAdmins(){
        EMClient.getInstance().conferenceManager().getConferenceInfo(ConferenceInfo.getInstance().getConference().getConferenceId(),ConferenceInfo.getInstance().getPassword(),
                new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(EMConference value) {
                        ConferenceInfo.getInstance().getConference().setTalkers(value.getTalkers());
                        ConferenceInfo.getInstance().getConference().setAudienceTotal(value.getAudienceTotal());
                        ConferenceInfo.getInstance().getConference().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().getConference().setMemberNum(value.getMemberNum());

                        adminList = ConferenceInfo.getInstance().getAdmins();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(adminList != null){
                                    if(adminList.size() > 0){
                                        for(int i = 0; i < adminList.size(); i++){
                                            String memName = adminList.get(i);

                                            EMConferenceStream  streamInfo =ConferenceInfo.getInstance().getConferenceStreamByMemId(memName);
                                            if(streamInfo != null){
                                                int viewId = getViewIdByStreamId(streamInfo.getStreamId());
                                                if(EMClient.getInstance().getCurrentUser().equals(memName)){
                                                    admin_show_view.setVisibility(View.VISIBLE);
                                                }
                                                MultiMemberView memberView = findViewById(viewId);
                                                if(memberView != null){
                                                    memberView.setUsername(memName);
                                                }
                                            }

                                        }
                                        if(ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 0){
                                            if(adminList.contains(conferenceSession.getConferenceProfiles().get(0).getUserId())){
                                                admin_show_view.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }

                                }
                            }
                        });
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.i(TAG, "getConferenceInfo failed: error=" + error + ", msg=" + errorMsg);
                    }
                });
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

    private void  updateMute(String key , boolean enable){

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        EMLog.i(TAG, "onActivityResult: " + requestCode + ", result code: " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == ScreenCaptureManager.RECORD_REQUEST_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ScreenCaptureManager.getInstance().start(resultCode, data);
                }
            }
        }
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
        super.onDestroy();
        imMembers.clear();
        if(bluetoothReceiver != null){
            unregisterReceiver(bluetoothReceiver);
        }
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
            } else if(y2 - y1 > 50) {
            } else if(x1 - x2 > 50) {
            } else if(x2 - x1 > 50) {
                streamList.remove(ConferenceInfo.getInstance().getLocalStream());
                exitConference();
            }
        }
        return super.onTouchEvent(event);
    }
}