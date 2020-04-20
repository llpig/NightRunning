package com.kong.nightrunning;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserRegisteredActivity extends AppCompatActivity {

    private Button mButtonReturnLogin;
    private Button mButtonRegistered;
    private RadioGroup mRadioGroupSex;
    private ImageView mImageViewUserAvatar;
    private Tool tool;
    private NightRunningDatabase helper;
    private String mUserAvatarPath = null;
    private static int USERAVATARPATHREQUESTCODE = 10;
    private static int USERSEX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registered);
        initActivity();
    }

    public void initActivity() {
        tool = new Tool();
        helper = MainActivity.getDatabaseHelper();
        getSupportActionBar().hide();
        findViewAndSetOnClickListener();
        checkPermissions();
        Log.i("DATA", "进入注册界面");
    }

    private void findViewAndSetOnClickListener() {
        ViewOnClickListener onClickListener = new ViewOnClickListener();
        mButtonReturnLogin = findViewById(R.id.ButtonReturnLogin);
        mButtonReturnLogin.setOnClickListener(onClickListener);
        mButtonRegistered = findViewById(R.id.ButtonRegistered);
        mButtonRegistered.setOnClickListener(onClickListener);
        mImageViewUserAvatar = findViewById(R.id.ImageViewRegUserAvatar);
        mImageViewUserAvatar.setOnClickListener(onClickListener);
        mRadioGroupSex = findViewById(R.id.sexRadioGroup);
        mRadioGroupSex.setOnCheckedChangeListener(new OnRadioGroupCheckedChangeListener());
    }

    public void checkPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        if (deniedPermissions.size() != 0) {
            ActivityCompat.requestPermissions(this, deniedPermissions.toArray(new String[deniedPermissions.size()]), 0);
        }
    }

    private void getInputInformationSave() {
        //用户名
        String userName = ((EditText) findViewById(R.id.EditTextRegUserName)).getText().toString().trim();
        //邮箱
        String email = ((EditText) findViewById(R.id.EditTextRegEmail)).getText().toString().trim();
        //年龄
        int age = Integer.parseInt(((EditText) findViewById(R.id.EditTextRegAge)).getText().toString().trim());
        //身高
        int height = Integer.parseInt(((EditText) findViewById(R.id.EditTextRegHeight)).getText().toString().trim());
        //体重
        double weight = Double.parseDouble(((EditText) findViewById(R.id.EditTextRegWeight)).getText().toString().trim());
        //目标步数
        int targetStepNumber = Integer.parseInt(((EditText) findViewById(R.id.EditTextRegTargetStepNumber)).getText().toString().trim());
        //目标里程
        double targetMileage = Double.parseDouble(((EditText) findViewById(R.id.EditTextRegTargetMileage)).getText().toString().trim());
        //密码
        String password = tool.getMD5Code(((EditText) findViewById(R.id.EditTextRegPassword)).getText().toString().trim());
        //确认密码
        String checkPassword = tool.getMD5Code(((EditText) findViewById(R.id.EditTextRegCheckPassword)).getText().toString().trim());


        Log.i("DATA", "用户名:" + userName + ",邮箱:" + email + ",年龄：" + age + ",身高" + height + ",体重:" + weight +
                "目标步数：" + targetStepNumber + ",目标里程：" + targetMileage + ",密码：" + password + ",确认密码：" + checkPassword + ",头像：" + mUserAvatarPath + ",性别：" + USERSEX);
        if (checkData(userName, password, checkPassword)) {
            //将数据更新到数据库
            SQLiteDatabase db = helper.getReadableDatabase();
            if (helper.insertRecordsToUserInfoTable(db, userName, password, USERSEX, age, height, weight, targetStepNumber, targetMileage, mUserAvatarPath)) {
                tool.showToast(UserRegisteredActivity.this, "您已成功注册即将为您跳转到登录界面");
                tool.startActivityFromIntent(UserRegisteredActivity.this, UserLoginActivity.class);
            } else {
                tool.showToast(UserRegisteredActivity.this, "注册失败，请重新注册");
            }
        }
    }

    private boolean checkData(String userName, String password, String checkPassword) {
        boolean bRet = true;
        String tmp = new String();
        if (userName == null) {
            tmp = "用户名为空";
            bRet = false;
        }
        if (!(password.equals(checkPassword))) {
            tmp = "两次密码输入不一致";
            bRet = false;
        }
        tool.showToast(this, tmp);
        return bRet;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USERAVATARPATHREQUESTCODE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mUserAvatarPath = cursor.getString(columnIndex);
            mImageViewUserAvatar.setImageURI(Uri.parse(mUserAvatarPath));
            cursor.close();
            Log.i("DATA", mUserAvatarPath);
        }
    }

    private class OnRadioGroupCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.manRadioButton: {
                    USERSEX = 0;
                    break;
                }
                case R.id.womenRadioButton: {
                    USERSEX = 1;
                    break;
                }
            }
        }
    }

    private class ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ButtonReturnLogin: {
                    tool.startActivityFromIntent(UserRegisteredActivity.this, UserLoginActivity.class);
                    Log.i("DATA", "返回登录界面");
                    break;
                }
                case R.id.ButtonRegistered: {
                    getInputInformationSave();
                    Log.i("DATA", "注册完成");
                    break;
                }
                case R.id.ImageViewRegUserAvatar: {
                    //调用系统相册
                    startActivityForResult(new Intent(
                            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), USERAVATARPATHREQUESTCODE);
                    break;
                }
            }
        }
    }

}
