package com.easemob.videocall.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.easemob.videocall.R;
import com.easemob.videocall.ui.widget.AvatarImageView;
import com.hyphenate.chat.EMClient;
import com.jaouan.compoundlayout.RadioLayout;

public class MultiMemberView extends RadioLayout {

	private Context context;
	private ImageView avatarView;
	private ImageView videoingView;
	private ImageView talkingView;
	private TextView nameView;

	private FrameLayout smallerVideoPreview;
	private boolean isVideoOff = true;
	private boolean isAudioOff = true;
	private String streamId;

	private String username;

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
		videoingView =  findViewById(R.id.icon_videoing);
		talkingView =  findViewById(R.id.icon_speaking);
		nameView =  findViewById(R.id.icon_text);
	}

	/**
	 * 更新静音状态
	 */
	public void setAudioOff(boolean state) {
		isAudioOff = state;
		if (isAudioOff) {
			talkingView.setBackgroundResource(R.drawable.call_mic_off);
		} else {
            talkingView.setBackgroundResource(R.drawable.call_mic_on);
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
			videoingView.setBackgroundResource(R.drawable.call_video_off);
			avatarView.setVisibility(VISIBLE);
		} else {
			videoingView.setBackgroundResource(R.drawable.call_video_on);
			avatarView.setVisibility(View.GONE);
		}
	}

	public boolean isVideoOff() {
		return isVideoOff;
	}


	/**
	 * 更新说话状态
	 */
	public void setTalking(boolean talking) {
		/*if (talking) {
			//talkingView.setVisibility(VISIBLE);
			//audioOffView.setVisibility(View.GONE);
		} else {
			talkingView.setVisibility(GONE);
		}*/
	}

	/**
	 * 设置当前 view 对应的 stream 的用户，主要用来语音通话时显示对方头像
	 */
	public void setUsername(String username) {
		this.username = username;

		String fristStr = username.substring(0,5);
		String lastStr =  username.substring(username.length()-5);
		fristStr = fristStr+"***"+lastStr;
		if(username  == EMClient.getInstance().getCurrentUser()){
			nameView.setText(fristStr + " (我)");
		}else{
			nameView.setText(fristStr);
		}
	}

	public String getUsername(){
		return username;
	}


	public ImageView getAvatarImageView(){
		return avatarView;
	}


	public FrameLayout getSurfaceViewContainer() {
		return smallerVideoPreview;
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
