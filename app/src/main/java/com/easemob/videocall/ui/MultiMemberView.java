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
import com.jaouan.compoundlayout.RadioLayout;

public class MultiMemberView extends RadioLayout {

	private Context context;
	private AvatarImageView avatarView;
	private ImageView audioOffView;
	private ImageView talkingView;
	private TextView nameView;

	private FrameLayout smallerVideoPreview;
	private boolean isVideoOff = true;
	private boolean isAudioOff = false;
	private String streamId;

	private String username;

	public MultiMemberView(Context context) {
		this(context, null);
	}

	public MultiMemberView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.em_widget_multi_member_view, this);
		init();
	}

	private void init() {
		smallerVideoPreview = findViewById(R.id.mp_call_small_preview);
		avatarView =  findViewById(R.id.img_call_avatar);
		audioOffView =  findViewById(R.id.icon_mute);
		talkingView =  findViewById(R.id.icon_talking);
		nameView =  findViewById(R.id.text_name);
	}

	/**
	 * 更新静音状态
	 */
	public void setAudioOff(boolean state) {
		isAudioOff = state;
		if (isAudioOff) {
			audioOffView.setVisibility(View.VISIBLE);
			talkingView.setVisibility(View.GONE);
		} else {
			audioOffView.setVisibility(View.GONE);
		}
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (checked) {
			avatarView.setShowBoarder(true);
			smallerVideoPreview.setVisibility(View.GONE);
		} else {
			avatarView.setShowBoarder(false);
//			avatarView.setBorderWidth(0);
			if (isVideoOff()){
				smallerVideoPreview.setVisibility(View.GONE);
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
			smallerVideoPreview.setVisibility(View.GONE);
		} else {
			if (isChecked()){
				smallerVideoPreview.setVisibility(View.GONE);
			}else{
				smallerVideoPreview.setVisibility(View.VISIBLE);
			}
		}
	}

	public boolean isVideoOff() {
		return isVideoOff;
	}


	/**
	 * 更新说话状态
	 */
	public void setTalking(boolean talking) {
		if (talking) {
			talkingView.setVisibility(VISIBLE);
			audioOffView.setVisibility(View.GONE);
		} else {
			talkingView.setVisibility(GONE);
		}
	}

	/**
	 * 设置当前 view 对应的 stream 的用户，主要用来语音通话时显示对方头像
	 */
	public void setUsername(String username) {
		this.username = username;
//		EaseUserUtils.setUserAvatar(context, username, avatarView);
		//AvatarUtils.setAvatarContent(getContext(), username, avatarView);
		//nameView.setText(AppHelper.getInstance().getModel().getUserExtInfo(username).getNick());
		nameView.setText("grgwrgwr");

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
