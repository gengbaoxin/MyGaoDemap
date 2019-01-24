package com.example.mymap;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

/**
 * Created by Administrator on 2019/1/21.
 */

public class GaoDeMapActivity extends Activity implements LocationSource,
        AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener, AMap.OnCameraChangeListener, View.OnClickListener {

    private MapView mv;
    private TextView jingdu, weidu, weizhi, jingweizhi;
    private TextView Cjingdu, Cweidu, Cweizhi;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    MarkerOptions markerOption;
    LatLng latLng;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private StringBuffer buffer;
    private GeocodeSearch geocoderSearch;
    private String Sweidu, Sjingdu;
    private String SCweidu, SCjingdu;
    private Double dWeidu, dJingdu;
    private LatLonPoint lp;
    private ImageView iv_center_location, iv_search;
    private Marker marker;
    private ObjectAnimator mTransAnimator;//地图中心标志动态
    private static final int SEARCHREQUESTCODE = 1001;
    private PoiItem userSelectPoiItem;
    private boolean isSearchData = false;//是否搜索地址数据
    private float zoom = 14;//地图缩放级别
    private int searchAllPageNum;//Poi搜索最大页数，可应用于上拉加载更多
    private int searchNowPageNum;//当前poi搜索页数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaode);
        mv = findViewById(R.id.map);
        jingdu = findViewById(R.id.jingdu);
        weidu = findViewById(R.id.weidu);
        weizhi = findViewById(R.id.weizhi);
        jingweizhi = findViewById(R.id.jingweizhi);
        Cjingdu = findViewById(R.id.Cjingdu);
        Cweidu = findViewById(R.id.Cweidu);
        Cweizhi = findViewById(R.id.Cweizhi);
        iv_search = findViewById(R.id.iv_search);
        iv_center_location = findViewById(R.id.iv_center_location);
        iv_search.setOnClickListener(this);
        mTransAnimator = ObjectAnimator.ofFloat(iv_center_location, "translationY", 0f, -80f, 0f);
        mTransAnimator.setDuration(800);
        mv.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    private void init() {
        if (aMap == null) {
            aMap = mv.getMap();
            setUpMap();
        }

    }

    private void setUpMap() {
        aMap.setOnCameraChangeListener(this);// 对amap添加移动地图事件监听器
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.point5));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(2);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {//长按拖动开始
//                ToastUtil.showLong("开始得到经纬度");
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                //拖动中
                jingdu.setText(marker.getPosition().latitude + "");
                weidu.setText(marker.getPosition().longitude + "");
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //拖动结束
                weidu.setText(marker.getPosition().latitude + "");
                jingdu.setText(marker.getPosition().longitude + "");
                Sweidu = weidu.getText().toString().trim();
                Sjingdu = jingdu.getText().toString().trim();
                dWeidu = Double.parseDouble(Sweidu);
                dJingdu = Double.parseDouble(Sjingdu);
                Log.i("dove", dWeidu + "" + dJingdu);
                geocoderSearch = new GeocodeSearch(GaoDeMapActivity.this);
                geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                    @Override
                    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                        String formatAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                        weizhi.setVisibility(View.GONE);
                        jingweizhi.setVisibility(View.VISIBLE);
                        jingweizhi.setText(formatAddress);
                        Log.e("dove", "formatAddress:" + formatAddress);
                        Log.e("dove", "rCode:" + i);
                    }

                    @Override
                    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

                    }
                });
                LatLonPoint lp = new LatLonPoint(dWeidu, dJingdu);
                RegeocodeQuery query = new RegeocodeQuery(lp, 200, GeocodeSearch.AMAP);
                geocoderSearch.getFromLocationAsyn(query);
