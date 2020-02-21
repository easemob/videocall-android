package com.src.videocall.easemobvideocall.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import  com.hyphenate.media.EMCallSurfaceView;
import com.src.videocall.easemobvideocall.R;
import com.superrtc.sdk.VideoView;


/**
 * Created by lzan13 on 2017/8/21.
 * 多人会议显示控件
 */
public class ConferenceMemberView extends RelativeLayout {

    private Context context;

    private boolean isVideoOff = true;
    private boolean isAudioOff = false;
    private String streamId;

    public ConferenceMemberView(Context context) {
        this(context, null);
    }

    public ConferenceMemberView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConferenceMemberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        View.inflate(context, R.layout.activity_conference_member_view, this);
    }
}
