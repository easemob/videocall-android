package com.easemob.videocall.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.hyphenate.chat.EMConference;

import java.util.ArrayList;
import java.util.List;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class ConferenceSession implements Parcelable {
    private String selfUserId;
    private EMConference mConferenceParam;
    private List<ConferenceMemberInfo> memberInfoList = null;
    private String confrId;
    private String confrPwd;

    public ConferenceSession(){}

    private ConferenceSession(Parcel in) {
        selfUserId = in.readString();
        memberInfoList = in.readArrayList(ArrayList.class.getClassLoader());
        confrId = in.readString();
        confrPwd = in.readString();
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

    public EMConference getStreamParam() {
        return mConferenceParam;
    }

    public void setStreamParam(EMConference streamParam) {
        mConferenceParam = streamParam;
    }

    public String getSelfUserId() {
        return selfUserId;
    }

    public void setSelfUserId(String selfUserId) {
        this.selfUserId = selfUserId;
    }

    public List<ConferenceMemberInfo> getConferenceProfiles() {
        return memberInfoList;
    }

    public ConferenceMemberInfo getConferenceMemberInfo(String userId){
        if (userId == null || memberInfoList == null || memberInfoList.isEmpty()) {
            return null;
        }
        for (ConferenceMemberInfo userProfile : memberInfoList){
            if (userProfile.getUserId().equals(userId)){
                return userProfile;
            }
        }
        return null;
    }

    public ConferenceMemberInfo getConferenceMemberByStreamId(String streamId){
        if (streamId == null || memberInfoList == null || memberInfoList.isEmpty()) {
            return null;
        }
        for (ConferenceMemberInfo userProfile : memberInfoList){
            if (userProfile.getStreamId().equals(streamId)){
                return userProfile;
            }
        }
        return null;
    }

    public void setConferenceProfiles(List<ConferenceMemberInfo> callUserProfiles) {
        memberInfoList = callUserProfiles;
    }

    public String getConfrId() {
        return confrId;
    }

    public void setConfrId(String confrId) {
        this.confrId = confrId;
    }

    public String getConfrPwd() {
        return confrPwd;
    }

    public void setConfrPwd(String confrPwd) {
        this.confrPwd = confrPwd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(selfUserId);
        dest.writeString(confrId);
        dest.writeString(confrPwd);
    }
}
