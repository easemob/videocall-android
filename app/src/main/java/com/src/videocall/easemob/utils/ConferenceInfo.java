package com.src.videocall.easemob.utils;
import android.util.Log;

import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.util.EasyUtils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConferenceInfo {
    private String roomname = null;
    private String password = null;
    private EMConference conference;

    private static ConferenceInfo conferenceInfo = null;
    private List<EMConferenceStream> streamList = new ArrayList<>();
    private EMConferenceStream localStream = new EMConferenceStream();
    private EMConferenceManager.EMConferenceRole conferenceRole;

    public static int currentIndex = -1;
    public static EMConferenceStream currentStream = null;
    public static boolean changeflag = false;
    public static boolean removeflag = false;

    public static List<String> subscribestream = new ArrayList<>();
    private List<EMConferenceStream> streamListAddLocal = new ArrayList<>();

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
        subscribestream.clear();
        currentIndex = -1;
        currentStream = null;
        changeflag = false;
        removeflag = false;
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

    public List<EMConferenceStream> getConferenceStreamList(){
        return  streamList;
    }

    private List<EMConferenceStream> getConferenceStreamListAddLocal()
    {
        return streamListAddLocal;
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

    public String getAdmin(){
        String[] admins = ConferenceInfo.getInstance().getConference().getAdmins();
        String adminStr = "";
        if(admins.length > 0){
            for (int i = 0; i < admins.length; i++) {
                adminStr = admins[0];
                adminStr = EasyUtils.useridFromJid(adminStr);
                return adminStr;
            }
        }
        return adminStr;
    }
}
