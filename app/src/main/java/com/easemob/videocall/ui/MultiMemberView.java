package com.easemob.videocall.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.PreferenceManager;
import com.easemob.videocall.utils.StringUtils;
import com.hyphenate.chat.EMClient;
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

	private final String TAG = this.getClass().getSimpleName();

	private Context context;
	private ImageView avatarView;
	private ImageView admin_show_view;
	private TextView nameView;
	private ImageView speak_show_view;
	private ImageView whiteboardView;
	private LinearLayout loading_dialog;


	private FrameLayout smallerVideoPreview;
	private boolean isVideoOff = true;
	private boolean isAudioOff = true;
	private String streamId;

	private String username;
	private String headImage;
	private String url;
	private String nickname;
	private boolean isDesktop = false;

	private boolean isWhiteboard = false;
	private String whiteboardRoomName;
	private String whiteboardPwd;
	private boolean iswhiteboardCreator;
	ValueAnimator animator;

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
		avatarView  =   findViewById(R.id.call_avatar);
		admin_show_view =  findViewById(R.id.admin_show_view);
		nameView =  findViewById(R.id.icon_text);
		speak_show_view = findViewById(R.id.speaking_show_view);
		loading_dialog = findViewById(R.id.member_loading);

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
	}

    /**
	 * 更新静音状态
	 */
	public void setAudioOff(boolean state) {
		isAudioOff = state;
		animator.cancel();
		if (isDesktop) {
			return;
		}
		if (isAudioOff) {
			speak_show_view.setImageResource(R.drawable.em_speak_off);
		} else {
			speak_show_view.setImageResource(R.drawable.em_speak_on);
		}
	}

	public void setDesktop(boolean desktop) {
		isDesktop = desktop;
		if (isDesktop) {
			avatarView.setVisibility(View.GONE);
		}
	}

	public void setAudioSpeak(){
		animator.cancel();
		speak_show_view.setImageResource(R.drawable.em_voice_change);
		animator.start();
	}

	public void setAudioNoSpeak(){
		animator.cancel();
		if (isAudioOff) {
			speak_show_view.setImageResource(R.drawable.em_call_mic_off);
		}else {
			speak_show_view.setImageResource(R.drawable.em_call_mic_on);
		}
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (checked) {
			avatarView.setVisibility(VISIBLE);
			this.setBackgroundColor(Color.rgb(102,102,103));
			smallerVideoPreview.setVisibility(View.GONE);
		}else{
			if (isVideoOff()){
				avatarView.setVisibility(VISIBLE);
				smallerVideoPreview.setVisibility(View.GONE);
				this.setBackgroundColor(Color.rgb(102,102,103));
			}else{
				smallerVideoPreview.setVisibility(View.VISIBLE);
				avatarView.setVisibility(GONE);
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
			smallerVideoPreview.setVisibility(View.GONE);
		} else{
			avatarView.setVisibility(View.GONE);
			smallerVideoPreview.setVisibility(View.VISIBLE);
		}
	}

	public boolean isVideoOff() {
		return isVideoOff;
	}

	/**
	 * 设置当前 view 对应的 stream 的用户，主要用来语音通话时显示对方头像
	 */
	public void setUsername(String username,boolean showloading) {
		this.username = username;
		if(showloading && !username.equals(EMClient.getInstance().getCurrentUser())){
                loading_dialog.setVisibility(VISIBLE);
        }

		if(ConferenceInfo.getInstance().getAdmins().contains(username)){
			admin_show_view.setVisibility(VISIBLE);
		}else{
			admin_show_view.setVisibility(GONE);
		}
	}

	public String getUsername(){
		return username;
	}

	public void setNickname(String nickname){
		if(nickname != null){
			this.nickname = nickname;
			if(username.equals(EMClient.getInstance().getCurrentUser())){
				nameView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),6));
			}else{
				nameView.setText(StringUtils.tolongNickName(nickname,6));
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


	/**
	 * 设置动态loading
	 */
	public void cancelLoadingDialog(){
		loading_dialog.setVisibility(INVISIBLE);
	}

	public boolean isWhiteboard() {
		return isWhiteboard;
	}

	public void setWhiteboard(boolean whiteboard) {
		isWhiteboard = whiteboard;
	}

	public void setWhiteboardRoomId(String whiteboardRoomId) {
		this.whiteboardRoomName = whiteboardRoomId;
	}

	public void setIswhiteboardCreator(boolean iswhiteboardCreator) {
		this.iswhiteboardCreator = iswhiteboardCreator;
	}

	public void setWhiteboardPassword(String whiteboardPwd){
      	this.whiteboardPwd = whiteboardPwd;
	}
}
