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

    public ConferenceMemberInfo(){}

    protected ConferenceMemberInfo(Parcel in) {
        userId = in.readString();
        streamId = in.readString();
        mediaType = in.readInt();
        videoOff = in.readByte() != 0;
        audioOff = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(streamId);
        dest.writeInt(mediaType);
        dest.writeByte((byte) (videoOff ? 1 : 0));
        dest.writeByte((byte) (audioOff ? 1 : 0));
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
}
