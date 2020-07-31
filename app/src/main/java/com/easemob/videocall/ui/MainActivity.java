package com.easemob.videocall.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
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
import com.hyphenate.chat.EMLiveLayoutStyle;
import com.hyphenate.chat.EMMediaFormat;
import com.hyphenate.chat.EMRecordMediaFormat;
import com.hyphenate.chat.EMRoomConfig;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.easemob.videocall.DemoHelper;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hyphenate.EMError.CALL_TALKER_ISFULL;
import static com.hyphenate.EMError.GENERAL_ERROR;


/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class MainActivity extends Activity {
    private  final String TAG = this.getClass().getSimpleName();
    private EditText roomnameEditText;
    private EditText passwordEditText;
    private String username;
    private String currentRoomname;
    private String currentPassword;
    private String currentNickname;
    private EMConferenceManager.EMConferenceRole  conferenceRole;
    private String password = "123";
    final  private String regEx="[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？-]";
    private Button btn_anchor;
    private Button btn_audience;
    private TextView version_view;
    private ConferenceSession conferenceSession;
    private String url = "http://download-sdk.oss-cn-beijing.aliyuncs.com/downloads/RtcDemo/version.conf";
    private boolean showNickNameFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_anchor = (Button)findViewById(R.id.btn_anchor);
        btn_audience = (Button)findViewById(R.id.btn_audience);
        version_view = (TextView)findViewById(R.id.versionName_view);

        roomnameEditText = (EditText) findViewById(R.id.roomname);
        passwordEditText = (EditText) findViewById(R.id.password);
        roomnameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
        passwordEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});

        if(ConferenceInfo.getInstance().getRoomname() != null){
            roomnameEditText.setText(ConferenceInfo.getInstance().getRoomname());
        }
        if(ConferenceInfo.getInstance().getPassword() != null){
            passwordEditText.setText(ConferenceInfo.getInstance().getPassword());
        }
        EditText editText= (EditText)findViewById(R.id.roomname);
        editText.clearFocus();
        editText.setSelected(false);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        conferenceSession = DemoHelper.getInstance().getConferenceSession();

        //申请权限
        requestPermissions();

        getLatestVersion();

        setEditTextInhibitInputSpace(roomnameEditText);
        setEditTextInhibitInputSpace(passwordEditText);

    }


    /**
    主播加入会议房间
     */
    public void addconference_anchor(View view){
        if(showNickNameFlag){
            return;
        }
        getLatestVersion();
        setBtnEnable(false);
        currentRoomname = roomnameEditText.getText().toString().trim();
        currentPassword = passwordEditText.getText().toString().trim();
        if(currentRoomname.length() == 0 && currentPassword.length() == 0){
            Toast.makeText(getApplicationContext(), "房间名或密码不允许为空！", Toast.LENGTH_SHORT).show();
            setBtnEnable(true);
            return;
        }
        if(currentRoomname.length() < 3){
            Toast.makeText(getApplicationContext(), "房间名不能少于3位！", Toast.LENGTH_SHORT).show();
            setBtnEnable(true);
            return;
        }
        if(currentPassword.length() < 3){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "密码不能少于3位！", Toast.LENGTH_SHORT).show();
            return;
        }

        if(currentRoomname.length() > 18){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "房间名不能超过18位！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentPassword.length() > 18){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "房间密码不能超过18位！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isLegalChars(currentRoomname)){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "房间名不允许输入除数字、中文、英文、下划线或者减号以外的特殊字符!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isLegalChars(currentPassword)){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "密码不允许输入除数字、中文、英文、下划线或者减号以外的特殊字符!", Toast.LENGTH_SHORT).show();
            return;
        }

        conferenceRole = EMConferenceManager.EMConferenceRole.Talker;
        ConferenceInfo.getInstance().setCurrentrole(EMConferenceManager.EMConferenceRole.Talker);

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
    观众加入会议房间
     */
    public void addconference_audience(View view){
        getLatestVersion();
        setBtnEnable(false);
        currentRoomname = roomnameEditText.getText().toString().trim();
        currentPassword = passwordEditText.getText().toString().trim();
        if(currentRoomname.length() == 0 && currentPassword.length() == 0){
            Toast.makeText(getApplicationContext(), "房间名或密码不允许为空！", Toast.LENGTH_SHORT).show();
            setBtnEnable(true);
            return;
        }
        if(currentRoomname.length() < 3){
            Toast.makeText(getApplicationContext(), "房间名不能少于3位！", Toast.LENGTH_SHORT).show();
            setBtnEnable(true);
            return;
        }
        if(currentPassword.length() < 3){
            Toast.makeText(getApplicationContext(), "密码不能少于3位！", Toast.LENGTH_SHORT).show();
            setBtnEnable(true);
            return;
        }

        if(currentRoomname.length() > 18){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "房间名不能超过18位！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentPassword.length() > 18){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "房间密码不能超过18位！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isLegalChars(currentRoomname)){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "房间名不允许输入除数字、中文、英文、下划线或者减号以外的特殊字符!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isLegalChars(currentPassword)){
            setBtnEnable(true);
            Toast.makeText(getApplicationContext(), "密码不允许输入除数字、中文、英文、下划线或者减号以外的特殊字符!", Toast.LENGTH_SHORT).show();
            return;
        }

        conferenceRole = EMConferenceManager.EMConferenceRole.Audience;
        ConferenceInfo.getInstance().setCurrentrole(EMConferenceManager.EMConferenceRole.Audience);

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

    public static void setEditTextInhibitInputSpace(EditText editText){
        InputFilter filter=new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if(source.equals(" ")){
                    return "";
                }else {
                    return null;
                }
            }
        };

        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
    }


    /**
    自动注册一个账号
     */
    public void register(){
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
                    runOnUiThread(new Runnable() {
                        public void run() {
                            int errorCode=e.getErrorCode();
                            if(errorCode==EMError.NETWORK_ERROR){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            }else if(errorCode == EMError.USER_ALREADY_EXIST){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            }else if(errorCode == EMError.USER_AUTHENTICATION_FAILED){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            }else if(errorCode == EMError.USER_ILLEGAL_ARGUMENT){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name),Toast.LENGTH_SHORT).show();
                            }else if(errorCode == EMError.EXCEED_SERVICE_LIMIT){

                            }else{
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
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
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");
                //登录成功进入会议房间
                if(currentRoomname == null || currentPassword == null){
                    Toast.makeText(getApplicationContext(), "房间名和密码不允许为空",
                            Toast.LENGTH_SHORT).show();
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
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
                                Toast.LENGTH_SHORT).show();
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
        EMClient.getInstance().setDebugMode(true);
        ConferenceInfo.getInstance().Init();
        if(conferenceSession.getConferenceProfiles() != null){
            conferenceSession.getConferenceProfiles().clear();
        }
        DemoHelper.getInstance().setGlobalListeners();
        EMRoomConfig roomConfig = new EMRoomConfig();
        roomConfig.setNickName(currentNickname);
        roomConfig.setRecord(PreferenceManager.getInstance().isRecordOnServer());
        roomConfig.setMergeRecord(PreferenceManager.getInstance().isMergeStream());
//        roomConfig.setMaxVideoCount(2);
//        roomConfig.setMaxTalkerCount(3);
//        roomConfig.setMaxPubDesktopCount(1);
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
                        EMLog.e(TAG, "join conference failed error " + error + ", msg " + errorMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setBtnEnable(true);
                                if(error == CALL_TALKER_ISFULL) {
                                    takerFullDialogDisplay();
                                }else{
                                    Toast.makeText(getApplicationContext(), "Join conference failed " + error + " " + errorMsg, Toast.LENGTH_SHORT).show();
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
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(MainActivity.this, R.layout.activity_nickname_editshow, null);
        dialog.setView(dialogView);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

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
                            Toast.makeText(getApplicationContext(), "昵称不允许为空!",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            showNickNameFlag = false;
                        }else {
                            dialog.dismiss();
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
                        dialog.dismiss();
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
        btn_audience.setEnabled(enable);
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
