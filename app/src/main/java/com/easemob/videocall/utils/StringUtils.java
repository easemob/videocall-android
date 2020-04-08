package com.easemob.videocall.utils;

public class StringUtils {

    /**
     * 昵称太长截取显示
     * @param nickName
     * @return
     */
    public static String tolongNickName(String nickName ,int length){
        if(nickName != null){
            if(nickName.length() > length){
                //String fristStr = nickName.substring(0,3);
                //String lastStr =  nickName.substring(nickName.length()-3);
                //fristStr = fristStr+"***"+lastStr;
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
