/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.videocall;

import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.hyphenate.util.EMLog;
import com.easemob.videocall.utils.ConferenceInfo;
import com.tencent.smtt.sdk.QbSdk;
/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */


public class DemoApplication extends Application implements Thread.UncaughtExceptionHandler {
	public static Context applicationContext;
	private static DemoApplication instance;
	static public ConferenceInfo conferenceInstance;
	static public String baseurl = "https://download-sdk.oss-cn-beijing.aliyuncs.com/downloads/RtcDemo/headImage/";
	static public String meeting_share_baseurl = "http://rtc-turn4-hsb.easemob.com/rtc-ws/meeting-share-loading-page/index.html?";

	/**
	 * nickname for current user, the nickname instead of ID be shown when user receive notification from APNs
	 */
	@Override
	public void onCreate() {
		MultiDex.install(this);
		super.onCreate();
        applicationContext = this;
        instance = this;

		//init demo helper
        DemoHelper.getInstance().init(applicationContext);

		conferenceInstance = ConferenceInfo.getInstance();

		addErrorListener();

		//搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
		QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
			@Override
			public void onViewInitFinished(boolean arg0) {
				// TODO Auto-generated method stub
				//x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
				Log.d("app", " onViewInitFinished is " + arg0);
			}

			@Override
			public void onCoreInitFinished() {
				// TODO Auto-generated method stub
			}
		};
		//x5内核初始化接口
		QbSdk.initX5Environment(getApplicationContext(),  cb);
	}

	private void addErrorListener() {
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public static DemoApplication getInstance() {
		return instance;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		EMLog.e("uncaughtException : ", e.getMessage());
		System.exit(1);
		Process.killProcess(Process.myPid());
	}
}
