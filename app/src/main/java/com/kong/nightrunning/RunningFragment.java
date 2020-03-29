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
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;

//跑步模式Fragment
public class RunningFragment extends Fragment {

//    在用户进行跑步模式时，需要手机常亮。保证安全。
    NightRunningMapLocation mapLocation=null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);
        MapView mapView = view.findViewById(R.id.map);
        // 此方法必须重写
        mapView.onCreate(savedInstanceState);
        setMapStyle(mapView);
        mapLocation=new NightRunningMapLocation(getActivity().getApplicationContext());
        mapLocation.startLocation();
        return view;
    }

    //修稿地图样式
    private void setMapStyle(MapView mapView) {
        //获取地图样式
        long timeCycle = 2000;
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.interval(timeCycle);
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        //设置地图风格
        mapView.getMap().setMyLocationStyle(locationStyle);
        mapView.getMap().setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mapView.getMap().moveCamera(CameraUpdateFactory.zoomTo(17));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mapLocation.destroyLocation();
    }
}
