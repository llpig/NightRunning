package com.kong.nightrunning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//跑步模式Fragment
public class RunningFragment extends Fragment {

    private Tool tool;
    private AMap mMap = null;
    private Timer mDrawTimer = null;
    private AMapLocationClient mMapLocationClient = null;
    private List<LatLng> mLatLngs = new ArrayList<LatLng>();
    private Button mButtonRunningFlag;
    private TextView mTextViewRunningTime;
    private TextView mTextViewRunningSpeed;
    private MapLocationListener mapLocationListener;
    private Timer timer = new Timer();

    private static int RUNNINGFLAG = 0;
    private static int MAP_ZOOM_LEVEL = 17;
    private static int POSITIONINGCYCLE = 1000;
    public static int PERMISSION_REQUEST_CODE = 10;
    private static int DRAW_MAP_PATH_LINE_WIDTH = 10;
    private static int TIMINGMESSAGEFLAG = 1;

    private LatLng lastLatLng;
    private float currentSpend;
    private float currentMileage = 0;
    private long currentRunningTime;
    private String currentCity = null;
    private LocalWeatherLive mWeatherLive;
    private NightRunningDatabase helper;
    private static float SAFETYDISTANCE = 7.0f;
    private static String SAFETYPHONE;
    private AMapLocation currentAMapLocation;
    private Tool.SafetySMS safetySMS;
    private static int SAFETYTIME = 180;
    private PowerManager.WakeLock wakeLock;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);
        tool = new Tool();
        MapView mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mMap = mapView.getMap();
        mButtonRunningFlag = view.findViewById(R.id.ButtonRunningFlag);
        mTextViewRunningTime = view.findViewById(R.id.TextViewRunningTime);
        mTextViewRunningSpeed = view.findViewById(R.id.TextViewRunningSpeed);
        initRunningFragment();
        return view;
    }

    public void checkPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WAKE_LOCK
        };
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        if (deniedPermissions.size() != 0) {
            ActivityCompat.requestPermissions(getActivity(), deniedPermissions.toArray(new String[deniedPermissions.size()]), PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private void initRunningFragment() {
        checkPermissions();
        helper = tool.getRunningDatabase(getActivity());
        SAFETYPHONE = helper.selectEmergencyContact(helper.getReadableDatabase(), MainActivity.USERNAME).trim();
        safetySMS = tool.new SafetySMS(SAFETYPHONE);
        PowerManager powerManager = (PowerManager) getActivity().getSystemService(getActivity().POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "WAKELOCK");
        mapLocationListener = new MapLocationListener();
        mMapLocationClient = new AMapLocationClient(getActivity());
        mButtonRunningFlag.setOnClickListener(new ButtonRunningOnClickListener());
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        locationStyle.interval(POSITIONINGCYCLE);
        //设置地图风格
        mMap.setMyLocationStyle(locationStyle);
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mMap.setMyLocationEnabled(true);
        //地图缩放显示级别为17（0-19，数值越大越详细）
        mMap.moveCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM_LEVEL));
    }

    //设置定位模式
    private AMapLocationClientOption getMapLocationClientOption() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        //将定位模式设置为运动
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport);
        //设置定位精度为高精度
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位为连续定位间隔
        option.setInterval(POSITIONINGCYCLE);
        return option;
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置用户选项
        mMapLocationClient.setLocationOption(getMapLocationClientOption());
        //设置位置监听
        mMapLocationClient.setLocationListener(mapLocationListener);
        mMapLocationClient.stopLocation();
        mMapLocationClient.startLocation();
    }

    private void getWeatherInfo() {
        WeatherSearchQuery mquery = new WeatherSearchQuery(currentCity, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch mweathersearch = new WeatherSearch(getActivity());
        mweathersearch.setOnWeatherSearchListener(new WeatherSearchListener());
        mweathersearch.setQuery(mquery);
        mweathersearch.searchWeatherAsyn(); //异步搜索
    }

    private class WeatherSearchListener implements WeatherSearch.OnWeatherSearchListener {

        @Override
        public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                mWeatherLive = weatherLiveResult.getLiveResult();
                startRunningInfoHintDialog();
            } else {
                tool.showToast(getActivity(), "天气查询失败,请稍后重新点击。");
            }
        }

        @Override
        public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

        }
    }

    private void startRunningInfoHintDialog() {
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //    设置Title的图标
        builder.setIcon(R.drawable.logo);
        //    设置Title的内容
        builder.setTitle("温馨提示");
        //    设置Content来显示一个信息
        builder.setMessage(getStartRunningHintMessage());
        //    设置一个PositiveButton
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //更换图标并启动定期，开始定时
                mButtonRunningFlag.setBackgroundResource(R.drawable.stop_running);
                RUNNINGFLAG = 1 - RUNNINGFLAG;
//                if (judgmentTime()) {
//                    safetySMS.sendSMS(currentAMapLocation, tool.currentSystemTime() + "外出跑步");
//                }
                wakeLock.acquire();
                final TimingHandler handler = new TimingHandler();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Message timingMessage = new Message();
                        timingMessage.arg1 = TIMINGMESSAGEFLAG;
                        handler.sendMessage(timingMessage);
                    }
                };
                timer = new Timer();
                timer.schedule(timerTask, 0, POSITIONINGCYCLE);
                tool.showToast(getActivity(), "开始跑步请您注意安全");
            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tool.showToast(getActivity(), "您已取消了跑步计划");
            }
        });
        //    显示出该对话框
        builder.show();
    }

    private boolean judgmentTime() {
        boolean bRet = false;
        String currentTime = tool.currentSystemTime();
        int remainingHour = Integer.parseInt(PersonalCenterFragment.TRAININGLATESTTIME.substring(0, 2)) - Integer.parseInt(currentTime.substring(0, 2));
        int remainingMinute = Integer.parseInt(PersonalCenterFragment.TRAININGLATESTTIME.substring(3, 5)) - Integer.parseInt(currentTime.substring(3, 5));
        if (remainingHour < 0 || (remainingHour == 0 && remainingMinute <= 0)) {
            bRet = true;
        }
        return bRet;
    }

    private String getStartRunningHintMessage() {

        String[] badWeather = new String[]{
                "强风/劲风", "疾风", "大风", "烈风", "风暴", "狂爆风", "飓风", "热带风暴", "中度霾", "重度霾", "严重霾", "阵雨", "雷阵雨", "雷阵雨并伴有冰雹", "小雨", "中雨", "大雨", "暴雨", "大暴雨", "特大暴雨", "强阵雨", "强雷阵雨", "极端降雨", "毛毛雨/细雨", "雨", "小雨-中雨", "中雨-大雨", "大雨-暴雨", "暴雨-大暴雨", "大暴雨-特大暴雨", "雨雪天气", "雨夹雪", "阵雨夹雪", "冻雨", "雪", "阵雪", "小雪", "中雪", "大雪", "暴雪", "小雪-中雪", "中雪-大雪", "大雪-暴雪", "浮尘", "扬沙", "沙尘暴", "强沙尘暴", "龙卷风", "雾", "浓雾", "强浓雾", "轻雾", "大雾", "特强浓雾", "未知"
        };
        String message = "", goOutHint = "", temperatureHint = "";
        if (judgmentTime()) {
            goOutHint += "时间太晚，";
        }

        if (mWeatherLive != null) {
            message += "\n天气：" + mWeatherLive.getWeather() +
                    "\n气温：" + mWeatherLive.getTemperature()
                    + "°\n风向：" + mWeatherLive.getWindDirection()
                    + "\n风力等级:" + mWeatherLive.getWindPower()
                    + "级\n空气湿度：" + mWeatherLive.getHumidity() + "%\n";
            for (String value : badWeather) {
                if (value.equals(mWeatherLive.getWeather())) {
                    goOutHint += "室外天气不好，";
                    break;
                }
            }
            if (Integer.parseInt(mWeatherLive.getTemperature()) >= 25) {
                temperatureHint = "外界温度过高,外出跑步请注意防晒并携带适量饮用水.\n";
            } else if (Integer.parseInt(mWeatherLive.getTemperature()) <= 5) {
                temperatureHint = "外界温度过低,外出跑步请注意保暖,跑步前进行简单热身.\n";
            }
        }

        if (!goOutHint.isEmpty()) {
            message += goOutHint + "建议您不要外出。\n";
        }

        if (!temperatureHint.isEmpty()) {
            message += temperatureHint;
        }

        message += "\n您确定开始跑步吗？";

        return message;
    }

    private void stopRunningInfoHintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo);
        builder.setTitle("温馨提示");
        builder.setMessage(getStopRunningHintMessage());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (currentMileage > 0) {
                    currentMileage = Float.parseFloat(String.format("%.2f", currentMileage / 1000.0f));
                    NightRunningDatabase helper = tool.getRunningDatabase(getActivity());
                    helper.upDateRecordsToMotionInfoTableRunning(helper.getReadableDatabase(),
                            MainActivity.USERNAME, "date('now','localtime')", currentMileage, currentRunningTime);
                }
                mButtonRunningFlag.setBackgroundResource(R.drawable.start_running);
                RUNNINGFLAG = 1 - RUNNINGFLAG;
                currentRunningTime = 0;
                timer.cancel();
                wakeLock.release();
                tool.showToast(getActivity(), "跑步结束，停止定位，请及时关闭GPS");
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tool.showToast(getActivity(), "跑步继续");
            }
        });
        builder.show();
    }

    private String getStopRunningHintMessage() {
        String message = "距离:" + String.format("%.2f", currentMileage / 1000.0f) +
                "km\n时间:" + tool.secondsConversion(currentRunningTime)
                + "\n平均速度:" + String.valueOf(currentSpend / currentRunningTime)
                + "m/s\n消耗卡路里:" + String.format("%.2f", tool.getCalories(MainActivity.USERWEIGHT, currentMileage) / 10.0f)
                + "KCal\n\n您确定停止跑步吗？";
        return message;
    }

    private class TimingHandler extends Handler {
        public void handleMessage(Message message) {
            if (message.arg1 == TIMINGMESSAGEFLAG) {
                ++currentRunningTime;
                //不是每次都更新
                setTextViewRunningTime();
                setTextViewRunningSpeed();
            }
        }

        private void setTextViewRunningTime() {
            mTextViewRunningTime.setText("时间\n" + tool.secondsConversion(currentRunningTime));
            if (currentRunningTime % 2 == 0) {
                mTextViewRunningTime.setBackgroundColor(Color.RED);
            } else {
                mTextViewRunningTime.setBackgroundColor(Color.BLUE);
            }

        }

        private void setTextViewRunningSpeed() {
            mTextViewRunningSpeed.setText("速度\n" + String.valueOf(currentSpend));
            if (currentRunningTime % 2 == 0) {
                mTextViewRunningSpeed.setBackgroundColor(Color.BLUE);
            } else {
                mTextViewRunningSpeed.setBackgroundColor(Color.RED);
            }
        }
    }

    private class MapLocationListener implements AMapLocationListener {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                currentCity = aMapLocation.getCity();
                currentAMapLocation = aMapLocation;
                if (RUNNINGFLAG != 0) {
                    final LatLng currentLatng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    currentSpend = aMapLocation.getSpeed();
                    if (lastLatLng == null) {
                        lastLatLng = currentLatng;
                    }
                    mLatLngs.clear();
                    mLatLngs.add(lastLatLng);
                    mLatLngs.add(currentLatng);
                    mMap.addPolyline(getPolylineOptions(mLatLngs));
                    float distance = AMapUtils.calculateLineDistance(lastLatLng, currentLatng);
                    currentMileage += distance;
                    if (distance >= ((POSITIONINGCYCLE / 1000.0f) * SAFETYDISTANCE)) {
                        safetySMS.sendSMS(aMapLocation, "速度异常(" + (distance / ((POSITIONINGCYCLE / 1000.0f))) + ")");
                    }
                    if (lastLatLng.toString().equals(currentCity)) {
                        --SAFETYTIME;
                        if (SAFETYTIME == 0) {
                            safetySMS.sendSMS(aMapLocation, "超过3分钟静止不动");
                            SAFETYTIME = 60;
                        }
                    } else {
                        SAFETYTIME = 180;
                    }
                    lastLatLng = currentLatng;
                }
            }
        }

        private PolylineOptions getPolylineOptions(List<LatLng> latLngs) {
            PolylineOptions polylineOptions = new PolylineOptions();
            //设置线宽
            polylineOptions.width(DRAW_MAP_PATH_LINE_WIDTH);
            //设置颜色
            polylineOptions.color(R.color.colorAccent);
            //显示线是否可见
            polylineOptions.visible(true);
            //设置数据
            polylineOptions.addAll(latLngs);
            return polylineOptions;
        }
    }

    private class ButtonRunningOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ButtonRunningFlag: {
                    if (RUNNINGFLAG == 0) {
                        getWeatherInfo();
                    } else {
                        stopRunningInfoHintDialog();
                    }
                    break;
                }
            }
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDrawTimer.cancel();
        mMapLocationClient.onDestroy();
        mMapLocationClient.stopLocation();
    }
}
