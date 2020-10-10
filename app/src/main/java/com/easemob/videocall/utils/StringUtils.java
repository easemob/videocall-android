package com.easemob.videocall.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * 昵称太长截取显示
     * @param nickName
     * @return
     */
    public static String tolongNickName(String nickName ,int length){
        //如果字符串中有汉字 字符串长度/2
        Pattern  p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(nickName);
        if(m.find()){
            length = length/2;
        }
        if(nickName != null){
            if(nickName.length() > length){
                String fristStr = nickName.substring(0,length);
                fristStr += "...";
                return fristStr;
            }else{
                return nickName;
            }
        }else{
            return null;
        }
    }
}
