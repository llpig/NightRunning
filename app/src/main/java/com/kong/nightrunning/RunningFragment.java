package com.kong.nightrunning;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;

//跑步模式Fragment
public class RunningFragment extends Fragment {

    AMapLocationClient mapLocationClient = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);
        MapView mapView = view.findViewById(R.id.map);
        // 此方法必须重写
        mapView.onCreate(savedInstanceState);
        setMapStyle(mapView);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //启动服务
        Intent mapServiceIntent=new Intent();
        mapServiceIntent = new Intent(getActivity(), GaoDeMapService.class);
        getActivity().startService(mapServiceIntent);

        mapLocationClient = new AMapLocationClient(getActivity().getApplicationContext());
        //定位回调监听器
        AMapLocationListener mapLocationListener = new GaoDeMapService();
        //在定位客户端设置定位回调监听器
        mapLocationClient.setLocationListener(mapLocationListener);
        mapLocationClient.setLocationOption(setMapLocationMode());
        //重启定位，使得设置生效。
        mapLocationClient.stopLocation();
        mapLocationClient.startLocation();
    }

    //修稿地图样式
    private void setMapStyle(MapView mapView) {
        //获取地图样式
        long timeCycle = 2000;
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.interval(timeCycle);
        //设置地图风格
        mapView.getMap().setMyLocationStyle(locationStyle);
        LocationSource locationSource=new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {

            }

            @Override
            public void deactivate() {

            }
        };
//        mapView.getMap().setLocationSource();
    }

    //设置定位模式
    private AMapLocationClientOption setMapLocationMode() {
        long timeCycle = 2000;
        AMapLocationClientOption option = new AMapLocationClientOption();
        //将定位模式设置为运动
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport);
        //设置定位精度为高精度
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位为连续定位模式(2s自动定位一次)
        option.setInterval(timeCycle);
        return option;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //定制定位并销毁定位客户端
        mapLocationClient.stopLocation();
        mapLocationClient.onDestroy();
    }
}
