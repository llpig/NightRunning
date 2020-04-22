package com.kong.nightrunning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static String TAG;
    private Intent serviceIntent = null;
    public static String USERNAME;
    public static String USERAVATAR;
    private Tool tool = new Tool();
    private TextView mTextViewTitle;
    private ImageView mImageViewUserAvatar;
    private static NightRunningDatabase helper;
    private Button mButtonSportsShow, mButtonRunning, mButtonPersonalCenter;
    public Fragment mLastFragment, mSportsShowFragment, mRunningFragment, mPersonalCenterFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //取消App的标题栏
        getSupportActionBar().hide();
        helper = new NightRunningDatabase(this, "NightRunning", null, 1);
        TAG = getPackageName();
        initFragment();
        getUserLoginInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences(UserLoginActivity.USERINFOFILENAME, MODE_PRIVATE);
        USERNAME = preferences.getString(UserLoginActivity.USERNAME, null);
        USERAVATAR = preferences.getString(UserLoginActivity.AVATAR, null);
        initActivity();
        startNightRunningService();
    }


    //初始化Activity
    private void initActivity() {
        findViewAndSetOnClickListener();
    }

    private void initFragment() {
        mTextViewTitle = findViewById(R.id.TextViewTitle);
        mSportsShowFragment = new SportsShowFragment();
        mRunningFragment = new RunningFragment();
        mPersonalCenterFragment = new PersonalCenterFragment();
        //上一个点击使用的Fragment
        mLastFragment = mSportsShowFragment;
        //将运动展示界面作为App的首页
        getSupportFragmentManager().beginTransaction().add(R.id.LayoutContent, mSportsShowFragment).commit();
    }

    //读取用户登录信息
    private void getUserLoginInfo() {
        SharedPreferences preferences = getSharedPreferences(UserLoginActivity.USERINFOFILENAME, MODE_PRIVATE);
        USERNAME = preferences.getString(UserLoginActivity.USERNAME, null);
        if (USERNAME == null) {
            tool.startActivityFromIntent(this, UserLoginActivity.class);
        }
    }

    //启动服务
    private void startNightRunningService() {
        //启动服务
        if (serviceIntent == null) {
            serviceIntent = new Intent(MainActivity.this, NightRunningService.class);
            startService(serviceIntent);
            if (NightRunningSensorEventListener.getTodayAddStepNumber() == -1) {
                stopService(serviceIntent);
                tool.showToast(MainActivity.this, "无可用传感器");
            } else {
                tool.showToast(MainActivity.this, "传感器已注册，系统已开始记录步数");
            }
        }
    }

    //Fragment管理(添加新的会导致之前的内容被覆盖)
    private void fragmentLoadingManager(Fragment currentFragment) {
        //如果本次点击内容和上次点击内容不相同
        if (currentFragment != mLastFragment) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            //隐藏上次点击展示的内容
            //App从后台重现切入前台后，hide失效。
            fragmentTransaction.hide(mLastFragment);
            //更新上次的Fragment
            mLastFragment = currentFragment;
            //判断该组件是否被加载过
            if (currentFragment.isAdded()) {
                fragmentTransaction.show(currentFragment);
                Log.i("DATA", "FragmentManagerShow");
            } else {
                fragmentTransaction.add(R.id.LayoutContent, currentFragment);
                Log.i("DATA", "FragmentManagerAdd");
            }
            fragmentTransaction.commit();
        }
    }

    //查询组件并设置点击事件
    private void findViewAndSetOnClickListener() {
        ViewOnClickListener onClickListener = new ViewOnClickListener();
        mImageViewUserAvatar = findViewById(R.id.ImageViewMainUserAvatar);
        mImageViewUserAvatar.setOnClickListener(onClickListener);
        mImageViewUserAvatar.setImageURI(Uri.parse(USERAVATAR));

        mButtonSportsShow = findViewById(R.id.ButtonSportsShow);
        mButtonSportsShow.setOnClickListener(onClickListener);

        mButtonRunning = findViewById(R.id.ButtonRunning);
        mButtonRunning.setOnClickListener(onClickListener);

        mButtonPersonalCenter = findViewById(R.id.ButtonSportsCircle);
        mButtonPersonalCenter.setOnClickListener(onClickListener);
    }

    //用户头像点击事件（点击头像进入登录）
    private void userAvatarOnClickListener() {
        //登录
        tool.startActivityFromIntent(this, UserLoginActivity.class);
    }

    //运动展示点击事件
    private void sportsShowOnClickListener() {
        mTextViewTitle.setText(R.string.sports_show);
        mButtonSportsShow.setBackgroundResource(R.drawable.sports_show_red);
        mButtonRunning.setBackgroundResource(R.drawable.running_black);
        mButtonPersonalCenter.setBackgroundResource(R.drawable.sports_circle_balck);
        fragmentLoadingManager(mSportsShowFragment);
    }

    //跑步点击事件
    private void runningOnClickListener() {

        mTextViewTitle.setText(R.string.running);
        mButtonSportsShow.setBackgroundResource(R.drawable.sports_show_black);
        mButtonRunning.setBackgroundResource(R.drawable.running_red);
        mButtonPersonalCenter.setBackgroundResource(R.drawable.sports_circle_balck);
        fragmentLoadingManager(mRunningFragment);
    }

    //个人中心点击事件
    private void personalCenterOnClickListener() {
        mTextViewTitle.setText(R.string.sports_circle);
        mButtonSportsShow.setBackgroundResource(R.drawable.sports_show_black);
        mButtonRunning.setBackgroundResource(R.drawable.running_black);
        mButtonPersonalCenter.setBackgroundResource(R.drawable.sports_circle_red);
        fragmentLoadingManager(mPersonalCenterFragment);
    }

    //组件点击事件监听器
    private class ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //用户头像
                case R.id.ImageViewMainUserAvatar: {
                    userAvatarOnClickListener();
                    break;
                }
                //运动展示
                case R.id.ButtonSportsShow: {
                    sportsShowOnClickListener();
                    break;
                }
                //跑步模式
                case R.id.ButtonRunning: {
                    runningOnClickListener();
                    break;
                }
                //运动圈
                case R.id.ButtonSportsCircle: {
                    personalCenterOnClickListener();
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public static NightRunningDatabase getDatabaseHelper() {
        return helper;
    }
}
