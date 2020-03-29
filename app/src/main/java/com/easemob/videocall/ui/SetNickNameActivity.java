package com.easemob.videocall.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.videocall.R;
import com.easemob.videocall.utils.PreferenceManager;

public class SetNickNameActivity extends Activity  implements View.OnClickListener {

    EditText edit_nickName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname_setting);


        //设置昵称
        edit_nickName = findViewById(R.id.edit_nickName);
        String nickName = PreferenceManager.getInstance().getCurrentUserNick();
        edit_nickName.setText(nickName);

        Button nicknameCancel = findViewById(R.id.editnickname_back);
        nicknameCancel.setOnClickListener(this);

        Button nicknameSave = findViewById(R.id.save_nickname);
        nicknameSave.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editnickname_back:
                setNickName();
                break;
            case R.id.save_nickname:
                setNickName();
                break;
            default:
                break;
        }
    }

    private  void setNickName(){
        String name = edit_nickName.getText().toString().trim();
        if(name.length() <= 0){
            Toast.makeText(getApplicationContext(), "昵称不允许为空!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        PreferenceManager.getInstance().setCurrentUserNick(name);
        getIntent().putExtra("nickName", name);
        setResult(RESULT_OK, getIntent());
        finish();
    }
}
