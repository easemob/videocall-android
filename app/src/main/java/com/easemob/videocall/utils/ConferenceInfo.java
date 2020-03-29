package com.easemob.videocall.utils;
import android.util.Log;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.util.EasyUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/15/2020
 */

public class ConferenceInfo {
    private String roomname = null;
    private String password = null;
    private EMConference conference;

    private static ConferenceInfo conferenceInfo = null;
    private List<EMConferenceStream> streamList = new ArrayList<>();
    private EMConferenceStream localStream = new EMConferenceStream();
    private EMConferenceManager.EMConferenceRole conferenceRole;
    private List<EMConferenceMember> memberList;
    private List<String> adminsList;

    public static boolean Initflag = false;
    static public ConferenceInfo getInstance(){
        if(conferenceInfo == null){
            synchronized (ConferenceInfo.class){
                if(conferenceInfo == null){
                    conferenceInfo = new ConferenceInfo();
                }
            }
        }
        return conferenceInfo;
    }

    private ConferenceInfo(){

    }

    public void Init(){
        Initflag = false;
        streamList.clear();
        if(memberList != null){
            memberList.clear();
         }
        if(adminsList != null){
           adminsList.clear();
        }
    }

    public EMConferenceManager.EMConferenceRole getCurrentrole(){
        return conferenceRole;
    }

    public void setCurrentrole(EMConferenceManager.EMConferenceRole role){
         conferenceRole = role;
    }

    public EMConferenceStream getLocalStream(){
        return localStream;
    }

    public List<EMConferenceMember> getConferenceMemberList(){
        if(memberList == null){
            memberList = new ArrayList<>();
        }
        return memberList;
    }

    public EMConferenceMember getConferenceMemberInfo(String memberName){
        if (memberList == null || memberName == null || memberList.isEmpty()) {
            return null;
        }
        String memName = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), memberName);
        for (EMConferenceMember memberInfo : memberList){
            if (memberInfo.memberName.equals(memName)){
                return memberInfo;
            }
        }
        return null;
    }

    public EMConferenceMember getConferenceStream(String streamId){
        if (memberList == null || streamId == null || memberList.isEmpty()) {
            return null;
        }
        for (EMConferenceMember memberInfo : memberList){
            if (memberInfo.memberId.equals(streamId)){
                return memberInfo;
            }
        }
        return null;
    }

    public EMConferenceStream getConferenceSpeakStream(String streamId){
        if (streamId == null || streamList == null || streamList.isEmpty()) {
            return null;
        }
        for (EMConferenceStream streamInfo : streamList){
            if (streamInfo.getStreamId().equals(streamId)){
                return streamInfo;
            }
        }
        return null;
    }

    public List<EMConferenceStream> getConferenceStreamList(){
        return  streamList;
    }


    public String getRoomname() {
        Log.e("tag", "get name = "+roomname + " class = "+this);
        return roomname;
    }

    public void setRoomname(String roomname) {
        Log.e("tag", "set name = "+roomname + " class = "+this);
        this.roomname = roomname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public EMConference getConference() {
        return conference;
    }

    public void setConference(EMConference conference) {
        this.conference = conference;
    }

    public List<String> getAdmins(){
        String[] admins = ConferenceInfo.getInstance().getConference().getAdmins();
        String adminStr = "";
        if(admins != null){
            if(admins.length > 0){
                for (int i = 0; i < admins.length; i++) {
                    if(adminsList == null){
                        adminsList = new ArrayList<>();
                    }
                    adminStr = admins[i];
                    adminStr = EasyUtils.useridFromJid(adminStr);
                    if(!adminsList.contains(adminStr)) {
                        adminsList.add(adminStr);
                    }
                }
            }
        }else{
            if(adminsList == null){
                adminsList = new ArrayList<>();
            }
        }
        return adminsList;
    }
}
