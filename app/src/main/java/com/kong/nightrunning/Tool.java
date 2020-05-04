package com.kong.nightrunning;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//工具类
public class Tool {
    Intent intent = new Intent();
    Toast toast;
    private NightRunningDatabase helper;
    private static String packName = "com.kong.nightrunning";

    public NightRunningDatabase getRunningDatabase(Context currentContext) {
        if (helper == null) {
            helper = new NightRunningDatabase(currentContext, "NightRunning", null, 1);
        }
        return helper;
    }


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

    public String currentSystemTime() {
        Date date = new Date();
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    public String secondsConversion(long second) {
        //将秒转化为时：分：秒
        long hours, minutes;
        hours = second / 3600;//转换小时数
        second = second % 3600;//剩余秒数
        minutes = second / 60;//转换分钟
        second = second % 60;//剩余秒数
        String time = "";
        if (hours < 10) {
            time += "0";
        }
        time += String.valueOf(hours) + ":";
        if (minutes < 10) {
            time += "0";
        }
        time += String.valueOf(minutes) + ":";
        if (second < 10) {
            time += "0";
        }
        time += String.valueOf(second);
        return time;
    }

    public double getCalories(float weight, float mileage) {
        return (weight * mileage * 1.036) / 100;
    }

    public double getCalories(float stepNumber, float weight, int height, int age, boolean sex) {
        return getCalories(weight, getMileage((int) stepNumber, height, age, sex));
    }

    public float getMileage(float stepNumber, int height, int age, boolean sex) {
        float k = 0.4f;
        //女性年龄大于60
        if (sex) {
            k = 0.39f;
            if (sex && age > 60) {
                k = 0.37f;
            }
        }
        if (!sex && age > 60) {
            k = 0.385f;
        }
        return stepNumber * (height / 100.0f * k);
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
    public static void saveRunningDate(String fileName, String date) {
        String filePath = PersonalCenterFragment.FILEPATH;
        File pathDir = new File(filePath);
        if (!pathDir.exists()) {
            pathDir.mkdirs();
        }
        File pathDate = new File(pathDir, fileName);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(pathDate, true);
            outputStream.write((date + "\n").getBytes());
            outputStream.close();
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
                    weight = "Weight", age = "Age", emergencyContact = "EmergencyContact", avatar = "Avatar";
        }

        public class MotionInfoTable {
            String tableName = "MotionInfoTable", userName = "UserName", date = "Date", runningTime = "RunningTime", stepNumber = "StepNumber", mileage = "Mileage", equipmentInfo = "EquipmentInfo";
        }

        public class MovementLocusTable {
            String tableName = "MovementLocusTable", userName = "UserName", movementLocus = "MovementLocus";
        }

        public class AchievementTable {
            String tableName = "AchievementTable", userName = "UserName", achievement = "Achievement";
        }
    }

    //短信
    public class SafetySMS {
        private String mPhoneNumber;

        SafetySMS(String phoneNumber) {
            mPhoneNumber = phoneNumber;
        }

        public void sendSMS(AMapLocation location, String message) {
            //获取短信管理器
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
            //拆分短信内容（手机短信长度限制）
            List<String> divideContents = smsManager.divideMessage(setMessage(location, message));
            for (String text : divideContents) {
                smsManager.sendTextMessage(mPhoneNumber, null, text, null, null);
            }
        }

        private String setMessage(AMapLocation location, String message) {
            String msg = "夜跑APP安全提示:" +
                    "\n[用户]" + MainActivity.USERNAME +
                    "\n[异常行为]" + message +
                    "\n[位置]" + location.getCity() + location.getStreet() +
                    "\n请及时联系。";
            return msg;
        }
    }


}
