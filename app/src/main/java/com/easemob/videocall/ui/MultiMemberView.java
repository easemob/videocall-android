package com.easemob.videocall.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceManager;
import com.jaouan.compoundlayout.RadioLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class MultiMemberView extends RadioLayout {

	private Context context;
	private ImageView avatarView;
	private ImageView talkingView;
	private TextView nameView;
	private ImageView adminshowView;

	private FrameLayout smallerVideoPreview;
	private boolean isVideoOff = true;
	private boolean isAudioOff = true;
	private String streamId;

	private String username;
	private String headImage;
	private String url;
	private String nickname;
	private boolean isDesktop = false;

	public MultiMemberView(Context context) {
		this(context, null);
	}

	public MultiMemberView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.activity_conference_member_view, this);
		init();
	}

	private void init() {
		smallerVideoPreview = findViewById(R.id.small_preview);
		avatarView =   findViewById(R.id.call_avatar);
		talkingView =  findViewById(R.id.icon_speaking);
		nameView =  findViewById(R.id.icon_text);
		adminshowView = findViewById(R.id.admin_show_image);
	}

	/**
	 * 更新静音状态
	 */
	public void setAudioOff(boolean state) {
		isAudioOff = state;
		if (isDesktop) {
			return;
		}
		if (isAudioOff) {
			talkingView.setVisibility(VISIBLE);
			talkingView.setBackgroundResource(R.drawable.call_mute_small);
		} else {
			talkingView.setVisibility(GONE);
		}
	}

	public void setDesktop(boolean desktop) {
		isDesktop = desktop;
		if (isDesktop) {
			avatarView.setVisibility(View.GONE);
		}
	}

	public void setAudioSpeak(){
		talkingView.setVisibility(VISIBLE);
		talkingView.setBackgroundResource(R.drawable.call_unmute_smal);
	}

	public void setAudioNoSpeak(){
		if (isAudioOff) {
			talkingView.setVisibility(VISIBLE);
			talkingView.setBackgroundResource(R.drawable.call_mute_small);
		}else {
			talkingView.setVisibility(GONE);
		}
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (checked) {
			avatarView.setVisibility(VISIBLE);
			smallerVideoPreview.setVisibility(View.GONE);
		}else{
			avatarView.setVisibility(GONE);
			if (isVideoOff()){
				smallerVideoPreview.setVisibility(View.GONE);
				avatarView.setVisibility(VISIBLE);
			}else{
				smallerVideoPreview.setVisibility(View.VISIBLE);
			}
		}
	}

	public boolean isAudioOff() {
		return isAudioOff;
	}

	/**
	 * 更新视频显示状态
	 */
	public void setVideoOff(boolean state) {
		isVideoOff = state;
		if (isVideoOff) {
			avatarView.setVisibility(VISIBLE);
		} else{
			avatarView.setVisibility(View.GONE);
		}
	}

	public boolean isVideoOff() {
		return isVideoOff;
	}

	/**
	 * 设置当前 view 对应的 stream 的用户，主要用来语音通话时显示对方头像
	 */
	public void setUsername(String username) {
		this.username = username;
		if(ConferenceInfo.getInstance().getAdmins().contains(username)){
				adminshowView.setVisibility(VISIBLE);
		}else{
				adminshowView.setVisibility(GONE);
		}
	}

	public String getUsername(){
		return username;
	}

	public void setNickname(String nickname){
		if(nickname != null){
			this.nickname = nickname;
			if(username == EMClient.getInstance().getCurrentUser()){
				nameView.setText(nickname);
			}else{
				nameView.setText(nickname);
			}
		}else{
			nameView.setText(null);
		}
	}

	public void setHeadImage(String headImage){
		try {
	    if(EMClient.getInstance().getCurrentUser().equals(username))	{
			this.headImage = headImage;
		}else {
			JSONObject object = new JSONObject(headImage);
			this.headImage = object.optString("headImage");
		}
		url = DemoApplication.baseurl;
		url = url + this.headImage;

		//加载头像图片
		loadImage();
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public String  getHeadImage(){
		return headImage;
	}

	public ImageView getAvatarImageView(){
		return avatarView;
	}


	public FrameLayout getSurfaceViewContainer() {
		return smallerVideoPreview;
	}

	/**
	 * 获取网落图片资源
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
				if(bitmap != null){
					avatarView.setImageBitmap(bitmap);
				}
			}
		}.execute(url);
	}

	/**
	 * 设置当前控件显示的 Stream Id
	 */
	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public String getStreamId() {
		return streamId;
	}

	private int dp2px(float dpValue){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
	}
}
