package com.easemob.videocall.utils;

import android.os.Parcel;
import android.os.Parcelable;
import com.hyphenate.media.EMCallSurfaceView;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class ConferenceMemberInfo implements Parcelable {
    private String userId;
    private String streamId;
    private EMCallSurfaceView videoView;
    private int mediaType;
    private boolean videoOff;
    private boolean audioOff;
    private boolean isDesktop;

    private boolean isWhiteboard = false;
    private String whiteboardPwd;
    private String whiteboardRoomName;

    public ConferenceMemberInfo(){}

    protected ConferenceMemberInfo(Parcel in) {
        userId = in.readString();
        streamId = in.readString();
        whiteboardPwd = in.readString();
        mediaType = in.readInt();
        videoOff = in.readByte() != 0;
        audioOff = in.readByte() != 0;
        isDesktop = in.readByte() != 0;
        isWhiteboard = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(streamId);
        dest.writeString(whiteboardPwd);
        dest.writeInt(mediaType);
        dest.writeByte((byte) (videoOff ? 1 : 0));
        dest.writeByte((byte) (audioOff ? 1 : 0));
        dest.writeByte((byte) (isDesktop ? 1 : 0));
        dest.writeByte((byte) (isWhiteboard ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ConferenceMemberInfo> CREATOR = new Creator<ConferenceMemberInfo>() {
        @Override
        public ConferenceMemberInfo createFromParcel(Parcel in) {
            return new ConferenceMemberInfo(in);
        }

        @Override
        public ConferenceMemberInfo[] newArray(int size) {
            return new ConferenceMemberInfo[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public EMCallSurfaceView getVideoView() {
        return videoView;
    }

    public void setVideoView(EMCallSurfaceView videoView) {
        this.videoView = videoView;
    }

    public boolean isVideoOff() {
        return videoOff;
    }

    public void setVideoOff(boolean videoOff) {
        this.videoOff = videoOff;
    }

    public boolean isAudioOff() {
        return audioOff;
    }

    public void setAudioOff(boolean audioOff) {
        this.audioOff = audioOff;
    }

    public boolean isDesktop() { return isDesktop; }

    public void setDesktop(boolean desktop) { isDesktop = desktop; }

    public boolean isWhiteboard() { return isWhiteboard; }

    public void setWhiteboard(boolean whiteboard) { isWhiteboard = whiteboard; }

    public String getWhiteboardPwd() { return whiteboardPwd;}

    public void setWhiteboardPwd(String whiteboardUrl) { this.whiteboardPwd = whiteboardUrl; }
}
