package com.kong.nightrunning;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UserLoginActivity extends AppCompatActivity {

    private Button mButtonUserLogin, mButtonQQAvatar;
    private TextView mTextViewRegistered, mTextViewForgetPassword;
    private Tool tool;
    private NightRunningDatabase helper;
    public static String USERINFOFILENAME="LoginInfo";
    public static String USERNAME="userName";
    public static String PASSWORD="password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        this.initActivity();
        this.findViewAndSetOnClickListener();
    }

    private void initActivity() {
        getSupportActionBar().hide();
        tool = new Tool();
        helper = MainActivity.getDatabaseHelper();
        tool.showToast(this,"检测到您还尚未登录，已为您跳转到登录界面。");
    }

    private void goToRegisteredActivity() {
        tool.startActivityFromIntent(this, UserRegisteredActivity.class);
    }

    private void findViewAndSetOnClickListener() {
        ViewOnClickListener onClickListener = new ViewOnClickListener();
        //登录
        mButtonUserLogin = findViewById(R.id.ButtonUserLogin);
        mButtonUserLogin.setOnClickListener(onClickListener);
        //QQ头像
        mButtonQQAvatar = findViewById(R.id.ButtonQQAvatar);
        mButtonQQAvatar.setOnClickListener(onClickListener);
        //注册账号
        mTextViewRegistered = findViewById(R.id.TextViewRegistered);
        mTextViewRegistered.setOnClickListener(onClickListener);
        //忘记密码
        mTextViewForgetPassword = findViewById(R.id.TextViewForgetPassword);
        mTextViewForgetPassword.setOnClickListener(onClickListener);
    }

    //登录事件
    private void login() {
        String userName = ((EditText) findViewById(R.id.EditTextLoginUserName)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.EditTextLoginPassword)).getText().toString().trim();
        if (userName.isEmpty() || password.isEmpty()) {
            tool.showToast(this, "用户名或密码为空");
        } else {
            if (helper.selectRecordsToUserInfoTable(helper.getReadableDatabase(), userName).equals(password)) {
                tool.showToast(this, "您已经成功登录");
                //将信息存入应用的私有文件夹中（用户不需要每次都登录）
                SharedPreferences preferences = getSharedPreferences(USERINFOFILENAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(USERNAME, userName);
                editor.putString(PASSWORD, password);
                editor.commit();
                this.finish();
            } else {
                tool.showToast(this, "用户名或者密码错误");
            }
        }
    }

    private class ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ButtonUserLogin: {
                    //登录
                    login();
                    break;
                }
                case R.id.ButtonQQAvatar: {
                    //QQ登录
                    break;
                }
                case R.id.TextViewRegistered: {
                    //注册
                    goToRegisteredActivity();
                    break;
                }
                case R.id.TextViewForgetPassword: {
                    //忘记密码
                    break;
                }
            }
        }
    }

}
