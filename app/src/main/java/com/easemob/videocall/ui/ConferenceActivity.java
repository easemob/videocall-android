package com.easemob.videocall.ui;


import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.adapter.ViewPagerAdapter;
import com.easemob.videocall.ui.widget.NoScrollViewPager;
import com.easemob.videocall.ui.widget.TouchWebView;
import com.easemob.videocall.utils.ConferenceMemberInfo;
import com.easemob.videocall.utils.ConferenceSession;
import com.easemob.videocall.utils.ConfigManager;
import com.easemob.videocall.utils.LocalBroadcastReceiver;
import com.easemob.videocall.utils.OrientationListener;
import com.easemob.videocall.utils.StringUtils;
import com.easemob.videocall.utils.WhiteBoardRoomInfo;
import com.easemob.videocall.utils.X5WebView;
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
import com.hyphenate.chat.EMWhiteboard;
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
import com.hyphenate.util.NetUtils;
import com.jaouan.compoundlayout.CompoundLayout;
import com.jaouan.compoundlayout.RadioLayoutGroup;

import com.superrtc.mediamanager.ScreenCaptureManager;
import com.superrtc.sdk.VideoView;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.utils.TbsLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.easemob.videocall.ui.WhiteBoardTbsActivity.FIREHOUSE_RESULT_CODE;
import static com.easemob.videocall.utils.ConferenceAttributeOption.REQUEST_TOBE_MUTE_ALL;
import static com.easemob.videocall.utils.ConferenceAttributeOption.WHITE_BOARD;
import static com.hyphenate.util.DensityUtil.dip2px;
import static com.hyphenate.util.DensityUtil.px2dip;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class ConferenceActivity extends AppCompatActivity  implements EMConferenceListener {
    private final String TAG = this.getClass().getSimpleName();
    private static final int STATE_AUDIENCE = 0;
    private static final int STATE_TALKER = 1;
    private ConferenceActivity activity;

    private TextView meeting_roomId_view;
    private TextView meeting_duration;

    private Button btn_mic;
    private Button btn_video;
    private Button btn_talker_list;
    private Button btn_desktop_share;
    private Button btn_whiteboard;

    private RelativeLayout btn_mic_layout;
    private RelativeLayout btn_video_layout;
    private RelativeLayout btn_talker_list_layout;
    private RelativeLayout btn_screenShare_layout;
    private RelativeLayout btn_more_layout;

    private RelativeLayout btn_switch_camera_layout;
    private RelativeLayout btn_audio_device_layout;
    private ImageView btn_audio_device;
    private RelativeLayout btn_hangup_layout;
    private Button btn_speaker_setting;
    private Button btn_expansion;

    private TextView video_view;
    private TextView mic_view;
    private TextView whiteboard_view;
    private TextView desktop_share_text;

    private RelativeLayout avatarView;
    private ImageView headImageView;
    private TextView nickNameView;
    private String currentSelectMemName;

    private EMStreamParam desktopParam;

    private ImageView speak_show_view;
    private ImageView admin_show_view;

    private RelativeLayout memberInfo_layout;
    private ImageView adminImage_view;
    private ImageView speakImage_view;
    private TextView nicknameShow_view;

    private LinearLayout rootContainer;
    private RelativeLayout bottomContainer;
    private RelativeLayout topContainer;
    private RelativeLayout bottomContainer11;
    private HorizontalScrollView bottomContainerView;
    private ScrollView bottomContainerScrollView;
    private NoScrollViewPager viewPager;
    final List<Fragment> fragments = new ArrayList<>();
    private List<View> listView;
    private ViewPagerAdapter vp_Adapter;
    private View conference_Info_view;
    private View desktop_share_view;
    private View more_btn_view;

    private EMCallSurfaceView desktop_share_surfaceview;
    private ImageView desktop_headImage_view;
    private TextView desktop_nickName_view;
    private RelativeLayout desktop_share_avatar;
    private LinearLayout loading_stream_layout;

    private View whiteborad_view;
    private TextView destory_btn;
    private TextView back_btn;
    private ViewGroup mViewParent;
    private boolean mNeedTestPage = false;
    private URL mIntentUrl;
    private String roomUrl;
    private TouchWebView mWebView;
    private ValueCallback<Uri> uploadFile;
    private ValueCallback<Uri[]> uploadFiles;

    private RelativeLayout btn_wheat_layout;
    private RelativeLayout btn_setting_layout;
    private RelativeLayout btn_invite_layout;

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
    ValueAnimator animator;

    private String choose_userId;
    private boolean audio_openSpeaker = false;

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
    private boolean desktopHeadImage = false;
    private PopupWindow popupWindow;

    private String headImageurl = null;
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    public static final String KEY_ID = "ID";
    public static int mId;
    private TelephonyManager telephonyManager;
    private SensorManager mSensorManager;
    private OrientationListener mOrientationListener;
    private boolean portrait = true;
    private List<ConferenceMemberInfo> subMemberList = new LinkedList<>();
    private int subVideoCount = 0;
    int currentScrollX = 0;
    private boolean updateSubVideo = true;
    List<ConferenceMemberInfo> newSubMumber = new LinkedList<>();
    List<ConferenceMemberInfo> commonSubMumber = new LinkedList<>();

    ValueAnimator video_on_animator;
    ValueAnimator video_off_animator;

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

        getWindow().setFormat(PixelFormat.TRANSLUCENT);


        //把会议详情加入viewPaper
        conference_Info_view = View.inflate(this,R.layout.activity_conference_info, null);
        listView = new ArrayList<View>();
        vp_Adapter = new ViewPagerAdapter(listView, this);
        listView.add(conference_Info_view);
        viewPager = (NoScrollViewPager)findViewById(R.id.conference_viewpager);
        viewPager.setAdapter(vp_Adapter);

        //共享桌面加入viewPaper
        desktop_share_view = View.inflate(this,R.layout.activity_conference_whiteboard_desktop, null);
        desktop_share_surfaceview = desktop_share_view.findViewById(R.id.desktop_share_preview);
        desktop_share_avatar = desktop_share_view.findViewById(R.id.desktop_share_avatar);
        desktop_headImage_view = desktop_share_view.findViewById(R.id.desktop_headImage_view);
        desktop_nickName_view = desktop_share_view.findViewById(R.id.desktop_nickname_view);
        loading_stream_layout = desktop_share_view.findViewById(R.id.loading_stream_layout);

        //白板页面加入viewPaper
        whiteborad_view = View.inflate(this,R.layout.activity_white_board,null);
        destory_btn = whiteborad_view.findViewById(R.id.btn_whiteboard_destory);
        back_btn = whiteborad_view.findViewById(R.id.btn_whiteboard_back);
        destory_btn.setVisibility(VISIBLE);
        destory_btn.setBackgroundResource(R.drawable.em_call_scale_fill);
        back_btn.setVisibility(VISIBLE);


        streamList = ConferenceInfo.getInstance().getConferenceStreamList();
        talkerList = ConferenceInfo.getInstance().getTalkerList();
        memberList = ConferenceInfo.getInstance().getConferenceMemberList();
        adminList = ConferenceInfo.getInstance().getAdmins();
        localStream = ConferenceInfo.getInstance().getLocalStream();

        conference = ConferenceInfo.getInstance().getConference();

        rootContainer = (LinearLayout) findViewById(R.id.root_layout);
        bottomContainer = (RelativeLayout) findViewById(R.id.ll_surface_baseline);
        bottomContainerScrollView = (ScrollView)conference_Info_view.findViewById(R.id.surface_baseline_vertical);

        init();

        DemoHelper.getInstance().removeGlobalListeners();

        //增加监听
        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);

        registerBluetoothBroadCast();
        registerInviteBroadCast();

        EMClient.getInstance().conferenceManager().enableStatistics(true);

        OrientationInit();

        //动态订阅流初始化
        subMemberList.clear();
    }


    /*
     初始化
     */
    private void init() {
        activity = this;
        //netInfoView = (ImageView) conference_Info_view.findViewById(R.id.netInfo);
        avatarView = (RelativeLayout) conference_Info_view.findViewById(R.id.img_call_avatar);
        headImageView = (ImageView) conference_Info_view.findViewById(R.id.headImage_view);
        nickNameView = (TextView) conference_Info_view.findViewById(R.id.nickname_view);
        admin_show_view = (ImageView) conference_Info_view.findViewById(R.id.admin_show_view);

        //大屏显示主界面
        memberInfo_layout =  (RelativeLayout) conference_Info_view.findViewById(R.id.show_memberInfo_layout);
        adminImage_view = (ImageView) conference_Info_view.findViewById(R.id.adminImage_view);
        speakImage_view = (ImageView) conference_Info_view.findViewById(R.id.speakImage_view);
        nicknameShow_view = (TextView) conference_Info_view.findViewById(R.id.nicknameShow_view) ;

        speak_show_view = (ImageView) conference_Info_view.findViewById(R.id.icon_speak_show);
        btn_expansion = (Button) conference_Info_view.findViewById(R.id.btn_expansion);
        btn_speaker_setting = (Button) conference_Info_view.findViewById(R.id.btn_speak_setting);

        bottomContainer11 = (RelativeLayout) conference_Info_view.findViewById(R.id.ll_bottom_horizontal);

        animator = ValueAnimator.ofInt(1, 8);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                speak_show_view.getDrawable().setLevel(value);
            }
        });

        //增加滑动监听
        bottomContainerView = (HorizontalScrollView) conference_Info_view.findViewById(R.id.surface_baseline);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bottomContainerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    //更新scroll位置更新订阅流
                    currentScrollX = scrollX;
                    if(scrollX == oldScrollX){
                        updateSubVideo = false;
                    }else{
                        updateSubVideo = true;
                    }
                }
            });
        }

        //增加触控事件
        bottomContainerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP: {
                        //抬起鼠标以后重新计算进行订阅
                        if(updateSubVideo){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Calculate_Update_Sub(currentScrollX);
                                }
                            });
                        }
                        break;
                    }
                }
                return false;
            }
        });

        //bottomContainerScrollView 增加滑动监听事件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bottomContainerScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    //更新scroll位置更新订阅流
                    currentScrollX = scrollY;
                    if(scrollY == oldScrollY){
                        updateSubVideo = false;
                    }else{
                        updateSubVideo = true;
                    }
                }
            });
        }

        //增加触控事件
        bottomContainerScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP: {
                        //抬起鼠标以后重新计算进行订阅
                        if(updateSubVideo){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Calculate_Update_Sub(currentScrollX);
                                }
                            });
                        }
                        break;
                    }
                }
                return false;
            }
        });

        video_off_animator = ValueAnimator.ofInt(1, 8);
        video_off_animator.setDuration(1000);
        video_off_animator.setInterpolator(new LinearInterpolator());
        video_off_animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                speak_show_view.getDrawable().setLevel(value);
            }
        });

        video_on_animator = ValueAnimator.ofInt(1, 8);
        video_on_animator.setDuration(1000);
        video_on_animator.setInterpolator(new LinearInterpolator());
        video_on_animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                speakImage_view.getDrawable().setLevel(value);
            }
        });



        largeSurfacePreview = conference_Info_view.findViewById(R.id.large_preview);
        //动态加载布局
        memberContainer = (RadioLayoutGroup)View.inflate(this, R.layout.layout_icon, null);
        bottomContainerView.addView(memberContainer);
        memberContainer.bringToFront();


        topContainer = (RelativeLayout)findViewById(R.id.ll_top_container);
        btn_switch_camera_layout = (RelativeLayout) findViewById(R.id.call_switch_camera_layout);
        btn_audio_device_layout = (RelativeLayout)findViewById(R.id.call_audio_device_layout);
        btn_audio_device = (ImageView)findViewById(R.id.call_audio_device);
        btn_hangup_layout = (RelativeLayout)findViewById(R.id.call_hangup_layout);
        meeting_duration = (TextView)findViewById(R.id.Meeting_duration);
        meeting_roomId_view = (TextView)findViewById(R.id.Meeting_roomId);

        btn_mic = (Button) findViewById(R.id.btn_call_mic);
        btn_video = (Button) findViewById(R.id.btn_call_video);
        btn_talker_list = (Button) findViewById(R.id.btn_talker_list);
        btn_desktop_share = (Button)findViewById(R.id.btn_desktop_share);
        btn_whiteboard = (Button)findViewById(R.id.btn_whiteboard);

        desktop_share_text = (TextView) findViewById(R.id.text_desktop_share);

        btn_mic.setClickable(false);
        btn_video.setClickable(false);
        btn_talker_list.setClickable(false);
        btn_whiteboard.setClickable(false);
        btn_desktop_share.setClickable(false);
        btn_audio_device.setClickable(false);

        btn_mic_layout = (RelativeLayout)findViewById(R.id.btn_call_mic_layout);
        btn_video_layout = (RelativeLayout)findViewById(R.id.btn_call_video_layout);
        btn_talker_list_layout = (RelativeLayout)findViewById(R.id.btn_talker_list_layout);
        btn_more_layout = (RelativeLayout)findViewById(R.id.btn_more_layout);
        btn_screenShare_layout = (RelativeLayout) findViewById(R.id.btn_desktop_share_layout);

        mic_view= (TextView) findViewById(R.id.text_call_mic);
        video_view =(TextView)findViewById(R.id.text_call_video);
        whiteboard_view = (TextView)findViewById(R.id.text_whiteboard);

        timeHandler = new TimeHandler();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        btn_switch_camera_layout.setOnClickListener(listener);
        btn_audio_device_layout.setOnClickListener(listener);
        btn_hangup_layout.setOnClickListener(listener);
        rootContainer.setOnClickListener(listener);
        btn_speaker_setting.setOnClickListener(listener);
        btn_expansion.setOnClickListener(listener);

        btn_mic_layout.setOnClickListener(listener);
        btn_video_layout .setOnClickListener(listener);
        btn_talker_list_layout.setOnClickListener(listener);
        btn_screenShare_layout.setOnClickListener(listener);
        btn_more_layout.setOnClickListener(listener);
        viewPager.setOnClickListener(listener);

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
        btn_more_layout.setEnabled(true);
        avatarView.setVisibility(GONE);

        if (PreferenceManager.getInstance().isCallAudio()) {
            normalParam.setAudioOff(false);
            localStream.setAudioOff(false);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
            video_off_animator.cancel();
            speak_show_view.setVisibility(VISIBLE);
            speak_show_view.setImageResource(R.drawable.em_call_mic_on);
        } else {
            normalParam.setAudioOff(true);
            localStream.setAudioOff(true);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);

            video_off_animator.cancel();
            speak_show_view.setVisibility(VISIBLE);
            speak_show_view.setImageResource(R.drawable.em_call_mic_off);
        }
        if (PreferenceManager.getInstance().isCallVideo()) {
            normalParam.setVideoOff(false);
            localStream.setVideoOff(false);
            btn_video.setBackgroundResource(R.drawable.em_call_video_on);
        } else {
            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);
            video_off_animator.cancel();
            speak_show_view.setVisibility(VISIBLE);
            if(localStream.isVideoOff()){
                speak_show_view.setImageResource(R.drawable.em_call_mic_off);
            }else{
                speak_show_view.setImageResource(R.drawable.em_call_mic_on);
            }
        }

        btn_mic_layout.setActivated(normalParam.isAudioOff());
        btn_video_layout.setActivated(normalParam.isVideoOff());
        btn_more_layout.setActivated(true);

        meeting_roomId_view.setText(StringUtils.tolongNickName(ConferenceInfo.getInstance().getRoomname(),14));

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

        //监听viewpaper点击事件
        vp_Adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //只有开启摄像头才能全屏
                        if(avatarView.getVisibility() != VISIBLE){
                            setToolsIsHidden();
                        }
                    }
                });
            }
        });
    }


    private void Calculate_Update_Sub(int scrollX){
        updateSubVideo = false;
        int screenwidth = px2dip(getApplicationContext(), viewPager.getWidth());
        subVideoCount  = screenwidth/90 + (screenwidth % 90 == 0 ? 0 :1);
        if(scrollX > 0){
            //划出屏幕的video个数
            int scrollOutVideo = px2dip(getApplicationContext(), scrollX) / 90;
            int member_size = conferenceSession.getConferenceProfiles().size();
            commonSubMumber.clear();
            newSubMumber.clear();
            int updateCount;
            if(member_size - scrollOutVideo >= subVideoCount){
                updateCount = subVideoCount + scrollOutVideo;
            } else {
                updateCount = member_size;
            }
            for(int i = scrollOutVideo;i < updateCount;i++){
                ConferenceMemberInfo info = conferenceSession.getConferenceProfiles().get(i);
                if(subMemberList.contains(info)){
                    commonSubMumber.add(info);
                }else{
                    newSubMumber.add(info);
                }
            }
            //取消订阅视频
            int j;
            for(j = 0; j < subMemberList.size(); j++){
                ConferenceMemberInfo info = subMemberList.get(j);
                if(!commonSubMumber.contains(info)){
                    //更新流取消订阅视频
                    EMConferenceStream stream = ConferenceInfo.getInstance().
                            getConferenceSpeakStream(info.getStreamId());
                    if(stream != null){
                        stream.setVideoOff(true);
                        updateSubscribe(stream,info.getVideoView());
                    }
                    subMemberList.remove(info);
                }
            }

            //增加订阅视频流
            for(j = 0; j < newSubMumber.size(); j++){
                ConferenceMemberInfo info = newSubMumber.get(j);
                //订阅视频
                EMConferenceStream stream = ConferenceInfo.getInstance().
                        getConferenceSpeakStream(info.getStreamId());
                if(stream != null){
                    stream.setVideoOff(false);
                    updateSubscribe(stream,info.getVideoView());
                }
                subMemberList.add(info);
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }


    /**
     * 注册重力横屏感应事件
     */
    private void OrientationInit(){
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mOrientationListener = new OrientationListener(newOrientation -> {
            //判断是否开启自动旋转
            boolean autoRotateOn  = (android.provider.Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) == 1) ;
            if(autoRotateOn){
                //设置屏幕方向
                setRequestedOrientation(newOrientation);
            }
        });
        mSensorManager.registerListener(mOrientationListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
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
                    EMClient.getInstance().conferenceManager().updateRemoteSurfaceView
                            (lastCheckedMemberView.getStreamId(), null);
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
                        if (userProfile != null && userProfile.getStreamId() != null && userProfile.getStreamId().
                                equals(lastCheckedMemberView.getStreamId())) {
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
                        if (adminList.contains(userProfile.getUserId())) {
                            admin_show_view.setVisibility(VISIBLE);
                        } else {
                            admin_show_view.setVisibility(GONE);
                        }
                        if (userProfile != null && userProfile.getStreamId() != null && userProfile.getStreamId().
                                equals(view.getStreamId())) {
                            userProfile.setVideoView(videoView);
                            if (userProfile.getUserId().equals(EMClient.getInstance().getCurrentUser())) {
                                videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                            } else {
                                videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
                            }
                            //切换时候更新小图标状态
                            setLocalAudioVideoIcons(userProfile);
                            break;
                        }
                    }
                }
                if (view.isVideoOff()) {
                    avatarView.setVisibility(VISIBLE);
                    if (view.getUsername().equals(EMClient.getInstance().getCurrentUser())) {
                        currentSelectMemName = EMClient.getInstance().getCurrentUser();
                        nickNameView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(), 6));
                    } else {
                        EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(view.getUsername());
                        currentSelectMemName = memberInfo.memberName;
                        if (memberInfo != null) {
                            nickNameView.setText(StringUtils.tolongNickName(memberInfo.nickName, 6));
                        }
                    }
                    headImageurl = DemoApplication.baseurl;
                    headImageurl = headImageurl + view.getHeadImage();
                    loadImage();
                    setBigImageView(view);
                    largeSurfacePreview.setVisibility(GONE);
                    memberInfo_layout.setVisibility(GONE);
                } else {
                    avatarView.setVisibility(GONE);
                    largeSurfacePreview.setVisibility(VISIBLE);
                    if (isSelf) {
                        EMClient.getInstance().conferenceManager().updateLocalSurfaceView(videoView);
                    } else {
                        EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(view.getStreamId(), videoView);
                    }
                }

                view.getSurfaceViewContainer().setVisibility(GONE);

                //大屏个人信息显示
                memberInfo_layout.setVisibility(GONE);
                if (view.getUsername().equals(EMClient.getInstance().getCurrentUser())) {
                        currentSelectMemName = EMClient.getInstance().getCurrentUser();
                        if (ConferenceInfo.getInstance().getAdmins().contains(EMClient.
                                getInstance().getCurrentUser())) {
                            adminImage_view.setVisibility(VISIBLE);
                        } else {
                            adminImage_view.setVisibility(GONE);
                        }
                        nicknameShow_view.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(), 5));
                    } else {
                        EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(view.getUsername());
                        currentSelectMemName = memberInfo.memberName;
                        String memName = EasyUtils.useridFromJid(memberInfo.memberName);
                        if (ConferenceInfo.getInstance().getAdmins().contains(memName)) {
                            adminImage_view.setVisibility(VISIBLE);
                        } else {
                            adminImage_view.setVisibility(GONE);
                        }
                        if (memberInfo != null) {
                            nicknameShow_view.setText(StringUtils.tolongNickName(memberInfo.nickName, 5));
                        }
                        if (view.isAudioOff()) {
                            video_on_animator.cancel();
                            speakImage_view.setImageResource(R.drawable.em_speak_off);
                        } else {
                            video_on_animator.cancel();
                            speakImage_view.setImageResource(R.drawable.em_speak_on);
                        }
                    }


                    if (lastCheckedMemberView.isVideoOff()) {
                        lastCheckedMemberView.getSurfaceViewContainer().setVisibility(GONE);
                    } else {
                        lastCheckedMemberView.getSurfaceViewContainer().setVisibility(VISIBLE);
                        if (lastIsSelf) {
                            EMClient.getInstance().conferenceManager().updateLocalSurfaceView(lastSurfaceView);
                        } else {
                            EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(lastCheckedMemberView.
                                    getStreamId(), lastSurfaceView);
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
        if(!info.isWhiteboard()){
            memberView.setId(getViewIdByStreamId(info.getStreamId()));
            EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(info.getUserId());
            memberView.setUsername(info.getUserId(),true);
            memberView.setNickname(memberInfo.nickName);
            memberView.setHeadImage(memberInfo.extension);
            memberView.setStreamId(info.getStreamId());
            memberView.setAudioOff(info.isAudioOff());
            memberView.setVideoOff(info.isVideoOff());
            memberView.setDesktop(info.isDesktop());
        }else{
            memberView.setId(getViewIdByStreamId(info.getStreamId()));
            memberView.setWhiteboard(true);
            memberView.setWhiteboardRoomId(info.getStreamId());
            memberView.setWhiteboardPassword(info.getWhiteboardPwd());
            boolean flag = info.getUserId().equals(EMClient.getInstance()
                           .getCurrentUser())?true:false;
            memberView.setIswhiteboardCreator(flag);
        }

        //角色为观众第一个主播进来的时候 显示在大屏
        if (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience
                && conferenceSession.getConferenceProfiles().size() == 1) {
            memberView.setChecked(true);
            memberView.setOnCheckedChangeListener(mOnCheckedChangeListener);

            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                                  ViewGroup.LayoutParams.WRAP_CONTENT);
            memberContainer.addView(memberView, params);
            imMembers.add(info.getStreamId());
            if(!memberView.isWhiteboard()) {
                EMCallSurfaceView videoView = info.getVideoView();
                if (info.isAudioOff()) {
                    video_off_animator.cancel();
                    speak_show_view.setVisibility(VISIBLE);
                    speak_show_view.setImageResource(R.drawable.em_call_mic_off);
                } else {
                    video_off_animator.cancel();
                    speak_show_view.setVisibility(VISIBLE);
                    speak_show_view.setImageResource(R.drawable.em_call_mic_on);
                }
                if (memberView.isVideoOff()) {
                    setBigImageView(memberView);
                    avatarView.setVisibility(VISIBLE);
                    EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(memberView.getUsername());
                    if(memberInfo != null){
                        nickNameView.setText(StringUtils.tolongNickName(memberInfo.nickName,6));
                    }
                    headImageurl = DemoApplication.baseurl;
                    headImageurl = headImageurl + memberView.getHeadImage();
                    loadImage();
                    largeSurfacePreview.setVisibility(GONE);
                } else {
                    speak_show_view.setVisibility(GONE);
                    avatarView.setVisibility(GONE);
                    largeSurfacePreview.setVisibility(VISIBLE);
                }
                lastSelectedId = getViewIdByStreamId(info.getStreamId());

                videoView.setZOrderOnTop(false);
                videoView.setZOrderMediaOverlay(false);
                videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
                EMClient.getInstance().conferenceManager().setLocalSurfaceView(videoView);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                largeSurfacePreview.addView(videoView, lp);

                memberView.getAvatarImageView().setVisibility(VISIBLE);

                //打开小窗口的 麦克风 摄像头小图标
                setLocalAudioVideoIcons(info);

                //显示下边的x小窗口列表
                bottomContainer11.setVisibility(VISIBLE);
                bottomContainer.setVisibility(VISIBLE);
                bottomContainerView.setVisibility(VISIBLE);
            }
        } else {
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                      ViewGroup.LayoutParams.MATCH_PARENT);
            memberContainer.addView(memberView, params);
            imMembers.add(info.getStreamId());
            if(!memberView.isWhiteboard()){
                memberView.setOnCheckedChangeListener(mOnCheckedChangeListener);
                memberView.setChecked(false);

                EMCallSurfaceView videoView = info.getVideoView();
                videoView.setZOrderMediaOverlay(true);
                memberView.getSurfaceViewContainer().addView(videoView);
                EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(info.getStreamId(), videoView);
            }
        }

        //第二个主播进入
        if (ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.
                EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() == 2) {
            bottomContainer11.setVisibility(VISIBLE);
            bottomContainer.setVisibility(VISIBLE);
            bottomContainerView.setVisibility(VISIBLE);
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
        if(ConferenceInfo.getInstance().getWhiteboardRoomInfo() != null){
            if(userId.equals(ConferenceInfo.getInstance().getWhiteboardRoomInfo().getRoomName())){
                memberContainer.removeView(findViewById(viewId));
                ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "destoryWhiteboard",ConferenceInfo.getInstance().getWhiteboardRoomInfo().getRoomName());
                ConferenceInfo.getInstance().setWhiteboardRoomInfo(null);
                return;
            }
        }
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
//                    speak_show_view.setVisibility(View.GONE);
//                    speak_show_view.setVisibility(View.VISIBLE);
//                    speak_show_view.setImageResource(R.drawable.em_call_mic_off);
                }
            }
        }
        memberContainer.removeView(findViewById(viewId));

        //当前角色为观众 房间内没有主播
       if((ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() == 0)) {
            setBtn_micAndBtn_vedio(EMConferenceManager.EMConferenceRole.Audience);
            setRequestBtnState(STATE_AUDIENCE);
        }

        if ((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() == 1)){
            bottomContainer11.setVisibility(GONE);
            bottomContainer.setVisibility(VISIBLE);
            bottomContainerView.setVisibility(GONE);
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
        if(memberInfo != null){
            memberView.setNickname(memberInfo.nickName);
        }else{
            if(EMClient.getInstance().getCurrentUser().equals(info.getUserId())){
                memberView.setNickname(PreferenceManager.getInstance().getCurrentUserNick());
            }
        }
        if (memberView.isChecked()) {
            if (info.isVideoOff()) {
                avatarView.setVisibility(VISIBLE);
                headImageurl = DemoApplication.baseurl;
                headImageurl = headImageurl + memberView.getHeadImage();
                loadImage();
                largeSurfacePreview.setVisibility(GONE);
                memberView.getAvatarImageView().setVisibility(VISIBLE);
                if(viewId == lastSelectedId){
                    memberInfo_layout.setVisibility(GONE);
                    bottomContainer.setVisibility(VISIBLE);
                    topContainer.setVisibility(VISIBLE);
                }
                if(memberInfo != null){
                    nickNameView.setText(StringUtils.tolongNickName(memberInfo.nickName,6));
                }else{
                    if(EMClient.getInstance().getCurrentUser().equals(info.getUserId())){
                        nickNameView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6));
                    }
                }
            } else {
                if(viewId == lastSelectedId){
                    if(bottomContainer.getVisibility() == GONE){
                        memberInfo_layout.setVisibility(VISIBLE);
                    }
                }
                avatarView.setVisibility(GONE);
                largeSurfacePreview.setVisibility(VISIBLE);
                videoView.setZOrderMediaOverlay(true);
                videoView.setZOrderOnTop(false);
                memberView.getAvatarImageView().setVisibility(VISIBLE);
            }
            //更新麦克风 摄像头小图标
            setLocalAudioVideoIcons(info);
        } else {
            if (info.isVideoOff()) {
                videoView.setZOrderMediaOverlay(true);
                memberView.getSurfaceViewContainer().setVisibility(GONE);
                memberView.getAvatarImageView().setVisibility(VISIBLE);
            } else {
                videoView.setZOrderMediaOverlay(true);
                memberView.getAvatarImageView().setVisibility(GONE);
                memberView.getSurfaceViewContainer().setVisibility(VISIBLE);
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
            speakImage_view.setVisibility(VISIBLE);
            speakImage_view.setBackgroundResource(R.drawable.em_call_mic_off);
            mic_view= (TextView) findViewById(R.id.text_call_mic);
            video_view =(TextView)findViewById(R.id.text_call_video);
            mic_view.setText("解除静音");
            video_view.setText("打开视频");

            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);

            btn_screenShare_layout.setClickable(false);
            btn_screenShare_layout.setVisibility(GONE);
            btn_mic_layout.setActivated(false);
            btn_video_layout.setActivated(false);

            avatarView.setVisibility(VISIBLE);
            nickNameView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6));
            headImageurl = DemoApplication.baseurl;
            headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
            btn_switch_camera_layout.setVisibility(GONE);
            loadImage();
            largeSurfacePreview.setVisibility(GONE);

            btn_mic_layout.setEnabled(false);
            btn_video_layout.setEnabled(false);
        } else {
            btn_mic_layout.setEnabled(true);
            btn_video_layout.setEnabled(true);

            btn_screenShare_layout.setClickable(true);
            btn_screenShare_layout.setVisibility(VISIBLE);

            btn_switch_camera_layout.setVisibility(VISIBLE);

            nicknameShow_view.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),5));

            if (PreferenceManager.getInstance().isCallAudio()) {
                normalParam.setAudioOff(false);
                localStream.setAudioOff(false);
                btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
                video_off_animator.cancel();
                speak_show_view.setVisibility(VISIBLE);
                speak_show_view.setImageResource(R.drawable.em_call_mic_on);
                EMClient.getInstance().conferenceManager().openVoiceTransfer();
                mic_view.setText("静音");
                video_on_animator.cancel();
                speakImage_view.setImageResource(R.drawable.em_call_mic_on);
            } else {
                normalParam.setAudioOff(true);
                localStream.setAudioOff(true);
                btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
                video_off_animator.cancel();
                speak_show_view.setVisibility(VISIBLE);
                speak_show_view.setImageResource(R.drawable.em_call_mic_off);
                EMClient.getInstance().conferenceManager().closeVoiceTransfer();
                mic_view.setText("解除静音");
                video_on_animator.cancel();
                speakImage_view.setImageResource(R.drawable.em_call_mic_off);
            }
            if (PreferenceManager.getInstance().isCallVideo()) {
                normalParam.setVideoOff(false);
                localStream.setVideoOff(false);
                btn_video.setBackgroundResource(R.drawable.em_call_video_on);
                avatarView.setVisibility(GONE);
                video_view.setText("关闭视频");
                EMClient.getInstance().conferenceManager().openVideoTransfer();
                speak_show_view.setVisibility(GONE);
                btn_switch_camera_layout.setVisibility(VISIBLE);
            } else {
                normalParam.setVideoOff(true);
                localStream.setVideoOff(true);
                btn_video.setBackgroundResource(R.drawable.em_call_video_off);
                avatarView.setVisibility(VISIBLE);
                video_view.setText("打开视频");
                EMClient.getInstance().conferenceManager().closeVideoTransfer();
                nickNameView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6));
                headImageurl = DemoApplication.baseurl;
                headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                btn_switch_camera_layout.setVisibility(GONE);
                loadImage();
                largeSurfacePreview.setVisibility(GONE);
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
                    removeConferenceView(removedUserProfile.getStreamId());
                    localuserProfile = null;
                    if (userProfiles.size() == 0) {
                        avatarView.setVisibility(VISIBLE);
                        nickNameView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6));
                        headImageurl = DemoApplication.baseurl;
                        headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                        loadImage();
                        largeSurfacePreview.setVisibility(GONE);
                    } else {
                        avatarView.setVisibility(GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //显示主播
        if (!ConferenceInfo.Initflag) {
            if (streamList.size() > 0) {
                avatarView.setVisibility(GONE);
                for (int i = 0; i < streamList.size(); i++) {
                    onStreamAdded(streamList.get(i));
                }
            }

            addWhiteBoardWindow();
            ConferenceInfo.Initflag = true;
        }
    }

    /**
     * 大屏上麦克风 摄像头小图标状态切换
     */
    private void setLocalAudioVideoIcons(ConferenceMemberInfo info) {
        if (info.isAudioOff()) {
            video_on_animator.cancel();
            speakImage_view.setVisibility(VISIBLE);
            speakImage_view.setImageResource(R.drawable.em_call_mic_off);
        } else {
            video_on_animator.cancel();
            speakImage_view.setVisibility(VISIBLE);
            speakImage_view.setImageResource(R.drawable.em_call_mic_on);
        }

        if(info.isVideoOff()){
            if (info.isAudioOff()) {
                video_off_animator.cancel();
                speak_show_view.setVisibility(VISIBLE);
                speak_show_view.setImageResource(R.drawable.em_call_mic_off);
            } else {
                video_off_animator.cancel();
                speak_show_view.setVisibility(VISIBLE);
                speak_show_view.setImageResource(R.drawable.em_call_mic_on);
            }
        }else{
            video_off_animator.cancel();
            speak_show_view.setVisibility(GONE);
        }

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.call_audio_device_layout:
                    voiceDeviceSwitch();
                    break;
                case R.id.btn_call_mic_layout:
                    voiceSwitch();
                    break;
                case R.id.btn_call_video_layout:
                    videoSwitch();
                    break;
                case R.id.call_switch_camera_layout:
                    changeCamera();
                    break;
                case R.id.call_hangup_layout:
                    hangup();
                    break;
                case R.id.btn_talker_list_layout:
                    opentalkerlist();
                    break;
                case R.id.btn_desktop_share_layout:
                    desktopShare_WhiteBoard();
                    break;
                case R.id.btn_expansion:
                    setbtnexpansion();
                    break;
                case R.id.btn_more_layout:
                     show_more_btn(view);
;                    break;
                case R.id.root_layout:
                    if(avatarView.getVisibility() != VISIBLE){
                        setToolsIsHidden();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 点击屏幕上下工具操作栏是否隐藏
     */
    public void setToolsIsHidden(){
        if(bottomContainer.getVisibility() == VISIBLE) {
            if(conferenceSession.getConferenceProfiles() != null){
                if ((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 1) ||
                        (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 0)) {
                    RelativeLayout.LayoutParams params;
                    if(portrait){
                        bottomContainer11.setVisibility(VISIBLE);
                        bottomContainerView.setVisibility(VISIBLE);
                    }
                }
                bottomContainer.setVisibility(GONE);
                topContainer.setVisibility(GONE);
                memberInfo_layout.setVisibility(VISIBLE);
            }
        } else {
            if(conferenceSession.getConferenceProfiles() != null){
                if ((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 1) ||
                        (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 0) ) {
                    if(portrait){
                        bottomContainer11.setVisibility(VISIBLE);
                        bottomContainerView.setVisibility(VISIBLE);
                    }
                }
            }
            bottomContainer.setVisibility(VISIBLE);
            topContainer.setVisibility(VISIBLE);
            memberInfo_layout.setVisibility(GONE);
        }
    }


    /**
     * 显示更多按钮
     */
    private void show_more_btn(View view){
        // 一个自定义的布局，作为显示的内容
        more_btn_view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.activity_more_pop_window, null);

        popupWindow = new PopupWindow(more_btn_view);
        // 设置弹窗外可点击
        popupWindow.setOutsideTouchable(true);
        popupWindow.setWidth(dip2px(getApplicationContext(),220));
        popupWindow.setHeight(dip2px(getApplicationContext(),80));
        popupWindow.setContentView(more_btn_view);

        more_btn_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = more_btn_view.getMeasuredHeight();
        int popupWidth = more_btn_view.getMeasuredWidth();
        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //在控件上方显示向上移动y轴是负数
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0]-popupWindow.getWidth() + dip2px(getApplicationContext(),30), location[1] - popupHeight - dip2px(getApplicationContext(),25));

        // 设置按钮的点击事件
        btn_wheat_layout = more_btn_view.findViewById(R.id.more_wheat_layout);
        btn_setting_layout = more_btn_view.findViewById(R.id.more_setting_layout);
        btn_invite_layout  = more_btn_view.findViewById(R.id.more_invite_layout);
        ImageView  wheat_view = (ImageView) more_btn_view.findViewById(R.id.btn_more_wheat);
        TextView wheat_text_view = more_btn_view.findViewById(R.id.more_wheat_view);
        if(conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience){
            wheat_view.setBackgroundResource(R.drawable.em_call_request_connect);
            wheat_text_view.setText("上麦");
        }else{
            wheat_view.setBackgroundResource(R.drawable.em_call_request_disconnect);
            wheat_text_view.setText("下麦");
        }

        btn_wheat_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                requesteven_wheat();
                                popupWindow.dismiss();
                            }
                        });
                    }
                });
            }
        });

        btn_setting_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mId = Math.abs(new Random(System.currentTimeMillis()).nextInt());
                        Intent intent = new Intent(ConferenceActivity.this, RoomSettingActivity.class);
                        intent.putExtra(KEY_ID, mId);
                        startActivity(intent);
                        popupWindow.dismiss();
                    }
                });
            }
        });

        btn_invite_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {

                            ClipboardManager mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            String line = PreferenceManager.getInstance().getCurrentUserNick();
                            line += " 邀请您参加视频会议";
                            line += "\n";
                            line += "会议名称：" + ConferenceInfo.getInstance().getRoomname();
                            line += "\n";
                            line += "\n";
                            line += "点击以下链接直接加入会议：";
                            line += "\n";
                            String url = DemoApplication.meeting_share_baseurl;
                            url += "roomName=";
                            url += URLEncoder.encode(ConferenceInfo.getInstance().getRoomname(), "utf-8");
                            url += "&invitees=";
                            url += URLEncoder.encode(PreferenceManager.getInstance().getCurrentUserNick(), "utf-8");
                            line += url;
                            line += "\n";
                            line += "\n";
                            line += "app下载地址：";
                            line += "http://www.easemob.com/download/rtc";
                            ClipData mClipData = ClipData.newPlainText("Label", line);
                            mClipboardManager.setPrimaryClip(mClipData);
                            popupWindow.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, mClipData.getItemAt(0).getText().toString());
                            intent.setType("text/plain");
                            //设置分享列表的标题，并且每次都显示分享列表
                            startActivity(Intent.createChooser(intent, "分享到"));
                        }catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void setbtnexpansion(){
        if(expansionflag){
            topContainer.setVisibility(GONE);
            btn_expansion.setBackgroundResource(R.drawable.ic_comments_down);
        }else {
            topContainer.setVisibility(VISIBLE);
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
        } else if (CameraResolution.equals("(Auto)480P")) {
            normalParam.setVideoWidth(720);
            normalParam.setVideoHeight(480);
        } else if (CameraResolution.equals("720P")) {
            normalParam.setVideoWidth(1280);
            normalParam.setVideoHeight(720);
        }
//        normalParam.setVideoWidth(1920);
//        normalParam.setVideoHeight(1080);
        EMClient.getInstance().callManager().getCallOptions().setClarityFirst(true);

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
                        localViewContainer.setUsername(EMClient.getInstance().getCurrentUser(),true);
                        localViewContainer.setVideoOff(localStream.isVideoOff());
                        localViewContainer.setAudioOff(localStream.isAudioOff());

                        localViewContainer.setHeadImage(PreferenceManager.getInstance().getCurrentUserAvatar());
                        localViewContainer.setOnCheckedChangeListener(mOnCheckedChangeListener);
                        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        memberContainer.addView(localViewContainer, params);

                        if (localViewContainer.isVideoOff()) {
                            setBigImageView(localViewContainer);
                            avatarView.setVisibility(VISIBLE);
                            nickNameView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6));
                            headImageurl = DemoApplication.baseurl;
                            headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                            btn_switch_camera_layout.setVisibility(GONE);
                            loadImage();
                            largeSurfacePreview.setVisibility(GONE);
                        } else {
                            btn_switch_camera_layout.setVisibility(VISIBLE);
                            avatarView.setVisibility(GONE);
                            largeSurfacePreview.setVisibility(VISIBLE);
                        }
                        lastSelectedId = selfRadioButtonId;
                        avatarView.setVisibility(GONE);
                        EMCallSurfaceView localView = new EMCallSurfaceView(ConferenceActivity.this);
                        localView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                        localView.setZOrderOnTop(false);
                        localView.setZOrderMediaOverlay(false);
                        localuserProfile = new ConferenceMemberInfo();
                        localuserProfile.setUserId(EMClient.getInstance().getCurrentUser());
                        localuserProfile.setAudioOff(localStream.isAudioOff());
                        localuserProfile.setVideoOff(localStream.isVideoOff());
                        localuserProfile.setVideoView(localView);
                        localuserProfile.setStreamId(localStream.getStreamId());
                        localViewContainer.setNickname(PreferenceManager.getInstance().getCurrentUserNick());


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
                        //增加当前小窗口订阅流列表
                        if(subMemberList.size() <=  subVideoCount){
                            subMemberList.add(localuserProfile);
                        }

                        EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView);
                        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params1.addRule(RelativeLayout.CENTER_IN_PARENT);
                        largeSurfacePreview.addView(localView, params1);
                        localViewContainer.getAvatarImageView().setVisibility(VISIBLE);
                        //打开小窗口的 麦克风 摄像头小图标
                        setLocalAudioVideoIcons(localuserProfile);

                        if(ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin){
                            admin_show_view.setVisibility(VISIBLE);
                        }else {
                            admin_show_view.setVisibility(GONE);
                        }

                        localViewContainer.setStreamId(localStream.getStreamId());
                        localuserProfile.setStreamId(localStream.getStreamId());
                        imMembers.add(localStream.getStreamId());
                        if (!PreferenceManager.getInstance().isCallVideo()) {
                            avatarView.setVisibility(VISIBLE);
                            nickNameView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6));
                            headImageurl = DemoApplication.baseurl;
                            headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                            loadImage();
                            largeSurfacePreview.setVisibility(GONE);
                        }else{
                            memberInfo_layout.setVisibility(GONE);
                            nicknameShow_view.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(), 5));
                        }
                        if (!ConferenceInfo.Initflag) {
                            if (streamList.size() > 0) {
                                for (int i = 0; i < streamList.size(); i++) {
                                    onStreamAdded(streamList.get(i));
                                }
                            }
                            //增加白板小窗口
                            addWhiteBoardWindow();
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
                if (EMClient.getInstance().conferenceManager().isCreator() && PreferenceManager.getInstance().isPushCDN()) {
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
        speakSwitch(audio_openSpeaker);
        audio_openSpeaker = !audio_openSpeaker;
    }

    /**
     *切换音频设备
     */
    public void speakSwitch(boolean open) {
        if (open) {
            openSpeaker();
            btn_audio_device.setBackgroundResource(R.drawable.em_call_speaker_on);
         }else{
            closeSpeaker();
            btn_audio_device.setBackgroundResource(R.drawable.call_audio_device);
         }
    }

    /**
     * 语音开关
     */
    private void voiceSwitch() {
        EMLog.i(TAG, "voiceSwitch: State:" + normalParam.isAudioOff());
        if (normalParam.isAudioOff()) {
            normalParam.setAudioOff(false);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_on);
            localStream.setAudioOff(false);
            EMClient.getInstance().conferenceManager().openVoiceTransfer();
            mic_view.setText("静音");
        } else {
            normalParam.setAudioOff(true);
            localStream.setAudioOff(true);
            btn_mic.setBackgroundResource(R.drawable.em_call_mic_off);
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
            mic_view.setText("解除静音");
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
            btn_switch_camera_layout.setVisibility(VISIBLE);
            EMClient.getInstance().conferenceManager().openVideoTransfer();
        } else {
            normalParam.setVideoOff(true);
            localStream.setVideoOff(true);
            btn_video.setBackgroundResource(R.drawable.em_call_video_off);
            video_view.setText("打开视频");
            btn_switch_camera_layout.setVisibility(GONE);
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

//        EMCDNCanvas canvas = new EMCDNCanvas(ConferenceInfo.CanvasWidth, ConferenceInfo.CanvasHeight, 0,30,900,"H264");
//
//        String url = "rtmp://livepush.easemob.com/meeting/lijian3?auth_key=1595955952-0-0-ebfbc194992a915a9636968b983dfbea";
//        EMLiveConfig liveConfig = new EMLiveConfig(url, canvas);
//        EMClient.getInstance().conferenceManager().addLiveStream(liveConfig, new EMValueCallBack<String>() {
//           @Override
//           public void onSuccess(String value) {
//               EMLog.i(TAG, "addLiveStream  onSuccess");
//           }
//
//           @Override
//           public void onError(int error, String errorMsg) {
//               EMLog.i(TAG, "addLiveStream  error:"  + errorMsg);
//           }
//       });
    }

    /**
     * 更新cdn推流画布
     */
    private void updatelayout(){
        Map<String, String> livecfgs = EMClient.getInstance().conferenceManager().getLiveCfgs();
        String liveId = null;
        if (livecfgs.size() <= 0) {
           return;
        }
        Iterator<String> iter = livecfgs.keySet().iterator();
        while(iter.hasNext()) {
            liveId = iter.next();
            break;
        }
        int streamcount = streamList.size();
        if(ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience){
            streamcount++;
            if(btn_screenShare_layout.isActivated()){
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
            if(btn_screenShare_layout.isActivated()){
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
        EMClient.getInstance().conferenceManager().updateLiveLayout(liveId,regionsList,new EMValueCallBack<String>() {
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
     * 共享桌面或开启白板
     */
    private void desktopShare_WhiteBoard(){
        if(Desktop_share_Dialog.getDesktop_shareType() == -1){
            Desktop_share_Dialog  dialog =  Desktop_share_Dialog.
                        getNewInstance(EMClient.getInstance().getCurrentUser());
            dialog.setAppCompatActivity(this);
            dialog .show(this.getSupportFragmentManager(), "Desktop_share_Dialog");
        }else if(Desktop_share_Dialog.getDesktop_shareType() == 0){
            screenShare();
            Desktop_share_Dialog.setDesktop_shareType(-1);
        }else if(Desktop_share_Dialog.getDesktop_shareType() == 1){
            Desktop_share_Dialog  dialog =  Desktop_share_Dialog.
                    getNewInstance(EMClient.getInstance().getCurrentUser());
            dialog.setAppCompatActivity(this);
            dialog .show(this.getSupportFragmentManager(), "Desktop_share_Dialog");
            Desktop_share_Dialog.setDesktop_shareType(-1);
        }
    }

    /**
     * 共享桌面
     */
    public void screenShare(){
        if (btn_screenShare_layout.isActivated()) {
            btn_screenShare_layout.setActivated(false);
            btn_desktop_share.setBackgroundResource(R.drawable.call_screenshare);
            unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP));

            //移除共享桌面页面
//            listView.remove(0);
//            vp_Adapter.notifyDataSetChanged();
//            viewPager.setCurrentItem(1);

            viewPager.setCurrentItem(1);
            listView.remove(desktop_share_view);
            vp_Adapter.finishUpdate(viewPager);
            vp_Adapter.notifyDataSetChanged();// 刷新
            viewPager.setNoScroll(true);
            desktop_share_text.setText("共享桌面");
            Desktop_share_Dialog.setDesktop_shareType(-1);
        } else {
            btn_screenShare_layout.setActivated(true);
            desktop_share_text.setText("取消共享");
            btn_desktop_share.setBackgroundResource(R.drawable.call_screenshare_cancel);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                desktopParam.setShareView(null);
            } else {
                desktopParam.setShareView(activity.getWindow().getDecorView());
            }
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            EMClient.getInstance().callManager().getCallOptions().setVideoResolution(screenWidth, screenHeight);
            desktopParam.setAudioOff(false);
            EMClient.getInstance().conferenceManager().publish(desktopParam, new EMValueCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    conference.setPubStreamId(value, EMConferenceStream.StreamType.DESKTOP);
                    ScreenCaptureManager.State state = ScreenCaptureManager.getInstance().state;
                    startScreenCapture();

                    //增加共享桌面页面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Desktop_share_Dialog.setDesktop_shareType(0);
                            desktop_share_avatar.setVisibility(VISIBLE);
                            desktop_share_surfaceview.setVisibility(GONE);
//                            listView.add(desktop_share_view);
//                            vp_Adapter.notifyDataSetChanged();
//                            viewPager.setCurrentItem(1);
//                            viewPager.setNoScroll(false);
                            listView.add(desktop_share_view);
                            Collections.reverse(listView);
                            vp_Adapter.destroyItem(viewPager,0,null);
                            vp_Adapter.finishUpdate(viewPager);
                            vp_Adapter.setListView(listView);
                            viewPager.setAdapter(vp_Adapter);
                            vp_Adapter.notifyDataSetChanged();// 刷新
                            viewPager.setCurrentItem(0);
                            viewPager.setNoScroll(false);


                            desktop_nickName_view.setText((StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6)));
                            headImageurl = DemoApplication.baseurl;
                            headImageurl = headImageurl + PreferenceManager.getInstance().getCurrentUserAvatar();
                            desktopHeadImage = true;
                            loadImage();

                        }
                    });
                }
                @Override
                public void onError(int error, String errorMsg) {
                    Desktop_share_Dialog.setDesktop_shareType(-1);
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
            EMLog.i(TAG, "requesteven_wheat  request_tobe_audience");
            if (conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience) { // 发送消息，申请上麦
                if (adminList != null) {
                    if (adminList.size() > 0) {
                        if(adminList.contains(EMClient.getInstance().getCurrentUser())){
                            getAdminsToBeSpeaker();
                        }else{
                            EMConferenceMember adminMemberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(adminList.get(0));
                            if (adminMemberInfo != null) {
                                EMClient.getInstance().conferenceManager().applyTobeSpeaker(adminMemberInfo.memberId);
                            } else {
                                Toast.makeText(getApplicationContext(), "本房间暂无主播进入，不允许上麦!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "本房间还未指定主持人，不允许上麦!", Toast.LENGTH_SHORT).show();
                }
            } else { // 已经是主播，直接推流
                //publish();
                setRequestBtnState(STATE_TALKER);
            }
        } else if (btnState == STATE_TALKER) { // 当前按钮状态是主播，需要下麦
            EMLog.i(TAG, "requesteven_wheat  request_tobe_talker");
            OffWheatDialog  dialog = (OffWheatDialog)this.getSupportFragmentManager().findFragmentByTag("OffWheatDialog");
            if(dialog == null) {
                dialog =  OffWheatDialog.getNewInstance(EMClient.getInstance().getCurrentUser());
            }
            if(dialog.isAdded()) {
                return;
            }
            dialog.setAppCompatActivity(this);
            dialog .show(this.getSupportFragmentManager(), "OffWheatDialog");
        }
    }

    public void set_off_wheat(){
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

    public void whiteboard_option(String roomName,String roomPass){
        EMClient.getInstance().conferenceManager().createWhiteboardRoom(EMClient.getInstance().getCurrentUser()
                , EMClient.getInstance().getAccessToken(), roomName, roomPass,true,
                  new EMValueCallBack<EMWhiteboard>() {
                    @Override
                    public void onSuccess(EMWhiteboard value) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EMLog.i(TAG, "create and join"+
                                        "WhiteboardRoom success, roomId: " + value.getRoomId());
                                mId = Math.abs(new Random(System.currentTimeMillis()).nextInt());
                                Intent intent = new Intent(ConferenceActivity.
                                                            this, WhiteBoardTbsActivity.class);
                                EMLog.e(TAG,"WhiteBoardTbsActivity 111 go");
                                Bundle bundle = new Bundle();
                                bundle.putString("roomId",value.getRoomId());
                                bundle.putString("roomUrl",value.getRoomUrl());
                                if(value.getRoomUrl().contains("isCreater=true")){
                                    EMLog.i(TAG, "Current user isCreater, roomId: " + value.getRoomId());
                                    ConferenceInfo.getInstance().setWhiteboard(value);
                                    ConferenceInfo.whiteboardCreator = true;
                                }else{
                                    ConferenceInfo.getInstance().setWhiteboard(null);
                                    ConferenceInfo.whiteboardCreator = false;
                                }
                                if(ConferenceInfo.whiteboardCreator){
                                    bundle.putBoolean("creator",true);
                                }else{
                                    bundle.putBoolean("creator",false);
                                }
                                bundle.putInt(KEY_ID, mId);
                                intent.putExtras(bundle);
                                startActivity(intent);

                                //创建者广播会议属性
                                if(ConferenceInfo.whiteboardCreator) {
                                    //设置白板会议属性
                                    try {
                                        JSONObject object = new JSONObject();
                                        object.put("creator", EMClient.getInstance().getCurrentUser());
                                        object.put("roomName", ConferenceInfo.getInstance().getRoomname());
                                        object.put("roomPswd", ConferenceInfo.getInstance().getPassword());
                                        long time = System.currentTimeMillis();
                                        object.put("timestamp", time / 1000);

                                        EMClient.getInstance().conferenceManager().setConferenceAttribute(WHITE_BOARD,
                                                object.toString(), new EMValueCallBack<Void>() {
                                                    @Override
                                                    public void onSuccess(Void value) {
                                                        EMLog.i(TAG, "setConferenceAttribute WHITE_BOARD success");

                                                    }

                                                    @Override
                                                    public void onError(int error, String errorMsg) {
                                                        EMLog.i(TAG, "setConferenceAttribute WHITE_BOARD failed"
                                                                + error + "" + errorMsg);
                                                    }
                                                });
                                    } catch (Exception e) {
                                        e.getStackTrace();
                                    }
                                }
                                if(conferenceSession.getConferenceMemberByStreamId(ConferenceInfo
                                            .getInstance().getRoomname()) == null){
                                        //增加白板小窗口
                                        ConferenceInfo.getInstance().setWhiteboard(value);
                                        ConferenceMemberInfo userProfile = new ConferenceMemberInfo();
                                        userProfile.setStreamId(ConferenceInfo.getInstance().getRoomname());
                                        userProfile.setWhiteboardPwd(ConferenceInfo.getInstance().getPassword());
                                        userProfile.setUserId(EMClient.getInstance().getCurrentUser());
                                        userProfile.setWhiteboard(true);
                                        conferenceSession.getConferenceProfiles().add(userProfile);
                                        WhiteBoardRoomInfo roomInfo = new WhiteBoardRoomInfo();
                                        roomInfo.setCreator(EMClient.getInstance().getCurrentUser());
                                        roomInfo.setRoomName(ConferenceInfo.getInstance().getRoomname());
                                        roomInfo.setRoomPswd(ConferenceInfo.getInstance().getPassword());
                                        ConferenceInfo.getInstance().setWhiteboardRoomInfo(roomInfo);
                                    }

                            }
                        });
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EMLog.i(TAG, "createWhiteboardRoom failed, error: " + error + " - " + errorMsg);
                                Toast.makeText(getApplicationContext(), "创建白板  " + ConferenceInfo.getInstance().getRoomname() + "失败: " + errorMsg + " !",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
    }

    public void add_whiteboard_view(String roomName,String roomPass){
        EMClient.getInstance().conferenceManager().createWhiteboardRoom(EMClient.getInstance().getCurrentUser()
                , EMClient.getInstance().getAccessToken(), roomName, roomPass,true,
                new EMValueCallBack<EMWhiteboard>() {
                    @Override
                    public void onSuccess(EMWhiteboard value) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EMLog.i(TAG, "create and join"+
                                        "WhiteboardRoom success, roomId: " + value.getRoomId());

                                //加入白板view
                                roomUrl = value.getRoomUrl();
                                mViewParent = (ViewGroup)whiteborad_view.
                                        findViewById(R.id.whiteboard_view_layout);
                                mTestHandler.sendEmptyMessage(MSG_INIT_UI);
//                                listView.add(whiteborad_view);
//                                vp_Adapter.notifyDataSetChanged();
//                                viewPager.setCurrentItem(1);
                                listView.add(whiteborad_view);
                                Collections.reverse(listView);
                                vp_Adapter.destroyItem(viewPager,0,null);
                                vp_Adapter.finishUpdate(viewPager);
                                vp_Adapter.setListView(listView);
                                viewPager.setAdapter(vp_Adapter);
                                vp_Adapter.notifyDataSetChanged();// 刷新
                                viewPager.setCurrentItem(0);
                                viewPager.setNoScroll(false);


                                if(value.getRoomUrl().contains("isCreater=true")){
                                    EMLog.i(TAG, "Current user isCreater, roomId: " + value.getRoomId());
                                    ConferenceInfo.getInstance().setWhiteboard(value);
                                    ConferenceInfo.whiteboardCreator = true;
                                }else{
                                    ConferenceInfo.getInstance().setWhiteboard(null);
                                    ConferenceInfo.whiteboardCreator = false;
                                }
                                if(conferenceSession.getConferenceMemberByStreamId(ConferenceInfo
                                        .getInstance().getRoomname()) == null){
                                    //增加白板小窗口
                                    ConferenceInfo.getInstance().setWhiteboard(value);
                                    ConferenceMemberInfo userProfile = new ConferenceMemberInfo();
                                    userProfile.setStreamId(ConferenceInfo.getInstance().getRoomname());
                                    userProfile.setWhiteboardPwd(ConferenceInfo.getInstance().getPassword());
                                    userProfile.setUserId(EMClient.getInstance().getCurrentUser());
                                    userProfile.setWhiteboard(true);
                                    conferenceSession.getConferenceProfiles().add(userProfile);

                                    WhiteBoardRoomInfo roomInfo = new WhiteBoardRoomInfo();
                                    roomInfo.setCreator(EMClient.getInstance().getCurrentUser());
                                    roomInfo.setRoomName(ConferenceInfo.getInstance().getRoomname());
                                    roomInfo.setRoomPswd(ConferenceInfo.getInstance().getPassword());
                                    ConferenceInfo.getInstance().setWhiteboardRoomInfo(roomInfo);
                                }

                            }
                        });
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EMLog.i(TAG, "createWhiteboardRoom failed, error: " + error + " - " + errorMsg);
                                Toast.makeText(getApplicationContext(), "创建白板  " + ConferenceInfo.getInstance().getRoomname() + "失败: " + errorMsg + " !",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
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
            btn_screenShare_layout.setVisibility(GONE);
            if(btn_screenShare_layout.isActivated()){
                //停止桌面推流
                unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP));
            }
            if(adminList != null){
                if(adminList.contains(EMClient.getInstance().getCurrentUser())) {
                    adminList.remove(EMClient.getInstance().getCurrentUser());
                }
            }
        } else if (state == STATE_TALKER) {
            onwheat();
            btn_screenShare_layout.setVisibility(VISIBLE);
            btn_desktop_share.setBackgroundResource(R.drawable.call_screenshare);
            btn_screenShare_layout.setActivated(false);
            btn_whiteboard.setActivated(false);
            whiteboard_view.setText("更多");
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
                 HangUpDialog dialog = (HangUpDialog)this.getSupportFragmentManager().findFragmentByTag("HangUpDialog");
                 if(dialog == null) {
                     dialog = HangUpDialog.getNewInstance(EMClient.getInstance().getCurrentUser());
                 }
                 if(dialog.isAdded()) {
                     return;
                 }
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
        if(ConferenceInfo.whiteboardCreator && ConferenceInfo.getInstance().getWhiteboard() != null)
        {
            EMClient.getInstance().conferenceManager().
                    deleteConferenceAttribute(WHITE_BOARD, new EMValueCallBack<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            EMLog.i(TAG, "deleteConferenceAttribute WHITE_BOARD success");
                            exit_confrence(null);
                        }
                        @Override
                        public void onError(int error, String errorMsg) {
                            EMLog.i(TAG, "deleteConferenceAttribute WHITE_BOARD failed: "
                                    + error + ""  + errorMsg);
                            exit_confrence(null);
                        }
                    });
        }else{
            exit_confrence(null);
        }
    }

    private void exit_confrence(String roomName){
        ScreenCaptureManager.getInstance().stop();
        stopAudioTalkingMonitor();
        timeHandler.stopTime();
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
                if(roomName != null){
                    intent.putExtra("roomName", roomName);
                }
                startActivity(intent);
                EMLog.i(TAG, "finish ConferenceActivity");
                destoryWhiteboard();
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.i(TAG, "exit conference failed " + error + ", " + errorMsg);
                Intent intent = new Intent(ConferenceActivity.this, MainActivity.class);
                if(roomName != null){
                    intent.putExtra("roomName", roomName);
                }
                startActivity(intent);
                destoryWhiteboard();
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
                        Toast.makeText(getApplicationContext(), "您已成功销毁当前会议！",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                EMLog.i(TAG, "start  MainActivity");
                Intent intent = new Intent(ConferenceActivity.this, MainActivity.class);
                startActivity(intent);
                EMLog.i(TAG, "finish ConferenceActivity");
                destoryWhiteboard();
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.i(TAG, "exit conference failed " + error + ", " + errorMsg);
                Intent intent = new Intent(ConferenceActivity.this, MainActivity.class);
                startActivity(intent);
                destoryWhiteboard();
            }
        });
    }

    private void destoryWhiteboard(){
        if(ConferenceInfo.whiteboardCreator && ConferenceInfo.getInstance().getWhiteboard() != null){
                String roomId = ConferenceInfo.getInstance().getWhiteboard().getRoomId();
                ConferenceInfo.getInstance().setWhiteboard(null);
                EMClient.getInstance().conferenceManager().destroyWhiteboardRoom(
                        EMClient.getInstance().getCurrentUser(),
                        EMClient.getInstance().getAccessToken(),roomId, new EMCallBack(){
                            @Override
                            public void onSuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        EMLog.i(TAG, "createWhiteboardRoom success, roomId: " + roomId);
                                        Toast.makeText(getApplicationContext(), "销毁白板  " +
                                                ConferenceInfo.getInstance().getRoomname() + "成功!",
                                                Toast.LENGTH_SHORT).show();
                                        //销毁以后重置
                                        ConferenceInfo.whiteboardCreator = false;
                                        ConferenceInfo.getInstance().setWhiteboard(null);
                                        finish();
                                    }
                                });
                            }

                            @Override
                            public void onError(int code, String error) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        EMLog.i(TAG, "destroyWhiteboardRoom error, roomId: "
                                                + roomId);
                                        Toast.makeText(getApplicationContext(), "销毁白板  " +
                                                        ConferenceInfo.getInstance().getRoomname() + "失败:" + error + " !",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                            @Override
                            public void onProgress(int progress, String status) {

                            }
                        });

            }else{
             finish();
        }
    }


    private void startAudioTalkingMonitor() {
        EMClient.getInstance().conferenceManager().startMonitorSpeaker(300);
    }

    private void stopAudioTalkingMonitor() {
        EMClient.getInstance().conferenceManager().stopMonitorSpeaker();
    }

    BroadcastReceiver bluetoothReceiver;
    LocalBroadcastReceiver localReceiver;
    LocalBroadcastManager localBroadcastManager= LocalBroadcastManager.getInstance(DemoHelper.getInstance().getContext());
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
     *监听收到会议邀请广播
     */
    private void registerInviteBroadCast() {
        localReceiver = new LocalBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String roomName = intent.getStringExtra("roomName");
                EMLog.i(TAG, "registerInviteBroadCast :roomName:" + roomName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinNewConferenceDisplay(roomName);
                    }
                });
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.invate.conference.LOCAL_BROADCAST");
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
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

        //停止蓝牙连接
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(false);

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
                        ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "member_add", member.memberName);
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
                ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "member_remove", member.memberName);
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
                int screenwidth = px2dip(getApplicationContext(),viewPager.getWidth());
                subVideoCount = screenwidth/90 + (screenwidth % 90 == 0 ? 0 :1);

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
                        }
                        ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "stream_add",stream.getMemberName());
                        ConferenceMemberInfo userProfile = new ConferenceMemberInfo();
                        userProfile.setStreamId(stream.getStreamId());
                        userProfile.setUserId(username);
                        userProfile.setAudioOff(stream.isAudioOff());
                        userProfile.setVideoOff(stream.isVideoOff());
                        userProfile.setDesktop(stream.getStreamType() == EMConferenceStream.StreamType.DESKTOP);
                        if(stream.getStreamType() != EMConferenceStream.StreamType.DESKTOP){
                            EMCallSurfaceView videoView = new EMCallSurfaceView(DemoHelper.getInstance().getContext());
                            videoView.setZOrderMediaOverlay(true);
                            videoView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                            userProfile.setVideoView(videoView);
                            userProfiles.add(userProfile);
                            //大于订阅视频数不去订阅视频流
                            if(subMemberList.size() >= subVideoCount){
                                //不去订阅视频流
                                stream.setVideoOff(true);
                            }else{
                                subMemberList.add(userProfile);
                            }
                            subscribe(stream, videoView);
                        }else{
                            //增加共享桌面流显示
//                            listView.add(desktop_share_view);
//                            vp_Adapter.notifyDataSetChanged();
//                            viewPager.setCurrentItem(1);
                            listView.add(desktop_share_view);
                            Collections.reverse(listView);
                            vp_Adapter.destroyItem(viewPager,0,null);
                            vp_Adapter.finishUpdate(viewPager);
                            vp_Adapter.setListView(listView);
                            viewPager.setAdapter(vp_Adapter);
                            vp_Adapter.notifyDataSetChanged();// 刷新
                            viewPager.setCurrentItem(0);
                            viewPager.setNoScroll(false);

                            desktop_share_avatar.setVisibility(View.INVISIBLE);
                            desktop_share_surfaceview.setVisibility(VISIBLE);
                            desktop_share_surfaceview.setZOrderMediaOverlay(true);
                            desktop_share_surfaceview.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                            userProfile.setVideoView(desktop_share_surfaceview);
                            userProfiles.add(userProfile);

                            //共享桌面增加加载图标
                            loading_stream_layout.setVisibility(VISIBLE);
                            subscribe(stream, desktop_share_surfaceview);
                        }

                        if(stream.getStreamType() != EMConferenceStream.StreamType.DESKTOP){
                            addConferenceView(userProfile);
                        }
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
                        ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "stream_remove",stream.getMemberName());
                        if(!TalkerListActivity.isActivte){
                            streamList.remove(stream);
                        }
                        ConferenceMemberInfo removedUserProfile = userProfiles.remove(index);
                        if(removedUserProfile.isDesktop()){
                            //移除共享桌面页面
//                            listView.remove(desktop_share_view);
//                            vp_Adapter.notifyDataSetChanged();
//                            viewPager.setCurrentItem(0);
                            viewPager.setCurrentItem(1);
                            listView.remove(desktop_share_view);
                            vp_Adapter.finishUpdate(viewPager);
                            vp_Adapter.notifyDataSetChanged();// 刷新
                            viewPager.setNoScroll(true);
                        }else{
                            removeConferenceView(removedUserProfile.getStreamId());
                        }
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
                    ConferenceMemberInfo userProfile = conferenceSession.
                            getConferenceMemberByStreamId(stream.getStreamId());
                    if (userProfile != null) {
                        ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "stream_update",stream.getMemberName());
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
//        EMLog.e(TAG,  "Encode Resolution: " + statistics.getLocalEncodedWidth() + "  " + statistics.getLocalEncodedHeight() + " bps: " +statistics.getLocalVideoActualBps() + "  FPS: " + statistics.getLocalEncodedFps());
//
//        EMLog.e(TAG,  "akps:" +  statistics.getLocalAudioBps() + " vkps:" + statistics.getLocalVideoActualBps());
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
    public void onStreamStateUpdated(String streamId,StreamState state){
        EMLog.i(TAG,"onStreamStateUpdated   streamId：" + streamId + "  " +
                "state: " + state.name());
        if(state == StreamState.STREAM_NO_AUDIO_DATA || state == StreamState.STREAM_NO_VIDEO_DATA) {
            EMConferenceStream stream = ConferenceInfo.getInstance().getConferenceSpeakStream(streamId);
            if(stream != null){
               String userName =  EasyUtils.useridFromJid(stream.getMemberName());
                EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(userName);
                if (memberInfo != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (state == StreamState.STREAM_NO_AUDIO_DATA) {
                                Toast.makeText(getApplicationContext(), "已收不到 " + memberInfo.nickName + " 的音频数据",
                                        Toast.LENGTH_SHORT).show();
                            } else if (state == StreamState.STREAM_NO_VIDEO_DATA) {
                                Toast.makeText(getApplicationContext(), "已收不到 " + memberInfo.nickName + " 的视频数据",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onFirstFrameSent(String streamId,StreamFrameType frameType){
        EMLog.i(TAG,"onFirstFrameSended frameType: " + frameType.name());
    }

    @Override
    public void onFirstFrameRecived(String streamId,StreamFrameType frameType){
        EMLog.i(TAG,"onFirstFrameRecived frameType: " + frameType.name());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //获取第一帧视频数据后停止转圈等待
                EMConferenceStream stream = ConferenceInfo.getInstance().getConferenceSpeakStream(streamId);
                if(stream.getStreamType() != EMConferenceStream.StreamType.DESKTOP){
                    if(stream != null){
                        //流有视频时候开启视频时候再停止等待
                        int viewId = getViewIdByStreamId(stream.getStreamId());
                        MultiMemberView memberView = findViewById(viewId);
                        if(!stream.isVideoOff()){
                            if(frameType == StreamFrameType.VIDEO_FRAME){
                                if(memberView != null){
                                    memberView.cancelLoadingDialog();
                                }
                            }
                        }else{
                            if(memberView != null){
                                memberView.cancelLoadingDialog();
                            }
                        }
                    }
                }else{
                    loading_stream_layout.setVisibility(GONE);
                }
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
                            if (speakers.contains(localStream.getStreamId())) {
                                setSpeakIcon(localStream.getStreamId(), true);
                            } else {
                                setSpeakIcon(localStream.getStreamId(), false);
                            }
                            for (EMConferenceStream streamInfo : streamList) {
                                if (speakers.contains(streamInfo.getStreamId())) {
                                    setSpeakIcon(streamInfo.getStreamId(), true);
                                } else {
                                    setSpeakIcon(streamInfo.getStreamId(), false);
                                }
                            }
                            return;
                        }
                    }
                    //会议中没有人说话
                    setSpeakIcon(localStream.getStreamId(), false);
                    for (EMConferenceStream streamInfo : streamList) {
                        setSpeakIcon(streamInfo.getStreamId(), false);
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
                    if(memberView.isVideoOff()){
                        video_off_animator.start();
                        speak_show_view.setImageResource(R.drawable.em_voice_change);
                    }else{
                        video_on_animator.start();
                        speakImage_view.setImageResource(R.drawable.em_voice_change);
                    }
                    if (viewId != selfRadioButtonId) {

                    } else{
                        if (localStream.isAudioOff()) {
                            memberView.setAudioNoSpeak();
                        }else{
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
                    if(memberView.isVideoOff()){
                        if(memberView.isAudioOff()){
                            video_off_animator.cancel();
                            speak_show_view.setImageResource(R.drawable.em_call_mic_off);
                        }else{
                            video_off_animator.start();
                            speak_show_view.setImageResource(R.drawable.em_call_mic_on);
                        }
                    }else{
                        if(memberView.isAudioOff()){
                            video_off_animator.cancel();
                            speakImage_view.setImageResource(R.drawable.em_call_mic_off);
                        }else{
                            video_off_animator.cancel();
                            speakImage_view.setImageResource(R.drawable.em_call_mic_on);
                        }
                    }
                    if (viewId == selfRadioButtonId) {
                        if (localStream.isAudioOff()) {
                            memberView.setAudioNoSpeak();
                        } else {
                            memberView.setAudioSpeak();
                        }
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
                EMLog.i(TAG,"Subscribe scuessed  streamId: " + stream.getStreamId());
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.i(TAG,"Subscribe failed  streamId: " + stream.getStreamId());
                if(stream.getStreamType() != EMConferenceStream.StreamType.DESKTOP){
                    int viewId = getViewIdByStreamId(stream.getStreamId());
                    MultiMemberView memberView =  findViewById(viewId);
                    if(memberView != null){
                        memberView.cancelLoadingDialog();
                    }else{
                        EMLog.i(TAG,"Subscribe memberView is null");
                    }
                }else{
                    loading_stream_layout.setVisibility(GONE);
                }
            }
        });
    }

    /**
     * 更新订阅指定成员stream
     */
    private void updateSubscribe(EMConferenceStream stream ,EMCallSurfaceView surfaceView) {
        EMClient.getInstance().conferenceManager().updateSubscribe(stream, surfaceView, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                EMLog.i(TAG,"updateSubscribe successed streamId:" + stream.getStreamId() + " memberId:"  + stream.getMemberName());
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.i(TAG,"updateSubscribe failed streamId:" + stream.getStreamId() + " error:"  + error + " errorMsg:" + errorMsg);
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
                        EMConferenceStream  streamInfo = ConferenceInfo.getInstance(). getConferenceStreamByMemId(memName);
                        if(streamInfo != null){
                            int viewId = getViewIdByStreamId(streamInfo.getStreamId());
                            MultiMemberView memberView = findViewById(viewId);
                            if(memberView != null){
                                memberView.setUsername(memName,false);
                            }
                            if(viewId == lastSelectedId){
                                admin_show_view.setVisibility(VISIBLE);
                                adminImage_view.setVisibility(VISIBLE);
                            }
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
                    EMConferenceStream  streamInfo = ConferenceInfo.getInstance(). getConferenceStreamByMemId(memName);
                    if(streamInfo != null){
                        int viewId = getViewIdByStreamId(streamInfo.getStreamId());
                        MultiMemberView memberView = findViewById(viewId);
                        if(memberView != null){
                            memberView.setUsername(memName,false);
                        }
                        if(viewId == lastSelectedId){
                            admin_show_view.setVisibility(GONE);
                            adminImage_view.setVisibility(GONE);
                        }
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
        //处理放弃主持人时的情况
        if(conference.getConferenceRole() == EMConferenceManager.EMConferenceRole.Admin && role == EMConferenceManager.EMConferenceRole.Talker){
            conference.setConferenceRole(role);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int viewId = getViewIdByStreamId(localStream.getStreamId());
                    MultiMemberView memberView = findViewById(viewId);
                    if(memberView != null){
                        memberView.setUsername(EMClient.getInstance().getCurrentUser(),false);
                    }
                    if(adminList.contains(EMClient.getInstance().getCurrentUser())){
                        adminList.remove(EMClient.getInstance().getCurrentUser());
                        adminImage_view.setVisibility(VISIBLE);
                    }
                    if(viewId == lastSelectedId){
                        admin_show_view.setVisibility(GONE);
                        adminImage_view.setVisibility(GONE);
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
                        memberView.setUsername(EMClient.getInstance().getCurrentUser(),false);
                    }
                    if(viewId == lastSelectedId){
                        admin_show_view.setVisibility(VISIBLE);
                        adminImage_view.setVisibility(VISIBLE);
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
    public void onPubDesktopStreamFailed(int error, String message){
        EMLog.i(TAG, "OnPubDesktopStreamFailed  error :" + error + " message:" + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "发布共享桌面流失败  errorMessage:"
                                + message, Toast.LENGTH_SHORT).show();

                btn_screenShare_layout.setActivated(false);
                btn_desktop_share.setBackgroundResource(R.drawable.call_screenshare);
                unpublish(conference.getPubStreamId(EMConferenceStream.StreamType.DESKTOP));
            }
        });

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
            }else if(usreId.equals(WHITE_BOARD)){ //白板
                if(!option.equals("")){
                    try {
                        JSONObject object = new JSONObject(option);
                        String creator = object.optString("creator");
                        String roomName = object.optString("roomName");
                        String roomPswd = object.optString("roomPswd");
                        long timestamp = object.optLong("timestamp");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //增加白板小窗口
                                if(conferenceSession.getConferenceMemberByStreamId(roomName) == null){
                                    ConferenceMemberInfo userProfile = new ConferenceMemberInfo();
                                    userProfile.setStreamId(roomName);
                                    userProfile.setWhiteboardPwd(roomPswd);
                                    userProfile.setUserId(creator);
                                    userProfile.setWhiteboard(true);
                                    conferenceSession.getConferenceProfiles().add(userProfile);
                                    WhiteBoardRoomInfo roomInfo = new WhiteBoardRoomInfo();
                                    roomInfo.setCreator(roomName);
                                    roomInfo.setRoomName(roomPswd);
                                    roomInfo.setRoomPswd(creator);
                                    ConferenceInfo.getInstance().
                                            setWhiteboardRoomInfo(roomInfo);
                                    whiteboard_option(roomName,roomPswd);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int index = -1;
                            List<ConferenceMemberInfo> userProfiles = conferenceSession.getConferenceProfiles();
                            for (int j = 0; j < userProfiles.size(); j++) {
                                ConferenceMemberInfo userProfile = userProfiles.get(j);
                                if (userProfile != null && userProfile.getStreamId() != null &&
                                        userProfile .getStreamId().equals(ConferenceInfo.
                                                getInstance().getWhiteboardRoomInfo().getRoomName())) {
                                    index = j;
                                    break;
                                }
                            }
                            ConferenceMemberInfo removedUserProfile = userProfiles.remove(index);
                            if(removedUserProfile != null){
//                                viewPager.setCurrentItem(1);
//                                listView.remove(whiteborad_view);
//                                vp_Adapter.finishUpdate(viewPager);
//                                vp_Adapter.notifyDataSetChanged();// 刷新
//                                viewPager.setNoScroll(true);

                                 if (conferenceSession.getConferenceProfiles().size() == 1){
                                    bottomContainer11.setVisibility(GONE);
                                    bottomContainer.setVisibility(VISIBLE);
                                    bottomContainerView.setVisibility(GONE);
                                 }

                                if(ConferenceInfo.getInstance().getWhiteboardRoomInfo() != null){
                                    if(removedUserProfile.getStreamId().equals(ConferenceInfo.getInstance().getWhiteboardRoomInfo().getRoomName())){
                                        ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this, "destoryWhiteboard",ConferenceInfo.getInstance().getWhiteboardRoomInfo().getRoomName());
                                        ConferenceInfo.getInstance().setWhiteboardRoomInfo(null);
                                        return;
                                    }
                                }

                            }
                        }
                    });
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
                                    EMLog.i(TAG, "onAttributesUpdated  request_tobe_audience failed, error: " +
                                                 error + " - " + errorMsg);
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
                                            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this,
                                                    "indexUpdate",userId);
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
                                            ConfigManager.getInstance().getConfig(mId).set(ConferenceActivity.this,
                                                                  "indexUpdate",userId);
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
            if(!usreId.equals(REQUEST_TOBE_MUTE_ALL) && !usreId.equals(WHITE_BOARD)){
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
                                EMLog.i(TAG, " requestTalkerDisplay  request_tobe_speaker changeRole failed, error: " +
                                                  error + " - " + errorMsg);
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
                                EMLog.i(TAG, " requestTalkerDisplay  request_tobe_admin changeRole failed, error: " +
                                             error + " - " + errorMsg);
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
                                EMLog.i(TAG, "takerListChooseDispaly ok choose to offline userId " + choose_userId +
                                            "  success, result: " + value);
                                //让申请的主播上线
                                String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), usreId);
                                EMClient.getInstance().conferenceManager().grantRole(conference.getConferenceId()
                                        , new EMConferenceMember(usreId, null, null,null)
                                        , EMConferenceManager.EMConferenceRole.Talker, new EMValueCallBack<String>() {
                                            @Override
                                            public void onSuccess(String value) {
                                                EMLog.i(TAG, "takerListChooseDispaly ok choose to online userId " + usreId +
                                                        "  success, result: " + value);
                                            }
                                            @Override
                                            public void onError(int error, String errorMsg) {
                                                EMLog.i(TAG, "takerListChooseDispaly ok choose to online userId " + usreId +
                                                         "  error: " + error + " - " + errorMsg);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "申请上麦失败!" + error +
                                                                  "  " + errorMsg, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                            }
                            @Override
                            public void onError(int error, String errorMsg) {
                                EMLog.i(TAG, "takerListChooseDispaly ok choose to offline userId " + choose_userId +
                                                   " failed, error: " + error + " - " + errorMsg);


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
                    if(desktopHeadImage){
                        //共享桌面界面显示头像
                        desktop_headImage_view.setImageBitmap(bitmap);
                        desktopHeadImage = false;
                    }else{
                        headImageView.setImageBitmap(bitmap);
                        largeSurfacePreview.setBackgroundColor(Color.rgb(60,60,60));
                    }
                }
            }
        }.execute(headImageurl);
    }


    /**
     * 实时检测网络信号强度
     * @return
     */
    public void checkWifiState() {
        NetUtils.Types type = NetUtils.getNetworkTypes( getApplicationContext());
        if (type == NetUtils.Types.WIFI) {
            WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int wifi = mWifiInfo.getRssi();//获取wifi信号强度
            if (wifi > -50 && wifi < 0) {//最强
                //Log.e(TAG, "最强");
                //netInfoView.setBackgroundResource(R.drawable.networkinfo);
            } else if (wifi > -70 && wifi < -50) {//较强
                //Log.e(TAG, "较强");
                //netInfoView.setBackgroundResource(R.drawable.networkinfo4);
            } else if (wifi > -80 && wifi < -70) {//较弱
                //Log.e(TAG, "较弱");
                //netInfoView.setBackgroundResource(R.drawable.networkinfo3);
            } else if (wifi > -100 && wifi < -80) {//微弱
                //Log.e(TAG, "微弱");
                //netInfoView.setBackgroundResource(R.drawable.networkinfo2);
            }
        } else if(type == NetUtils.Types.MOBILE){
                //是手机网络信号
                telephonyManager = (TelephonyManager) getApplicationContext()
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if(telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                    //4G网络 最佳范围   >-90dBm 越大越好
                    //Log.e(TAG, "最强");
                    //netInfoView.setBackgroundResource(R.drawable.networkinfo);
                }else if(telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                                telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                                telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                                telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS){
                            //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不
                            //Log.e(TAG, "较强");
                            //netInfoView.setBackgroundResource(R.drawable.networkinfo4);
                }else{
                    //2G网络最佳范围>-90dBm 越大越好
                    //Log.e(TAG, "较弱");
                    //netInfoView.setBackgroundResource(R.drawable.networkinfo3);
                    }
            }else{
                //无连接
               Log.e(TAG, "无网络连接");
               //netInfoView.setBackgroundResource(R.drawable.networkinfo0);
            }
    }

    /**
     * 定时更新通话时间
     * @param time
     */
    int count = 0;
    private void updateConferenceTime(String time) {
        meeting_duration.setText(time);
        //checkWifiState();
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
                bottomContainer11.setVisibility(VISIBLE);
                bottomContainer.setVisibility(VISIBLE);
                bottomContainerView.setVisibility(VISIBLE);
                bottomContainer11.bringToFront();
            }
        }
    }

    private void getConferenceInfoAdmins(){
        EMClient.getInstance().conferenceManager().getConferenceInfo(ConferenceInfo.getInstance().getConference().getConferenceId(),
                                               ConferenceInfo.getInstance().getPassword(), new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(EMConference value) {
                        EMLog.i(TAG, "getConferenceInfo  successed");
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
                                    if(value.getTalkers() != null){

                                        if(adminList.size() > 0){
                                            if(value.getTalkers() != null)
                                            {
                                                for(int i = 0; i < adminList.size(); i++){
                                                    String memName = adminList.get(i);
                                                    if(EMClient.getInstance().getCurrentUser().equals(memName)){
                                                        admin_show_view.setVisibility(VISIBLE);
                                                        adminImage_view.setVisibility(VISIBLE);
                                                    }else{
                                                        adminImage_view.setVisibility(GONE);
                                                    }
                                                    EMConferenceStream  streamInfo =ConferenceInfo.getInstance().getConferenceStreamByMemId(memName);
                                                    if(streamInfo != null){
                                                        int viewId = getViewIdByStreamId(streamInfo.getStreamId());
                                                        MultiMemberView memberView = findViewById(viewId);
                                                        if(memberView != null){
                                                            memberView.setUsername(memName,false);
                                                        }
                                                    }
                                                }
                                            }
                                            if(ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager
                                                    .EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 0){
                                                if(adminList.contains(conferenceSession.getConferenceProfiles().get(0).getUserId())){
                                                    admin_show_view.setVisibility(VISIBLE);
                                                    adminImage_view.setVisibility(VISIBLE);
                                                }else{
                                                    adminImage_view.setVisibility(GONE);
                                                }
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

    private void addWhiteBoardWindow(){
        //增加白板小窗口
        if(ConferenceInfo.getInstance().getWhiteboardRoomInfo() != null) {
            //增加白板小窗口
            ConferenceMemberInfo userProfile = new ConferenceMemberInfo();
            userProfile.setStreamId(ConferenceInfo.getInstance().getWhiteboardRoomInfo().getRoomName());
            userProfile.setWhiteboardPwd(ConferenceInfo.getInstance().getWhiteboardRoomInfo().getRoomPswd());
            userProfile.setUserId(ConferenceInfo.getInstance().getWhiteboardRoomInfo().getCreator());
            userProfile.setWhiteboard(true);
            conferenceSession.getConferenceProfiles().add(userProfile);
            add_whiteboard_view(ConferenceInfo.getInstance().getWhiteboardRoomInfo().getRoomName(),
                    ConferenceInfo.getInstance().getWhiteboardRoomInfo().getRoomPswd());
        }
    }

    //观众身份是管理管理员时候 申请管理员列表(主要是第一个观众进入创建房间情况)
    private void getAdminsToBeSpeaker() {
        EMClient.getInstance().conferenceManager().getConferenceInfo(ConferenceInfo.getInstance().getConference().getConferenceId(),
                ConferenceInfo.getInstance().getPassword(), new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(EMConference value) {
                        EMLog.i(TAG, "getConferenceInfo  successed");
                        ConferenceInfo.getInstance().getConference().setTalkers(value.getTalkers());
                        ConferenceInfo.getInstance().getConference().setAudienceTotal(value.getAudienceTotal());
                        ConferenceInfo.getInstance().getConference().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().getAdmins().clear();
                        ConferenceInfo.getInstance().setAdmins(value.getAdmins());
                        ConferenceInfo.getInstance().getConference().setMemberNum(value.getMemberNum());
                        adminList = ConferenceInfo.getInstance().getAdmins();

                        //申请上麦
                        String adminId = null;
                        for(int i =0 ;i < adminList.size(); i++){
                            if(!adminList.get(i).contains(EMClient.getInstance().getCurrentUser())){
                                adminId = adminList.get(i);
                                break;
                            }
                        }
                        if(adminId != null){
                            EMConferenceMember adminMemberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(adminId);
                            if (adminMemberInfo != null) {
                                EMClient.getInstance().conferenceManager().applyTobeSpeaker(adminMemberInfo.memberId);
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "本房间还未指定主持人，不允许上麦!", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "本房间还未指定主持人，不允许上麦!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        EMLog.i(TAG, "onActivityResult: " + requestCode + ", result code: " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == ScreenCaptureManager.RECORD_REQUEST_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ScreenCaptureManager.getInstance().start(resultCode, data);
                }
            }else if(requestCode == FIREHOUSE_RESULT_CODE){
                if (null != uploadFile) {
                    Uri result = data == null || resultCode != RESULT_OK ? null
                            : data.getData();
                    uploadFile.onReceiveValue(result);
                    uploadFile = null;
                }
                if (null != uploadFiles) {
                    Uri result = data == null || resultCode != RESULT_OK ? null
                            : data.getData();
                    uploadFiles.onReceiveValue(new Uri[]{result});
                    uploadFiles = null;
                }
            }else if(requestCode == 0){
                if (null != uploadFile) {
                    Uri result = data == null || resultCode != RESULT_OK ? null
                            : data.getData();
                    uploadFile.onReceiveValue(result);
                    uploadFile = null;
                }
            }
        }else if (resultCode == RESULT_CANCELED) {
            if (null != uploadFile) {
                uploadFile.onReceiveValue(null);
                uploadFile = null;
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
        if(localReceiver  != null){
            localBroadcastManager.unregisterReceiver(localReceiver);
        }

        mSensorManager.unregisterListener(mOrientationListener);
        ConferenceInfo.getInstance().setConference(null);

        EMLog.i(TAG,"onDestroy over   ConferenceActivity  Main threadID: " + Thread.currentThread().getName());

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation  == Configuration.ORIENTATION_LANDSCAPE) {  //横屏显示
            portrait = false;
            bottomContainerView.setVisibility(GONE);

            //菜单栏重新进行布局
            if(conferenceSession.getConferenceProfiles() != null) {
                if ((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 1) ||
                        (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 0) || portrait) {
                    bottomContainerScrollView.setVisibility(VISIBLE);
                    memberContainer.setOrientation(LinearLayout.VERTICAL);
                    bottomContainerView.removeAllViews();
                    bottomContainerScrollView.removeAllViews();
                    bottomContainerScrollView.addView(memberContainer);

                    bottomContainer11.setVisibility(VISIBLE);
                    bottomContainer.setVisibility(VISIBLE);
                    bottomContainerView.setVisibility(GONE);
                }
            }
        }else if(newConfig.orientation  == Configuration.ORIENTATION_PORTRAIT){ //竖屏显示
            portrait = true;
            bottomContainerScrollView.setVisibility(GONE);

            //菜单栏重新进行布局
            if(conferenceSession.getConferenceProfiles() != null) {
                if ((ConferenceInfo.getInstance().getConference().getConferenceRole() != EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 1) ||
                        (ConferenceInfo.getInstance().getConference().getConferenceRole() == EMConferenceManager.EMConferenceRole.Audience && conferenceSession.getConferenceProfiles().size() > 0)) {
                    bottomContainerView.setVisibility(VISIBLE);
                    memberContainer.setOrientation(LinearLayout.HORIZONTAL);
                    bottomContainerScrollView.removeAllViews();
                    bottomContainerView.removeAllViews();
                    bottomContainerView.addView(memberContainer);


                    bottomContainer11.setVisibility(VISIBLE);
                    bottomContainer.setVisibility(VISIBLE);
                    bottomContainerView.setVisibility(VISIBLE);
                }
            }
        }
    }

    private void initWhiteboardView(){
        mWebView = new TouchWebView(this, null);

        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.FILL_PARENT,
                FrameLayout.LayoutParams.FILL_PARENT));

        if(bottomContainer.getVisibility() == VISIBLE){
            destory_btn.setBackgroundResource(R.drawable.em_call_scale_fill);
        }else{
            destory_btn.setBackgroundResource(R.drawable.em_call_scale_fit);
        }
        destory_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setToolsIsHidden();
                        if(bottomContainer.getVisibility() == VISIBLE){
                            destory_btn.setBackgroundResource(R.drawable.em_call_scale_fill);
                        }else{
                            destory_btn.setBackgroundResource(R.drawable.em_call_scale_fit);
                        }
                    }
                });
            }
        });


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       viewPager.setCurrentItem(1);
                    }
                });
            }
        });



        mWebView.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(com.tencent.smtt.sdk.WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(com.tencent.smtt.sdk.WebView view, String url) {
                super.onPageFinished(view, url);
                //mTestHandler.sendEmptyMessageDelayed(MSG_OPEN_TEST_URL, 5000);// 5s?
            }
        });

        mWebView.setWebChromeClient(new com.tencent.smtt.sdk.WebChromeClient() {

            @Override
            public boolean onJsConfirm(com.tencent.smtt.sdk.WebView arg0, String arg1, String arg2,
                                       com.tencent.smtt.export.external.interfaces.JsResult arg3) {
                return super.onJsConfirm(arg0, arg1, arg2, arg3);
            }

            View myVideoView;
            View myNormalView;
            IX5WebChromeClient.CustomViewCallback callback;

            @Override
            public void onShowCustomView(View view,
                                         IX5WebChromeClient.CustomViewCallback customViewCallback) {
            }

            @Override
            public void onHideCustomView() {
                if (callback != null) {
                    callback.onCustomViewHidden();
                    callback = null;
                }
                if (myVideoView != null) {
                    ViewGroup viewGroup = (ViewGroup) myVideoView.getParent();
                    viewGroup.removeView(myVideoView);
                    viewGroup.addView(myNormalView);
                }
            }

            @Override
            public boolean onConsoleMessage(com.tencent.smtt.export.external.interfaces.ConsoleMessage consoleMessage) {
//                EMLog.d("MyApplication", consoleMessage.message() + " -- From line "
//                        + consoleMessage.lineNumber() + " of "
//                        + consoleMessage.sourceId());
                return true;
            }

            @Override
            public boolean onJsAlert(com.tencent.smtt.sdk.WebView arg0, String arg1, String arg2,
                                     com.tencent.smtt.export.external.interfaces.JsResult arg3) {
                return super.onJsAlert(null, arg1, arg2, arg3);
            }

            // For Android 3.0+
            public void openFileChooser(com.tencent.smtt.sdk.ValueCallback<Uri> uploadMsg, String acceptType) {
                EMLog.i(TAG, "openFileChooser 1");
                ConferenceActivity.this.uploadFile = uploadFile;
                openFileChooseProcess();
            }

            // For Android < 3.0
            public void openFileChooser(com.tencent.smtt.sdk.ValueCallback<Uri> uploadMsgs) {
                EMLog.i(TAG, "openFileChooser 2");
                ConferenceActivity.this.uploadFile = uploadFile;
                openFileChooseProcess();
            }

            // For Android  > 4.1.1
            public void openFileChooser(com.tencent.smtt.sdk.ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                EMLog.i(TAG, "openFileChooser 3");
                ConferenceActivity.this.uploadFile = uploadFile;
                openFileChooseProcess();
            }

            // For Android  >= 5.0
            public boolean onShowFileChooser(com.tencent.smtt.sdk.WebView webView,
                                             com.tencent.smtt.sdk.ValueCallback<Uri[]> filePathCallback,
                                             com.tencent.smtt.sdk.WebChromeClient.FileChooserParams fileChooserParams) {
                EMLog.i(TAG, "openFileChooser 4:" + filePathCallback.toString());
                ConferenceActivity.this.uploadFiles = filePathCallback;
                openFileChooseProcess();
                return true;
            }
        });

        mWebView.setDownloadListener(new com.tencent.smtt.sdk.DownloadListener() {

            @Override
            public void onDownloadStart(String arg0, String arg1, String arg2,
                                        String arg3, long arg4) {
                TbsLog.d(TAG, "url: " + arg0);
                new AlertDialog.Builder(ConferenceActivity.this)
                        .setTitle("allow to download？")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Toast.makeText(
                                                ConferenceActivity.this,
                                                "fake message: i'll download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton("no",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        Toast.makeText(
                                                ConferenceActivity.this,
                                                "fake message: refuse download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setOnCancelListener(
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        // TODO Auto-generated method stub
                                        Toast.makeText(
                                                ConferenceActivity.this,
                                                "fake message: refuse download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
            }
        });

        com.tencent.smtt.sdk.WebSettings webSetting = mWebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(com.tencent.smtt.sdk.WebSettings.
                LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        webSetting.setPluginState(com.tencent.smtt.sdk.WebSettings.PluginState.ON_DEMAND);
        long time = System.currentTimeMillis();
        if (mIntentUrl == null) {
            mWebView.loadUrl(roomUrl);
        } else {
            mWebView.loadUrl(mIntentUrl.toString());
        }
        TbsLog.d("time-cost", "cost time: "
                + (System.currentTimeMillis() - time));
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();

    }

    private void openFileChooseProcess() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("video/*;image/*");
        startActivityForResult(i,FIREHOUSE_RESULT_CODE);
    }

    public static final int MSG_OPEN_TEST_URL = 0;
    public static final int MSG_INIT_UI = 1;
    private final int mUrlStartNum = 0;
    private int mCurrentUrl = mUrlStartNum;
    private Handler mTestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OPEN_TEST_URL:
                    if (!mNeedTestPage) {
                        return;
                    }
                    String testUrl = "file:///sdcard/outputHtml/html/"
                            + Integer.toString(mCurrentUrl) + ".html";
                    if (mWebView != null) {
                        mWebView.loadUrl(testUrl);
                    }
                    mCurrentUrl++;
                    break;
                case MSG_INIT_UI:
                    initWhiteboardView();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent==null){
            return;
        }
    }


    /**
     * 是否加入新的会议提示框
     */
    public void joinNewConferenceDisplay(String roomName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConferenceActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(ConferenceActivity.this, R.layout.activity_talker_full, null);
        dialog.setView(dialogView);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        dialog.show();

        final Button btn_ok = dialogView.findViewById(R.id.btn_ok);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel);
        final TextView text_view = dialogView.findViewById(R.id.info_view);
        String infoStr = "是否要离开当前会议" +"\n";
        infoStr += "加入新会议：" + roomName;
        text_view.setText(infoStr);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                dialog.dismiss();
                EMLog.e(TAG, " join new conference roomName:" + roomName);
                finish();
                exit_confrence(roomName);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //主播已满不加入会议
                EMLog.e(TAG, "not join new conference roomName:" + roomName);
            }
        });
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