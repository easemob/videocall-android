package com.easemob.videocall.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.Config;
import com.easemob.videocall.utils.ConfigManager;
import com.easemob.videocall.utils.PermissionsManager;
import com.easemob.videocall.utils.PermissionsResultAction;
import com.easemob.videocall.utils.X5WebView;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.EMLog;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.utils.TbsLog;

import java.net.MalformedURLException;
import java.net.URL;

import static com.easemob.videocall.utils.ConferenceAttributeOption.WHITE_BOARD;
import static com.superrtc.mediamanager.EMediaManager.getContext;

public class WhiteBoardTbsActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    private String roomUrl;
    private String roomId;
    private TextView btn_back;
    private TextView btn_destory;
    private boolean creator;
    private X5WebView mWebView;
    private ViewGroup mViewParent;
    private boolean mNeedTestPage = false;

    private ValueCallback<Uri> uploadFile;
    private ValueCallback<Uri[]> uploadFiles;
    private URL mIntentUrl;

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
        EMLog.e(TAG,"WhiteBoardTbsActivity start");
        Bundle bundle = this.getIntent().getExtras();

        roomId = bundle.getString("roomId");
        roomUrl = bundle.getString("roomUrl");
        creator = bundle.getBoolean("creator");

        mId = bundle.getInt(ConferenceActivity.KEY_ID, -1);
        mAction = Config.ACTION_CONFIG_CHANGE + mId;

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(mAction));
        Config config = ConfigManager.getInstance().getConfig(mId);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Intent intent = getIntent();
        if (intent != null) {
            try {
                mIntentUrl = new URL(intent.getData().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            } catch (Exception e) {
            }
        }

        try {
            if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 11) {
                getWindow()
                        .setFlags(
                                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
        }

        setContentView(R.layout.activity_white_board);

        requestPermissions();
        initView();
    }


    private void initView(){
        btn_back = findViewById(R.id.btn_whiteboard_back);
        btn_back.setOnClickListener(this);
        btn_destory = findViewById(R.id.btn_whiteboard_destory);
        btn_destory.setOnClickListener(this);
        if(!creator){
            btn_destory.setVisibility(View.GONE);
        }

        mViewParent = (ViewGroup) findViewById(R.id.whiteboard_view_layout);

        //mTestHandler.sendEmptyMessageDelayed(MSG_INIT_UI, 10);
        mTestHandler.sendEmptyMessage(MSG_INIT_UI);
    }

    private void init() {

        mWebView = new X5WebView(this, null);

        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.FILL_PARENT,
                FrameLayout.LayoutParams.FILL_PARENT));


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
            public boolean onJsAlert(com.tencent.smtt.sdk.WebView arg0, String arg1, String arg2,
                                     com.tencent.smtt.export.external.interfaces.JsResult arg3) {
                return super.onJsAlert(null, arg1, arg2, arg3);
            }

            // For Android 3.0+
            public void openFileChooser(com.tencent.smtt.sdk.ValueCallback<Uri> uploadMsg, String acceptType) {
                EMLog.i(TAG, "openFileChooser 1");
                WhiteBoardTbsActivity.this.uploadFile = uploadFile;
                openFileChooseProcess();
            }

            // For Android < 3.0
            public void openFileChooser(com.tencent.smtt.sdk.ValueCallback<Uri> uploadMsgs) {
                EMLog.i(TAG, "openFileChooser 2");
                WhiteBoardTbsActivity.this.uploadFile = uploadFile;
                openFileChooseProcess();
            }

            // For Android  > 4.1.1
            public void openFileChooser(com.tencent.smtt.sdk.ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                EMLog.i(TAG, "openFileChooser 3");
                WhiteBoardTbsActivity.this.uploadFile = uploadFile;
                openFileChooseProcess();
            }

            // For Android  >= 5.0
            public boolean onShowFileChooser(com.tencent.smtt.sdk.WebView webView,
                                             com.tencent.smtt.sdk.ValueCallback<Uri[]> filePathCallback,
                                             com.tencent.smtt.sdk.WebChromeClient.FileChooserParams fileChooserParams) {
                EMLog.i(TAG, "openFileChooser 4:" + filePathCallback.toString());
                WhiteBoardTbsActivity.this.uploadFiles = filePathCallback;
                openFileChooseProcess();
                return true;
            }
        });

        mWebView.setDownloadListener(new com.tencent.smtt.sdk.DownloadListener() {

            @Override
            public void onDownloadStart(String arg0, String arg1, String arg2,
                                        String arg3, long arg4) {
                TbsLog.d(TAG, "url: " + arg0);
                new AlertDialog.Builder(WhiteBoardTbsActivity.this)
                        .setTitle("allow to download？")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Toast.makeText(
                                                WhiteBoardTbsActivity.this,
                                                "fake message: i'll download...",
                                                1000).show();
                                    }
                                })
                        .setNegativeButton("no",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        Toast.makeText(
                                                WhiteBoardTbsActivity.this,
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
                                                WhiteBoardTbsActivity.this,
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TbsLog.d(TAG, "onActivityResult, requestCode:" + requestCode
                + ",resultCode:" + resultCode);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FIREHOUSE_RESULT_CODE:
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
                     break;
                case 0:
                    if (null != uploadFile) {
                        Uri result = data == null || resultCode != RESULT_OK ? null
                                : data.getData();
                        uploadFile.onReceiveValue(result);
                        uploadFile = null;
                    }
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (null != uploadFile) {
                uploadFile.onReceiveValue(null);
                uploadFile = null;
            }
        }
    }

    private void openFileChooseProcess() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(i,FIREHOUSE_RESULT_CODE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null || mWebView == null || intent.getData() == null)
            return;
        mWebView.loadUrl(intent.getData().toString());
    }

    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                EMLog.i(TAG,"All permissions have been granted");
            }

            @Override
            public void onDenied(String permission) {
                EMLog.e(TAG,"Permission " + permission + " has been denied");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(WhiteBoardTbsActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(WhiteBoardTbsActivity.this,
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
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mTestHandler != null)
            mTestHandler.removeCallbacksAndMessages(null);
        if (mWebView != null)
            mWebView.destroy();
        super.onDestroy();
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
                    init();
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
