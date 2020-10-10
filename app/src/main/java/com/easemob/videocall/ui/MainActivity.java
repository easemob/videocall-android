package com.easemob.videocall.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.runtimepermissions.PermissionsManager;
import com.easemob.videocall.runtimepermissions.PermissionsResultAction;
import com.easemob.videocall.utils.ConferenceSession;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMAudioConfig;
import com.hyphenate.chat.EMCDNCanvas;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMLiveConfig;
import com.hyphenate.chat.EMRoomConfig;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.easemob.videocall.DemoHelper;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hyphenate.EMError.CALL_TALKER_ISFULL;


/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class MainActivity extends Activity {
    private final String TAG = this.getClass().getSimpleName();
    private EditText roomnameEditText;
    private String username;
    private String currentRoomname;
    private String currentPassword;
    private String currentNickname;
    private EMConferenceManager.EMConferenceRole conferenceRole;
    private String password = "123";
    final private String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？-]";
    private Button btn_anchor;
    private TextView version_view;
    private TextView error_text_view;
    private ConferenceSession conferenceSession;
    private String url = "http://download-sdk.oss-cn-beijing.aliyuncs.com/downloads/RtcDemo/version.conf";
    private boolean showNickNameFlag = false;
    private Dialog dialog;
    private boolean register = false;
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_anchor = (Button) findViewById(R.id.btn_anchor);
        version_view = (TextView) findViewById(R.id.versionName_view);
        roomnameEditText = (EditText) findViewById(R.id.roomname);
        roomnameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
        error_text_view = (TextView) findViewById(R.id.error_text_view);

        if (ConferenceInfo.getInstance().getRoomname() != null) {
            roomnameEditText.setText(ConferenceInfo.getInstance().getRoomname());
            btn_anchor.setBackgroundResource(R.drawable.em_button_add_bg);
        }
        EditText editText = (EditText) findViewById(R.id.roomname);
        editText.clearFocus();
        editText.setSelected(false);
        setError_text("");
        conferenceSession = DemoHelper.getInstance().getConferenceSession();

        //申请权限
        requestPermissions();
        getLatestVersion();
        roomnameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    String str1 = "";
                    for (int i = 0; i < str.length; i++) {
                        str1 += str[i];
                    }
                    roomnameEditText.setText(str1);
                    roomnameEditText.setSelection(start);
                }
                if (roomnameEditText.getText().length() > 2) {
                    btn_anchor.setBackgroundResource(R.drawable.em_button_add_bg);
                } else {
                    btn_anchor.setBackgroundResource(R.drawable.em_button_add_grey);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        String roomName = this.getIntent().getStringExtra("roomName");
        if(roomName == null){
            Intent intent = getIntent();
            if(intent != null) {
                uri = intent.getData();
                if (null != uri) {
                    EMLog.i(TAG, "uri-->" + "" + uri.getScheme()
                            + "-path->" + uri.getPath()
                            + "-roomName->" + uri.getQueryParameter("roomName"));
                    String roomInfo = uri.getQueryParameter("roomName");
                    roomInfo = URLDecoder.decode(roomInfo);
                    if (roomInfo != null && roomInfo != "") {
                        if (ConferenceInfo.getInstance().getConference() != null) {
                            if (roomInfo.equals(ConferenceInfo.getInstance().getRoomname())) {
                                Toast.makeText(getApplicationContext(), "您已经在此会议中",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }else{

                                Intent roomintent = new Intent("com.invate.conference.LOCAL_BROADCAST");
                                roomintent.putExtra("roomName", roomInfo);
                                LocalBroadcastManager.getInstance(DemoHelper.getInstance().getContext()).sendBroadcast(roomintent);
                                finish();
                            }
                        }else{
                            roomnameEditText.setText(roomInfo);
                            btn_anchor.setBackgroundResource(R.drawable.em_button_add_bg);
                            addconference_anchor(this.version_view);
                        }
                    }else {
                        roomnameEditText.setText(roomInfo);
                        btn_anchor.setBackgroundResource(R.drawable.em_button_add_bg);
                        addconference_anchor(this.version_view);
                    }
                }
            }
        }else{
            roomnameEditText.setText(roomName);
            btn_anchor.setBackgroundResource(R.drawable.em_button_add_bg);
            addconference_anchor(this.version_view);
        }
    }


    /**
     加入会议房间
     */
    public void addconference_anchor(View view){
        if(showNickNameFlag){
            return;
        }
        getLatestVersion();
        setBtnEnable(false);

        currentRoomname = roomnameEditText.getText().toString().trim();
        currentPassword = roomnameEditText.getText().toString().trim();

        if(currentRoomname.length() == 0 && currentPassword.length() == 0){
            setError_text("房间名不允许为空");
            setBtnEnable(true);
            return;
        }
        if(currentRoomname.length() < 3){
            setError_text("房间名不能少于3位");
            setBtnEnable(true);
            return;
        }
        if(currentPassword.length() < 3){
            setBtnEnable(true);
            setError_text("密码不能少于3位");
            return;
        }

        if(currentRoomname.length() > 18){
            setBtnEnable(true);
            setError_text("房间名不能超过18位");
            return;
        }
        if(currentPassword.length() > 18){
            setBtnEnable(true);
            setError_text("房间名不能超过18位");
            return;
        }
        if(!isLegalChars(currentRoomname)){
            setBtnEnable(true);
            setError_text("房间名不允许输入除数字、中文、英文、下划线或者减号以外的特殊字符");
            return;
        }
        if(!isLegalChars(currentPassword)){
            setBtnEnable(true);
            setError_text("密码不允许输入除数字、中文、英文、下划线或者减号以外的特殊字符");
            return;
        }

        if(!PreferenceManager.getInstance().isAudience()){
            conferenceRole = EMConferenceManager.EMConferenceRole.Talker;
            ConferenceInfo.getInstance().setCurrentrole(EMConferenceManager.EMConferenceRole.Talker);
        }else{
            conferenceRole = EMConferenceManager.EMConferenceRole.Audience;
            ConferenceInfo.getInstance().setCurrentrole(EMConferenceManager.EMConferenceRole.Audience);
        }

        username = PreferenceManager.getInstance().getCurrentUsername();

        currentNickname = PreferenceManager.getInstance().getCurrentUserNick();

        if(currentNickname != null){
            if(username == null){
                register();
            }else{
                password = PreferenceManager.getInstance().getCurrentUserPassWord();
                login();
            }
        }else{
            showNickNameFlag = true;
            setNickNameDialogDisplay();
        }
    }



    /**
     * 输入字符检测(只能输入数字，字母，汉字和下划线)
     * @param chars
     * @return
     */
    public static boolean isLegalChars(String chars){
        String regex ="^[\\u4e00-\\u9fa5A-Za-z0-9_-]*$";
        boolean result = chars.matches(regex);
        return result;
    }


    /**
    自动注册一个账号
     */
    public void register(){
        //加载loading
        register = true;
        setProgressDialog();
        new Thread(new Runnable() {
            public void run() {
                UUID uuid = UUID.randomUUID();
                String aa = "";//这里是将特殊字符换为aa字符串," "代表直接去掉
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(uuid.randomUUID().toString());//这里把想要替换的字符串传进来
                username = m.replaceAll(aa).trim();
                try {
                    // call method in SDK
                    EMClient.getInstance().createAccount(username, password);

                    //注册成功进行登录
                    PreferenceManager.getInstance().setCurrentUserName(username);
                    PreferenceManager.getInstance().setCurrentuserPassword(password);
                    login();
                } catch (final HyphenateException e) {
                    dialog.dismiss();
                    register = false;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            int errorCode=e.getErrorCode();
                            if(errorCode==EMError.NETWORK_ERROR){
                                setError_text(getResources().getString(R.string.network_anomalies));
                            }else if(errorCode == EMError.USER_ALREADY_EXIST){
                                setError_text(getResources().getString(R.string.User_already_exists));
                            }else if(errorCode == EMError.USER_AUTHENTICATION_FAILED){
                                setError_text( getResources().getString(R.string.registration_failed_without_permission));
                            }else if(errorCode == EMError.USER_ILLEGAL_ARGUMENT){
                                setError_text(getResources().getString(R.string.illegal_user_name));
                            }else if(errorCode == EMError.EXCEED_SERVICE_LIMIT){
                            }else{
                                setError_text(getResources().getString(R.string.Registration_failed));
                            }
                            setBtnEnable(true);
                        }
                    });

                }
            }
        }).start();
    }

    /**
    登录IM账号
     */
    public void login() {
        Log.d(TAG, "EMClient.getInstance().login");
        if(!register){
            setProgressDialog();
        }
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");
                //登录成功进入会议房间
                if(currentRoomname == null || currentPassword == null){
                    setError_text("房间名不允许为空");
                    return;
                }
                joinRoom();
            }
            @Override
            public void onProgress(int progress, String status) {
                Log.d(TAG, "login: onProgress");
            }
            @Override
            public void onError(final int code, final String message) {
                Log.d(TAG, "login: onError: " + code);
                dialog.dismiss();
                register = false;
                runOnUiThread(new Runnable() {
                    public void run() {
                        setError_text(getString(R.string.Login_failed) + message);
                        setBtnEnable(true);
                    }
                });
            }
        });
    }

    /**
     加入会议室
     */
    private void joinRoom() {
        ConferenceInfo.getInstance().Init();
        if(conferenceSession.getConferenceProfiles() != null){
            conferenceSession.getConferenceProfiles().clear();
        }
        DemoHelper.getInstance().setGlobalListeners();
        EMRoomConfig roomConfig = new EMRoomConfig();
        roomConfig.setNickName(currentNickname);
        roomConfig.setRecord(PreferenceManager.getInstance().isRecordOnServer());
        roomConfig.setMergeRecord(PreferenceManager.getInstance().isMergeStream());
        if(PreferenceManager.getInstance().isPushCDN()){
            if(PreferenceManager.getInstance().getCDNUrl() != null){
                if(PreferenceManager.getInstance().getCDNUrl().length() > 0) {
                    String url = PreferenceManager.getInstance().getCDNUrl();
                    EMLiveConfig liveConfig = null;
                    EMCDNCanvas canvas = null;
                    if(PreferenceManager.getInstance().isPushAudioStream()){ //开启纯音频推流
                        canvas = new EMCDNCanvas(0,0, 0,30,900,"H264");
                        liveConfig = new EMLiveConfig(url, canvas);
                        EMAudioConfig audioConfig = new EMAudioConfig();
                        liveConfig.setAudioConfig(audioConfig);
                    }else{
                        canvas = new EMCDNCanvas(ConferenceInfo.CanvasWidth,ConferenceInfo.CanvasHeight, 0,30,900,"H264");
                        liveConfig = new EMLiveConfig(url, canvas);
                    }
                    roomConfig.setLiveConfig(liveConfig);
               }
           }
        }

        try {
            JSONObject extobject = new JSONObject();
            extobject.putOpt("headImage",PreferenceManager.getInstance().getCurrentUserAvatar());
            String extStr = extobject.toString();
            extStr = extStr.replace("\\","");
            roomConfig.setExt(extStr);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        EMClient.getInstance().conferenceManager().joinRoom(currentRoomname, currentPassword, conferenceRole,roomConfig, new EMValueCallBack<EMConference>(){
                    @Override
                    public void onSuccess(EMConference value) {
                        setError_text("");
                        EMLog.i(TAG, "join  conference success");
                        ConferenceInfo.getInstance().setRoomname(currentRoomname);
                        ConferenceInfo.getInstance().setPassword(currentPassword);
                        ConferenceInfo.getInstance().setCurrentrole(value.getConferenceRole());
                        ConferenceInfo.getInstance().setConference(value);

                        EMLog.i(TAG, "Get ConferenceId:"+ value.getConferenceId() + "conferenceRole :"+  conferenceRole + " role：" + value.getConferenceRole());
                        conferenceSession.setConfrId(value.getConferenceId());
                        conferenceSession.setConfrPwd(value.getPassword());
                        conferenceSession.setSelfUserId(username);
                        conferenceSession.setStreamParam(value);

                        Intent intent = new Intent(MainActivity.this, ConferenceActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onError(final int error, final String errorMsg) {
                        dialog.dismiss();
                        register = false;
                        EMLog.e(TAG, "join conference failed error " + error + ", msg " + errorMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setBtnEnable(true);
                                if(error == CALL_TALKER_ISFULL) {
                                    takerFullDialogDisplay();
                                }else{
                                    setError_text("Join conference failed " + error + " " + errorMsg);
                                }
                            }
                        });
                    }
                });
    }

    /**
     * 主播已满提示对话框
     */
    public void takerFullDialogDisplay() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(MainActivity.this, R.layout.activity_talker_full, null);
        dialog.setView(dialogView);


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        dialog.show();

        final Button btn_ok = dialogView.findViewById(R.id.btn_ok);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                dialog.dismiss();
                EMLog.e(TAG, "talker is full , join conference as Audience");
                conferenceRole = EMConferenceManager.EMConferenceRole.Audience;
                ConferenceInfo.getInstance().setCurrentrole(EMConferenceManager.EMConferenceRole.Audience);
                joinRoom();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //主播已满不加入会议
                EMLog.e(TAG, "talker is full , not join conference");
            }
        });
    }


    /**
     * 查询最新版本号 进行强制升级
     *
     */
    private void getLatestVersion() {
        new AsyncTask<String, Void, String>() {
            //该方法运行在后台线程中，因此不能在该线程中更新UI，UI线程为主线程
            @Override
            protected String doInBackground(String... params) {
                String headImage = null;
                try {
                    String url = params[0];
                    URL HttpURL = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) HttpURL.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    String line;

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    while((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    headImage = sb.toString();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return headImage;
            }

            //在doInBackground 执行完成后，onPostExecute 方法将被UI 线程调用，
            // 后台的计算结果将通过该方法传递到UI线程，并且在界面上展示给用户.
            @Override
            protected void onPostExecute(String ImageStr) {
                if(ImageStr != null){
                    try {
                        ImageStr = ImageStr.replace(" ","");
                        JSONObject object = new JSONObject(ImageStr);
                        JSONObject versionobj = object.optJSONObject("version");
                        String newverison = versionobj.optString("Android");
                        String currentversion = getCurrentVersion(getApplicationContext());
                        String versionName = "V";
                        versionName = versionName + currentversion;
                        version_view.setText(versionName);
                        if(!currentversion.equals(newverison)){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    final AlertDialog dialog = builder.create();
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.setCancelable(false);
                                    View dialogView = View.inflate(MainActivity.this, R.layout.activity_updata_version, null);
                                    TextView infoView = dialogView.findViewById(R.id.current_info_view);
                                    infoView.setText("检测到当前不是最新版本"+ "\n" + "为了不影响您正常使用" + "\n" + "请更新到最新版");
                                    Button okbtn = dialogView.findViewById(R.id.btn_update_ok);
                                    okbtn.setText("确定");
                                    dialog.setView(dialogView);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                                    wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
                                    dialog.show();
                                    final Button btn_ok = dialogView.findViewById(R.id.btn_update_ok);
                                    btn_ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view){
                                            EMLog.i(TAG, "getLatestVersion currentversion:"+ currentversion + "  newverison:" + newverison);
                                            finish();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            });
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.execute(url);
    }




    /**
     * 获取当前版本号
     * @return
     * @throws Exception
     */
    public static synchronized String getCurrentVersion(Context context) {
        String appVersionCode = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appVersionCode = packageInfo.versionName;
            } else {
                appVersionCode = packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionCode;
    }


    /**
     * 设置昵称提示
     */
    private  void  setNickNameDialogDisplay(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog nickName_dialog = builder.create();
        View dialogView = View.inflate(MainActivity.this, R.layout.activity_nickname_editshow, null);
        nickName_dialog.setView(dialogView);

        nickName_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = nickName_dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        nickName_dialog.setCanceledOnTouchOutside(false);
        nickName_dialog.setCancelable(false);
        nickName_dialog.show();

        final Button btn_ok = dialogView.findViewById(R.id.btn_ok_nickname);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel_nickname);
        final EditText editText = dialogView.findViewById(R.id.nickname_text);

        CharSequence text = editText.getText();
        //Debug.asserts(text instanceof Spannable);
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable)text;
            Selection.setSelection(spanText, text.length());
        }

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                runOnUiThread(new Runnable() {
                    public void run() {
                        currentNickname = editText.getText().toString().trim();
                        if(currentNickname.length() == 0){
                            setBtnEnable(true);
                            setError_text("昵称不允许为空");
                            nickName_dialog.dismiss();
                            showNickNameFlag = false;
                            if(dialog != null){
                                dialog.dismiss();
                            }
                        }else {
                            nickName_dialog.dismiss();
                            showNickNameFlag = false;
                            EMLog.e(TAG,"setting nickName  succeed  currentNickname:" + currentNickname);
                            PreferenceManager.getInstance().setCurrentUserNick(currentNickname);
                            if(username == null){
                                register();
                            }else{
                                password = PreferenceManager.getInstance().getCurrentUserPassWord();
                                login();
                            }
                        }
                    }
                });
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        setBtnEnable(true);
                        nickName_dialog.dismiss();
                        if(dialog != null){
                            dialog.dismiss();
                        }
                        //主播已满不加入会议
                        EMLog.e(TAG, "cancel setting nickename");
                        currentNickname = null;
                        showNickNameFlag = false;
                    }
                });
            }
        });
    }

    /**
    个人设置
     */
    public void personalSetting(View view){
        Intent intent = new Intent(MainActivity.this,
                SettingActivity.class);
        startActivity(intent);
    }

    /**
     * 禁止进入房间按钮操作
     */
    private void setBtnEnable(boolean enable){
        btn_anchor.setEnabled(enable);
    }

    /**
     * 设置错误提示信息
     */
    private void setError_text(String errorText){
        runOnUiThread(new Runnable() {
            public void run() {
                error_text_view.setText(errorText);
            }
        });
    }

    /**
     * 设置动态加载进度条
     *
     */
    void  setProgressDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.login_loading_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (null != intent) {
            Uri uri = intent.getData();
            if (null != uri) {
                EMLog.i(TAG, "uri-->" + "" + uri.getScheme()
                        + "-path->" + uri.getPath()
                        + "-roomName->" + uri.getQueryParameter("roomName"));
                String roomInfo = uri.getQueryParameter("roomName");
                roomInfo = URLDecoder.decode(roomInfo);
                if (roomInfo != null && roomInfo != "") {
                    roomnameEditText.setText(roomInfo);
                    addconference_anchor(roomnameEditText);
                }
            }
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
}