//                Intent intent = new Intent(GaoDeMapActivity.this, MainActivity.class);
//                intent.putExtra("gaodejingdu", marker.getPosition().longitude + "");
//                intent.putExtra("gaodeweidu", marker.getPosition().latitude + "");
//                setResult(1, intent);
            }
        });
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mv.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mv.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mv.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mv.onDestroy();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                //定位成功回调信息，设置相关消息
                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(aMapLocation);
                    //获取定位信息
                    buffer = new StringBuffer();
                    addMarkerToMap(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    buffer.append(aMapLocation.getCountry() + ""
                            + aMapLocation.getProvince() + ""
                            + aMapLocation.getCity() + ""
                            + aMapLocation.getProvince() + ""
                            + aMapLocation.getDistrict() + ""
                            + aMapLocation.getStreet() + ""
                            + aMapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    weizhi.setText(buffer.toString());
                    isFirstLoc = false;
                    weidu.setText(aMapLocation.getLatitude() + "");
                    jingdu.setText(aMapLocation.getLongitude() + "");
//                    Intent intent = new Intent(GaoDeMapActivity.this, MainActivity.class);
//                    intent.putExtra("gaodeweidu", aMapLocation.getLatitude() + "");
//                    intent.putExtra("gaodejingdu", aMapLocation.getLongitude() + "");
//                    setResult(1, intent);

                }
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    private void addMarkerToMap(double jingdu, double weidu) {
        latLng = new LatLng(jingdu, weidu);
        markerOption = new MarkerOptions();
        markerOption.position(latLng);
        Marker marker = aMap.addMarker(markerOption);
        marker.setDraggable(true);
//        marker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                .decodeResource(getResources(), R.drawable.poi_marker_pressed)));
        marker.showInfoWindow();
        marker.setRotateAngle(30);
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != data && SEARCHREQUESTCODE == requestCode) {
            try {
                userSelectPoiItem = (PoiItem) data.getParcelableExtra(DatasKey.SEARCH_INFO);
                if (null != userSelectPoiItem) {
                    isSearchData = false;
//                    doSearchQuery(true, "", location.getCity(), userSelectPoiItem.getLatLonPoint());
                    moveMapCamera(userSelectPoiItem.getLatLonPoint().getLatitude(), userSelectPoiItem.getLatLonPoint().getLongitude());
//                    refleshMark(userSelectPoiItem.getLatLonPoint().getLatitude(), userSelectPoiItem.getLatLonPoint().getLongitude());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 把地图画面移动到定位地点(使用moveCamera方法没有动画效果)
     *
     * @param latitude
     * @param longitude
     */
    private void moveMapCamera(double latitude, double longitude) {
        if (null != aMap) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Cjingdu.setText(cameraPosition.target.latitude + "");
        Cweidu.setText(cameraPosition.target.longitude + "");
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Cjingdu.setText(cameraPosition.target.latitude + "");
        Cweidu.setText(cameraPosition.target.longitude + "");
        startTransAnimator();
        SCjingdu = Cjingdu.getText().toString().trim();
        SCweidu = Cweidu.getText().toString().trim();
        dWeidu = Double.parseDouble(SCjingdu);
        dJingdu = Double.parseDouble(SCweidu);
        geocoderSearch = new GeocodeSearch(GaoDeMapActivity.this);
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                String formatAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                weizhi.setVisibility(View.GONE);
//                jingweizhi.setVisibility(View.VISIBLE);
                Cweizhi.setText(formatAddress);
                Log.e("dove", "formatAddress:" + formatAddress);
                Log.e("dove", "rCode:" + i);
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        lp = new LatLonPoint(dWeidu, dJingdu);
        RegeocodeQuery query = new RegeocodeQuery(lp, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    /**
     * 移动动画
     */
    private void startTransAnimator() {
        if (null != mTransAnimator && !mTransAnimator.isRunning()) {
            mTransAnimator.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_search:
                startActivityForResult(new Intent(GaoDeMapActivity.this, SearchActivity.class), SEARCHREQUESTCODE);
                break;
        }
    }
}