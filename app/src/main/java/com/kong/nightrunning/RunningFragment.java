package com.kong.nightrunning;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//跑步模式Fragment
public class RunningFragment extends Fragment {

    private AMap mMap = null;
    private Timer mDrawTimer = null;
    private AMapLocationClient mMapLocationClient = null;
    private List<LatLng> mLatLngs = new ArrayList<LatLng>();

    private static int MAP_ZOOM_LEVEL = 17;
    private static int DRAW_MAP_PATH_PERIOD = 5000;
    private static int DRAW_MAP_PATH_LINE_WIDTH = 10;
    public static int PERMISSION_REQUEST_CODE = 10;
    public static boolean PERMISSION_FLAG = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);
        MapView mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mMap = mapView.getMap();
        initAMap();
        mMapLocationClient = new AMapLocationClient(getActivity());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置用户选项
        mMapLocationClient.setLocationOption(getMapLocationClientOption());
        //设置位置监听
        mMapLocationClient.setLocationListener(new MapLocationListener());
        mMapLocationClient.stopLocation();
        mMapLocationClient.startLocation();
        //开启定时器
        mDrawTimer = new Timer();
        mDrawTimer.schedule(new DrawPathTimerTask(), DRAW_MAP_PATH_PERIOD, DRAW_MAP_PATH_PERIOD);
    }

    private void initAMap() {
        checkPermissions();
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        locationStyle.interval(2000);
        //设置地图风格
        mMap.setMyLocationStyle(locationStyle);
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mMap.setMyLocationEnabled(true);
        //地图缩放显示级别为17（0-19，数值越大越详细）
        mMap.moveCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM_LEVEL));
    }


    //设置定位模式
    private AMapLocationClientOption getMapLocationClientOption() {
        long timeCycle = 1000;
        AMapLocationClientOption option = new AMapLocationClientOption();
        //将定位模式设置为运动
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport);
        //设置定位精度为高精度
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位为连续定位间隔
        option.setInterval(timeCycle);
        return option;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDrawTimer.cancel();
        mMapLocationClient.onDestroy();
        mMapLocationClient.stopLocation();
    }

    private class DrawPathTimerTask extends TimerTask {
        private String fileName = null;

        @Override
        public void run() {
            if (mLatLngs.size() > 0) {
                mMap.addPolyline(getPolylineOptions(mLatLngs));
                //将数据保存到文件中
                Tool.saveRunningPathDate(getFileName(), mLatLngs);
                //清理数据并保存最后一个，和下次数据连接使用
                LatLng tmpLaglng = mLatLngs.get(mLatLngs.size() - 1);
                mLatLngs.clear();
                mLatLngs.add(tmpLaglng);
            }
        }

        private PolylineOptions getPolylineOptions(List<LatLng> latLngs) {
            PolylineOptions polylineOptions = new PolylineOptions();
            //设置数据
            polylineOptions.addAll(latLngs);
            //设置线宽
            polylineOptions.width(DRAW_MAP_PATH_LINE_WIDTH);
            //设置颜色
            polylineOptions.color(R.color.colorAccent);
            //显示线是否可见
            polylineOptions.visible(true);
            return polylineOptions;
        }

        private String getFileName() {
            if (null == fileName) {
                fileName = "运动轨迹_" + System.currentTimeMillis();
            }
            return fileName;
        }
    }

    public void checkPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
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


    private class MapLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            double latitude, longitude;
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                //纬度
                latitude = aMapLocation.getLatitude();
                //经度
                longitude = aMapLocation.getLongitude();
                mLatLngs.add(new LatLng(latitude, longitude));
            }
        }
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

}
