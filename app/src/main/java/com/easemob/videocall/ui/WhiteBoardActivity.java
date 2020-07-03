package com.easemob.videocall.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.Config;
import com.easemob.videocall.utils.ConfigManager;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMWhiteboard;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;

import static com.easemob.videocall.utils.ConferenceAttributeOption.WHITE_BOARD;
import static com.superrtc.mediamanager.EMediaManager.getContext;

public class WhiteBoardActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    private String roomUrl;
    private String roomId;
    private TextView btn_back;
    private TextView btn_destory;
    private boolean creator;

    public ValueCallback<Uri[]> mUploadMessage;

    public final static int FIREHOUSE_RESULT_CODE = 1;

    private int mId;
    private String mAction;

    // 实例化一个广播接收器 接受成为管理员的变化
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver () {
        public void onReceive (Context context, Intent intent){
            // TODO 接收到广播时的逻辑
            if (mAction.equals(intent.getAction())) {
                String[] changedParts = intent.getStringArrayExtra(Config.KEY_CHANGED_PARTS);
                Config config = ConfigManager.getInstance().getConfig(mId);
                String key = changedParts[0];
                String value = (String) config.get(getContext(), key);

                if(key.equals("destoryWhiteboard")){
                    //结束activity
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_white_board);
        Bundle bundle = this.getIntent().getExtras();

        roomId = bundle.getString("roomId");
        roomUrl = bundle.getString("roomUrl");
        creator = bundle.getBoolean("creator");

        mId = bundle.getInt(ConferenceActivity.KEY_ID, -1);
        mAction = Config.ACTION_CONFIG_CHANGE + mId;

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(mAction));
        Config config = ConfigManager.getInstance().getConfig(mId);
        initView();
    }

    private void initView(){
        btn_back = findViewById(R.id.btn_whiteboard_back);
        btn_back.setOnClickListener(this);
        btn_destory = findViewById(R.id.btn_whiteboard_destory);
        btn_destory.setOnClickListener(this);
//        btn_interact = findViewById(R.id.btn_whiteboard_interact);
//        btn_interact.setOnClickListener(this);

        if(!creator){
            btn_destory.setVisibility(View.GONE);
        }

        WebView webview = findViewById(R.id.whiteboard_view_layout);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 设置可以支持缩放
        webview.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        webview.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        webview.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        webview.loadUrl(roomUrl);

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                   runOnUiThread(new Runnable(){
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                EMLog.d("MyApplication", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, FileChooserParams fileChooserParams) {
                openFileChooser(uploadMsg, fileChooserParams);
                return true;
            }
        });
    }
    private void openFileChooser(ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
        mUploadMessage = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, FIREHOUSE_RESULT_CODE);
    }


    private void destoryWhiteboard(){
        EMClient.getInstance().conferenceManager().destroyWhiteboardRoom(EMClient.getInstance().getCurrentUser(), EMClient.getInstance().getAccessToken(),
                roomId, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EMLog.i(TAG, "createWhiteboardRoom success, roomId: " + ConferenceInfo.getInstance().getWhiteboard().getRoomId());
                                Toast.makeText(getApplicationContext(), "销毁白板  " + ConferenceInfo.getInstance().getRoomname() + "成功!", Toast.LENGTH_SHORT).show();

                                //销毁以后重置
                                ConferenceInfo.whiteboardCreator = false;
                                ConferenceInfo.getInstance().setWhiteboard(null);

                                EMClient.getInstance().conferenceManager().deleteConferenceAttribute(WHITE_BOARD, new EMValueCallBack<Void>() {
                                    @Override
                                    public void onSuccess(Void value) {
                                        EMLog.i(TAG, "deleteConferenceAttribute WHITE_BOARD success");
                                        finish();

                                    }
                                    @Override
                                    public void onError(int error, String errorMsg) {
                                        EMLog.i(TAG, "deleteConferenceAttribute WHITE_BOARD failed: " + error + ""  + errorMsg);
                                        finish();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(int code, String error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EMLog.i(TAG, "createWhiteboardRoom success, roomId: " + ConferenceInfo.getInstance().getWhiteboard().getRoomId());
                                Toast.makeText(getApplicationContext(), "销毁白板  " + ConferenceInfo.getInstance().getRoomname() + "失败:" + error + " !",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FIREHOUSE_RESULT_CODE) {
            if (null == mUploadMessage)
                return;
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            if (result != null) {
                mUploadMessage.onReceiveValue(new Uri[]{result});
            } else {
                mUploadMessage.onReceiveValue(new Uri[]{});
            }
            mUploadMessage = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_whiteboard_back:
                 finish();
                 //updateWhiteboard();
                 break;
            case R.id.btn_whiteboard_destory:
                showDestoryDialog();
                 break;
            default:
                 break;
        }
    }

    private void showDestoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(WhiteBoardActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(WhiteBoardActivity.this,
                R.layout.activity_talker_full_kick, null);
        TextView infoView = dialogView.findViewById(R.id.info_view);
        Button cancelbtn = dialogView.findViewById(R.id.btn_kick_cancel);
        Button okbtn = dialogView.findViewById(R.id.btn_kick_ok);
        infoView.setText("退出后，将退出互动白板！");
        cancelbtn.setText("取消");
        okbtn.setText("退出");
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
                EMLog.i(TAG, " derstory whiteboard");
                dialog.dismiss();
                destoryWhiteboard();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void updateWhiteboard(){
        EMClient.getInstance().conferenceManager().updateWhiteboardRoomWithRoomId(EMClient.getInstance().getCurrentUser(), roomId, EMClient.getInstance().getAccessToken(), true, null, true, new EMCallBack() {
            @Override
            public void onSuccess() {
                EMLog.i(TAG, " updateWhiteboard successed");
            }

            @Override
            public void onError(int code, String error) {
                EMLog.i(TAG, "updateWhiteboard failed");
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    @Override
    protected void onResume() {
        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
