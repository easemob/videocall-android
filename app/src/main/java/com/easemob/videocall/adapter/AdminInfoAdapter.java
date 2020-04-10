package com.easemob.videocall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.easemob.videocall.R;
import com.easemob.videocall.utils.ConferenceInfo;
import com.easemob.videocall.utils.PreferenceManager;
import com.easemob.videocall.utils.StringUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceMember;

import java.util.List;

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 04/10/2020
 */

public class AdminInfoAdapter extends ArrayAdapter<String> {

    private int layoutId;

    public AdminInfoAdapter(Context context, int layoutId, List<String> list){
        super(context, layoutId, list);
        this.layoutId = layoutId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String item = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);

        TextView textView = (TextView) view.findViewById(R.id.item_admin_text);
        if(item != null){
            if(item.equals(EMClient.getInstance().getCurrentUser())){
                textView.setText(StringUtils.tolongNickName(PreferenceManager.getInstance().getCurrentUserNick(),10));
            }else{
                EMConferenceMember memberInfo = ConferenceInfo.getInstance().getConferenceMemberInfo(item);
                if(memberInfo != null){
                    textView.setText(StringUtils.tolongNickName(memberInfo.nickName,10));
                }
            }
        }
        return view;
    }
}
