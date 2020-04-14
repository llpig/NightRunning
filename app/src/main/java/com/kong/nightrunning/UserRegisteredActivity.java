package com.kong.nightrunning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserRegisteredActivity extends AppCompatActivity {

    private Button mButtonReturnLogin;
    private Button mButtonRegistered;
    private Tool tool;
    private NightRunningDatabase helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registered);
        initActivity();
    }

    public void initActivity() {
        tool=new Tool();
        helper=MainActivity.getDatabaseHelper();
        getSupportActionBar().hide();
        findViewAndSetOnClickListener();
        Log.i("DATA","进入注册界面");
    }

    private void findViewAndSetOnClickListener() {
        ViewOnClickListener onClickListener = new ViewOnClickListener();
        mButtonReturnLogin = findViewById(R.id.ButtonReturnLogin);
        mButtonReturnLogin.setOnClickListener(onClickListener);
        mButtonRegistered=findViewById(R.id.ButtonRegistered);
        mButtonRegistered.setOnClickListener(onClickListener);
    }

    private void getInputInformationSave(){
        EditText userName=findViewById(R.id.EditTextRegUserName);
        EditText password=findViewById(R.id.EditTextRegPassword);
        String userNameText=userName.getText().toString().trim();
        String passwordText=tool.getMD5Code(password.getText().toString().trim());
        SQLiteDatabase db=helper.getReadableDatabase();
//        helper.insertRecordsToUserInfoTable(db,userNameText,passwordText);
        //将数据更新到数据库

        try {
            tool.showToast(this,"注册成功，请登录。");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tool.startActivityFromIntent(UserRegisteredActivity.this,UserLoginActivity.class);
    }

    private class ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ButtonReturnLogin: {
                    tool.startActivityFromIntent(UserRegisteredActivity.this,UserLoginActivity.class);
                    Log.i("DATA","返回登录界面");
                    break;
                }
                case R.id.ButtonRegistered:{
                    getInputInformationSave();
                    Log.i("DATA","注册完成");
                    break;
                }
            }
        }
    }

}
