package com.kong.nightrunning;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.amap.api.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

//工具类
public class Tool {
    Intent intent = new Intent();
    Toast toast;
    private static String packName = "com.kong.nightrunning";

    //消息类型
    public static enum MessageType {
        CURRENTSTEPNUMBERKEY(1), FOREGROUNDSERVICE(2);
        private int index;

        MessageType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    //自定义广播
    public static enum CustomBroadcast {
        UPDATASTEPNUMBER(packName + ".updatastepnumber"),
        NULLSERSOR(packName + ".nullsersor");
        private String index;

        CustomBroadcast(String index) {
            this.index = index;
        }

        public String getIndex() {
            return index;
        }
    }

    //跳转活动
    public void startActivityFromIntent(Context currentContext, Class<?> jumpedClass) {
        intent.setClass(currentContext, jumpedClass);
        currentContext.startActivity(intent);
    }

    //提示消息
    public void showToast(Context currentContext, String message) {
        toast = Toast.makeText(currentContext, "", Toast.LENGTH_SHORT);
        toast.setText(message);
        toast.show();
    }

    //保存跑步路线数据
    public static void saveRunningPathDate(String fileName, List<LatLng> lagLngs) {
        String filePath = "/sdcard/NightRunning";
        File pathDir = new File(filePath);
        if (!pathDir.exists()) {
            pathDir.mkdirs();
        }
        File pathDate = new File(pathDir, fileName);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(pathDate, true);
            for (LatLng latLng : lagLngs) {
                outputStream.write((latLng.toString() + "\n").getBytes());
            }
            outputStream.close();
            loadFileData(pathDir + "/" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFileData(String fileName) {
        FileInputStream inputStream = null;
        String data = "";
        try {
            inputStream = new FileInputStream(fileName);
            int tmp = -1;
            do {
                tmp = inputStream.read();
                if (tmp == '\n') {
                    Log.i("DATA", data);
                    data = "";
                } else {
                    data += (char) tmp;
                }
            } while (tmp != -1);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //加密字符串
    public String getMD5Code(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("utf-8"));
            byte[] encryption = md5.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    stringBuffer.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    stringBuffer.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            Log.e(MainActivity.TAG, "MD5算法加密密码失败");
            return null;
        }
    }

    //保存数据相关信息
    public class NightRunningDB {
        UserInfoTable userInfoTable = new UserInfoTable();
        MotionInfoTable motionInfoTable = new MotionInfoTable();
        MovementLocusTable movementLocusTable = new MovementLocusTable();
        AchievementTable achievementTable = new AchievementTable();

        public class UserInfoTable {
            String tableName = "UserInfoTable", userName = "UserName", password = "Password", sex = "Sex", height = "Height",
                    weight = "Weight", age = "Age", targetStepNumber = "TargetStepNumber", targetMileage = "TargetMileage", avatar = "Avatar";
        }

        public class MotionInfoTable {
            String tableName = "MotionInfoTable", userName = "UserName", date = "Date", runningStartTime = "RunningStartTime", runningFinishTime = "RunningFinishTime", stepNumber = "StepNumber", mileage = "Mileage", equipmentInfo = "EquipmentInfo";
        }

        public class MovementLocusTable {
            String tableName = "MovementLocusTable", userName = "UserName", movementLocus = "MovementLocus";
        }

        public class AchievementTable {
            String tableName = "AchievementTable", userName = "UserName", achievement = "Achievement";
        }
    }
}
