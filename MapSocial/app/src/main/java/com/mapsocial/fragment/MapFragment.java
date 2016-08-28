package com.mapsocial.fragment;

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
import com.gy.utils.log.LogUtils;
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
    }

    private void initMap () {
        BaiduMap map = mMapView.getMap();

        //设置为普通定位图标
        map.setMyLocationConfigeration(
                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));

        //设置位置监听
        map.setMyLocationEnabled(true);
        LocationClient mLocationClient = new LocationClient(mActivity);
        mLocationClient.registerLocationListener(locationListener);
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setOpenGps(true);
        locationClientOption.setCoorType("bd09ll");
        locationClientOption.setScanSpan(1000);
        mLocationClient.setLocOption(locationClientOption);
        mLocationClient.start();

        //设置地图缩放级别和地图俯视角
        MapStatus mapStatus = new MapStatus.Builder(map.getMapStatus()).overlook(-30).zoom(18).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        map.animateMapStatus(mapStatusUpdate);
    }

    private boolean isFirstLoc = true;
    private BDLocationListener locationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mMapView == null) return;

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100)
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();
            BaiduMap map = mMapView.getMap();
            map.setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll);
                map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    };

    @Override
    protected BaseFragmentActivityController instanceController() {
        return new MainActivityCtrl(mActivity);
    }
}
