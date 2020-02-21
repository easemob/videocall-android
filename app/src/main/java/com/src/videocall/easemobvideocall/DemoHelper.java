package com.src.videocall.easemobvideocall;


import android.content.Context;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;


import com.src.videocall.easemobvideocall.db.DemoDBManager;

import com.src.videocall.easemobvideocall.db.UserDao;
import com.src.videocall.easemobvideocall.utils.PreferenceManager;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoHelper {
    /**
     * data sync listener
     */
    public interface DataSyncListener {
        /**
         * sync complete
         * @param success true：data sync successful，false: failed to sync data
         */
        void onSyncComplete(boolean success);
    }

    protected static final String TAG = "DemoHelper";

    /**
     * EMEventListener
     */
    protected EMMessageListener messageListener = null;

	private static DemoHelper instance = null;
	
	private DemoModel demoModel = null;

	private String username;

    private ExecutorService executor;

    protected android.os.Handler handler;

	private final String KEY_REST_URL = "rest_url";

	private String restUrl;

    Queue<String> msgQueue = new ConcurrentLinkedQueue<>();

	private DemoHelper() {
        executor = Executors.newCachedThreadPool();
	}

	public synchronized static DemoHelper getInstance() {
		if (instance == null) {
			instance = new DemoHelper();
		}
		return instance;
	}

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

	/**
	 * init helper
	 * 
	 * @param context
	 *            application context
	 */
	public void init(Context context) {
	    demoModel = new DemoModel(context);
	    EMOptions options = initChatOptions(context);
	    //wss://rtc-turn4-hsb.easemob.com/ws   //a1-hsb.easemob.com  //rtc-turn4-hsb.easemob.com  a1.easemob.com
		//沙箱 Rtcserver
		//turn4 Rtcserver
		//options.enableDNSConfig(true);
        options.setRestServer("a1-hsb.easemob.com"); //沙箱地址
        options.setIMServer("39.107.54.56");
        //options.enableDNSConfig(true);
        options.setImPort(6717);
		//options.setRtcServer("wss://rtc-turn4-hsb.easemob.com/ws");
		//options.setRtcServer("a1-hsb.easemob.com"); 沙箱时候注释掉
		PreferenceManager.init(context);

		EMClient.getInstance().init(context, options);
        //EMClient.getInstance().setDebugMode(true);
	}


    private EMOptions initChatOptions(Context context){
        Log.d(TAG, "init HuanXin Options");

		EMOptions options = new EMOptions();
        // set if accept the invitation automatically
        options.setAcceptInvitationAlways(false);
        // set if you need read ack
        options.setRequireAck(true);
        // set if you need delivery ack
        options.setRequireDeliveryAck(false);

        return options;
    }




	/**
	 * if ever logged in
	 * 
	 * @return
	 */
	public boolean isLoggedIn() {
		return EMClient.getInstance().isLoggedInBefore();
	}

	/**
	 * logout
	 * 
	 * @param unbindDeviceToken
	 *            whether you need unbind your device token
	 * @param callback
	 *            callback
	 */
	public void logout(boolean unbindDeviceToken, final EMCallBack callback) {
		Log.d(TAG, "logout: " + unbindDeviceToken);
		EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "logout: onSuccess");
			    reset();
				if (callback != null) {
					callback.onSuccess();
				}
			}
			@Override
			public void onProgress(int progress, String status) {
				if (callback != null) {
					callback.onProgress(progress, status);
				}
			}
			@Override
			public void onError(int code, String error) {
				Log.d(TAG, "logout: onSuccess");
                reset();
				if (callback != null) {
					callback.onError(code, error);
				}
			}
		});
	}

	public DemoModel getModel(){
        return (DemoModel) demoModel;
    }

    
    /**
     * set current username
     * @param username
     */
    public void setCurrentUserName(String username){
    	this.username = username;
    	//demoModel.setCurrentUserName(username);
    }
    
    /**
     * get current user's id
     */
    public String getCurrentUsernName(){
    	if(username == null){
    		//username = demoModel.getCurrentUsernName();
    	}
    	return username;
    }


    synchronized void reset(){
        DemoDBManager.getInstance().closeDB();
    }
}
