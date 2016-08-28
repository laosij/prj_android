package com.mapsocial.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.gy.appbase.controller.BaseFragmentActivityController;
import com.gy.appbase.fragment.BaseFragment;
import com.gy.appbase.inject.ViewInject;
import com.mapsocial.R;
import com.mapsocial.constant.Consts;
import com.mapsocial.controller.MainActivityCtrl;

/**
 * Created by ganyu on 2016/8/3.
 *
 */
public class MapFragment extends BaseFragment{

    @ViewInject (R.id.tv_title)     TextView mTvTitle;
    @ViewInject (R.id.map_baiduMap) MapView mMapView;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    protected void initViews(View view, Bundle savedInstanceState) {
        mTvTitle.setTypeface(Consts.getTypefaceGirl(mActivity));
        mTvTitle.setText(getString(R.string.app_name));

        initMap();
        startLocationClient();
        registSensor();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            stopLocationClient();
            unregistSensor();
        } else {
            startLocationClient();
            registSensor();
        }
    }


    /**************************** 百度地图位置服务 ************************************/
    private LocationClient mLocationClient = null;

    private void startLocationClient () {
        if (mLocationClient == null) initMap();
        mLocationClient.start();
    }

    private void stopLocationClient () {
        if (mLocationClient != null) mLocationClient.stop();
    }

    private void initMap () {
        BaiduMap map = mMapView.getMap();

        //设置为普通定位图标
        map.setMyLocationConfigeration(
                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));

        //设置位置监听
        map.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(mActivity);
        mLocationClient.registerLocationListener(locationListener);
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setOpenGps(true);
        locationClientOption.setCoorType("bd09ll");
        locationClientOption.setScanSpan(1000);
        mLocationClient.setLocOption(locationClientOption);
    }

    private boolean isFirstLoc = true;
    private BDLocationListener locationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mMapView == null) return;

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(orentation)
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();
            BaiduMap map = mMapView.getMap();
            map.setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                //设置地图缩放级别和地图俯视角
                MapStatus mapStatus = new MapStatus.Builder(map.getMapStatus())
                        .overlook(-30).zoom(18).target(ll).build();
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                map.animateMapStatus(mapStatusUpdate);
            }
        }
    };

    /**************************** 方将监听和计算 ************************************/
    private void registSensor () {
        SensorManager sensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(sensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregistSensor () {
        SensorManager sensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener);
    }

    private float orentation = 0;
    private long lastCalculateTime = 0;
    private float[] accValues = new float[3];
    private float[] magValues = new float[3];
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accValues = event.values;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magValues = event.values;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCalculateTime > 500) {
                calculateOrentation();
                lastCalculateTime = currentTime;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void calculateOrentation () {
        float[] values = new float[3];
        float[] matrix = new float[9];
        SensorManager.getRotationMatrix(matrix, null, accValues, magValues);
        SensorManager.getOrientation(matrix, values);
        orentation = (float) Math.toDegrees(values[0]);
    }

    /**************************** TODO 添加罗盘 ************************************/

    /**************************** TODO 添加附近 ************************************/

    /**************************** TODO 附近详情 ************************************/

    @Override
    protected BaseFragmentActivityController instanceController() {
        return new MainActivityCtrl(mActivity);
    }
}
