package com.kong.nightrunning;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class NightRunningMapLocation {

    AMapLocationClient mapLocationClient = null;

    NightRunningMapLocation(Context applicationContext){
        mapLocationClient = new AMapLocationClient(applicationContext);
        //定位回调监听器
        AMapLocationListener mapLocationListener = new NightRunningService();
        //在定位客户端设置定位回调监听器
        mapLocationClient.setLocationListener(mapLocationListener);
        mapLocationClient.setLocationOption(setMapLocationMode());
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

    //启动
    public void startLocation(){
        mapLocationClient.stopLocation();
        mapLocationClient.startLocation();
    }

    //销毁
    public void destroyLocation(){
        mapLocationClient.stopLocation();
        mapLocationClient.onDestroy();
    }

}
