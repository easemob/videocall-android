package com.easemob.videocall.utils;
import android.util.Log;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMWhiteboard;
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
    private List<EMConferenceStream> talkerList = new ArrayList<>();
    private EMConferenceStream localStream = new EMConferenceStream();
    private EMConferenceManager.EMConferenceRole conferenceRole;
    private List<EMConferenceMember> memberList;
    private List<String> adminsList;
    static public  String localNomalStreamId = null;
    static public  String localDeskStreamId = null;
    static public int rzorderTop = 1;

    private WhiteBoardRoomInfo whiteboardRoomInfo = null;

    static public boolean whiteboardCreator = false;
    private EMWhiteboard whiteboard;

    static public int CanvasWidth = 720;
    static public int CanvasHeight = 480;

    private List<String> talkersList = new ArrayList<>();

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
        rzorderTop = 1;
        if(memberList != null){
            memberList.clear();
         }
        if(adminsList != null){
           adminsList.clear();
        }
        if(talkersList != null){
            talkersList.clear();
        }
        whiteboardRoomInfo = null;
        whiteboardCreator = false;
        whiteboard = null;
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

    public EMConferenceStream getConferenceStreamByMemId(String memName){
        String  memNameStr = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey() ,memName);
        if (memName == null || streamList == null || streamList.isEmpty()) {
            return null;
        }
        for (EMConferenceStream streamInfo : streamList){
            if (streamInfo.getMemberName().equals(memNameStr)){
                return streamInfo;
            }
        }
        return null;
    }

    public List<EMConferenceStream> getConferenceStreamList(){
        return  streamList;
    }

    public List<EMConferenceStream> getTalkerList(){
        return  talkerList;
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

    public void setAdmins(String[] admins){
        String adminStr = "";
        if(admins != null){
            if(admins.length > 0){
                if(adminsList == null){
                    adminsList = new ArrayList<>();
                }
                for(int i = 0; i < admins.length; i++) {
                    adminStr = admins[i];
                    adminStr = EasyUtils.useridFromJid(adminStr);
                    if(!adminsList.contains(adminStr)) {
                        adminsList.add(adminStr);
                    }
                }
            }
        }
    }

    public List<String> getAdmins(){
        if(adminsList == null){
            adminsList = new ArrayList<>();
        }
        return adminsList;
    }

    public EMWhiteboard getWhiteboard() {
        return whiteboard;
    }

    public void setWhiteboard(EMWhiteboard whiteboard) {
        this.whiteboard = whiteboard;
    }

    public WhiteBoardRoomInfo getWhiteboardRoomInfo() { return whiteboardRoomInfo; }

    public void setWhiteboardRoomInfo(WhiteBoardRoomInfo whiteboardRoomInfo) {
        this.whiteboardRoomInfo = whiteboardRoomInfo;
    }

    public void setTalkersList(String[] talkers){
        talkersList.clear();
        if(talkers != null){
            for(int i = 0 ; i < talkers.length;i++){
                talkersList.add(talkers[i]);
            }
        }
    }

    public int getTalkersList() {
      if(talkersList == null){
          return 0;
      }
      return talkersList.size();
    }

    public int removeTalkersList(String memberName) {
        if (talkersList != null) {
            if (talkersList.contains(memberName)) {
                talkersList.remove(memberName);
            }
            return talkersList.size();
        }
        return  0;
    }

    public int addTalkersList(String memberName) {
        if (talkersList != null) {
            if (!talkersList.contains(memberName)) {
                talkersList.add(memberName);
            }
            return talkersList.size();
        }
        return  0;
    }

    public boolean containsTalkersList(String memberName) {
        if (talkersList != null) {
            if(talkersList.contains(memberName)) {
                return  true;
            }
        }
        return  false;
    }
}
