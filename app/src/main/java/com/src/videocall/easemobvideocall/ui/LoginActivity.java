package com.src.videocall.easemobvideocall.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.src.videocall.easemobvideocall.R;
import com.src.videocall.easemobvideocall.utils.ConferenceInfo;
import com.src.videocall.easemobvideocall.utils.PreferenceManager;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private  final String TAG = this.getClass().getSimpleName();
    public static final int REQUEST_CODE_SETNICK = 1;
    private EditText roomnameEditText;
    private EditText passwordEditText;
    private String username;
    private String currentRoomname;
    private String currentPassword;
    private String accessToken;
    private boolean istalkerfull_access = true;
    private EMConferenceManager.EMConferenceRole  conferenceRole;
    private String password = "123";
    final  private String regEx="[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？-]";

    AtomicBoolean isQuit = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roomnameEditText = (EditText) findViewById(R.id.roomname);
        passwordEditText = (EditText) findViewById(R.id.password);

        EditText editText= (EditText)findViewById(R.id.roomname);
        editText.clearFocus();
        editText.setSelected(false);
    }

    /*
    主播加入会议房间
     */
    public void addconference_anchor(View view){
        currentRoomname = roomnameEditText.getText().toString().trim();
        currentPassword = passwordEditText.getText().toString().trim();
        if(currentRoomname.length() == 0 && currentPassword.length() == 0){
            Toast.makeText(getApplicationContext(), "房间名或密码不允许为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentRoomname.length() < 3){
            Toast.makeText(getApplicationContext(), "房间名不能少于3位！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentPassword.length() < 3){
            Toast.makeText(getApplicationContext(), "密码不能少于3位！", Toast.LENGTH_SHORT).show();
            return;
        }

        ConferenceInfo.getInstance().setCurrentrole(EMConferenceManager.EMConferenceRole.Talker);
        conferenceRole = ConferenceInfo.getInstance().getCurrentrole();

        username = PreferenceManager.getInstance().getCurrentUsername();
        if(username == null){
            register(view);
        }else{
            password = PreferenceManager.getInstance().getCurrentUserPassWord();
            login(view);
        }
    }

    /*
    观众加入会议房间
     */
    public void addconference_audience(View view){
        currentRoomname = roomnameEditText.getText().toString().trim();
        currentPassword = passwordEditText.getText().toString().trim();

        if(currentRoomname.length() == 0 && currentPassword.length() == 0){
            Toast.makeText(getApplicationContext(), "房间名或密码不允许为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentRoomname.length() < 3){
            Toast.makeText(getApplicationContext(), "房间名不能少于3位！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentPassword.length() < 3){
            Toast.makeText(getApplicationContext(), "密码不能少于3位！", Toast.LENGTH_SHORT).show();
            return;
        }

        ConferenceInfo.getInstance().setCurrentrole(EMConferenceManager.EMConferenceRole.Audience);
        conferenceRole = ConferenceInfo.getInstance().getCurrentrole();
        username = PreferenceManager.getInstance().getCurrentUsername();
        if(username == null){
            register(view);
        }else{
            password = PreferenceManager.getInstance().getCurrentUserPassWord();
            login(view);
        }
    }


    /*
    自动注册一个账号
     */
    public void register(View view){
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
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //注册成功进行登录
                            PreferenceManager.getInstance().setCurrentUserName(username);
                            PreferenceManager.getInstance().setCurrentuserPassword(password);
                            login(view);
                        }
                    });
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
                        }
                    });
                }
            }
        }).start();
    }

    /*
    登录IM账号
     */
    public void login(View view) {
        // call login method
        Log.d(TAG, "EMClient.getInstance().login");
        //username = "154999cac4544bfa82c8fd36f65e5175";
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");
                accessToken = EMClient.getInstance().getAccessToken();
                //登录成功进入会议房间
                if(currentRoomname == null || currentPassword == null){
                    Toast.makeText(getApplicationContext(), "房间名和密码不允许为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                joinConference();
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
                    }
                });
            }
        });
    }

    /*
     加入一个聊天会议室
     */
    private void joinConference() {

        EMClient.getInstance().conferenceManager().enableStatistics(true);
        EMClient.getInstance().conferenceManager().set(accessToken,EMClient.getInstance().getOptions().getAppKey()  ,username);
        EMClient.getInstance().conferenceManager().createAndJoinMultiConference(username, accessToken, conferenceRole, EMConferenceManager.EMConferenceType.SmallCommunication,
                currentRoomname, currentPassword, null, new EMValueCallBack<EMConference>(){
                    @Override
                    public void onSuccess(EMConference value) {
                         //takerFullDialogDisplay();
                         ConferenceInfo.getInstance().setRoomname(currentRoomname);
                         ConferenceInfo.getInstance().setPassword(currentPassword);
                         ConferenceInfo.getInstance().setConference(value);

                         Intent intent = new Intent(LoginActivity.this, ConferenceActivity.class);
                         startActivity(intent);
                         finish();
                    }
                    @Override
                    public void onError(final int error, final String errorMsg) {
                        EMLog.e(TAG, "join conference failed error " + error + ", msg " + errorMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(error == EMError.TALKER_IS_FULL)
                                {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(LoginActivity.this, R.layout.activity_talker_full, null);
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
                istalkerfull_access = true;
                dialog.dismiss();

                Intent intent = new Intent(LoginActivity.this, ConferenceActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                istalkerfull_access = false;
                dialog.dismiss();

                //主播已满不加入会议
                EMLog.e(TAG, "talker is full , not join conference");
                EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
                    @Override
                    public void onSuccess(Object value) {
                        EMLog.e(TAG, "exit conference scuessed");
                    }
                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.e(TAG, "exit conference failed " + error + ", " + errorMsg);
                    }
                });
            }
        });

    }


    /*
    个人设置
     */
    public void personalSetting(View view){
        Intent intent = new Intent(LoginActivity.this,
                SettingActivity.class);
        startActivity(intent);
    }
}
