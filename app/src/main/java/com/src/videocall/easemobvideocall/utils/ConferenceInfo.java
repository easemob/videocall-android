package com.src.videocall.easemobvideocall.utils;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConferenceInfo {
    private String roomname;
    private String password;
    private EMConference conference;

    private static ConferenceInfo conferenceInfo = null;
    private List<EMConferenceStream> streamList = new ArrayList<>();
    private EMConferenceStream localStream = new EMConferenceStream();
    private EMConferenceManager.EMConferenceRole  conferenceRole;

    private List<EMConferenceStream> streamListAddLocal = new ArrayList<>();

    static public ConferenceInfo getInstance(){
        if(conferenceInfo == null){
            conferenceInfo = new ConferenceInfo();
        }
        return  conferenceInfo;
    }

    private ConferenceInfo(){

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
        return roomname;
    }

    public void setRoomname(String roomname) {
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

    public  List<EMConferenceStream> deepCopy(List<EMConferenceStream> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<EMConferenceStream> dest = (List<EMConferenceStream>) in.readObject();
        return dest;
    }

    private static String replaceAction(String username, String regular) {
        return username.replaceAll(regular, "*");
    }

    public static String userIdReplaceWithStar(String userId) {

        if (userId.isEmpty() || userId == null) {
            return null;
        } else {
            return replaceAction(userId, "(?<=\\d{4})\\d(?=\\d{4})");
        }
    }

}
