package com.hong.gdmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LocationSource,AMapLocationListener  定位
 * OnMapClickListener 地图点击事件
 * OnGeocodeSearchListener 地理编码
 * 所有的结果都通过回调函数获得
 */
public class MainActivity extends AppCompatActivity implements LocationSource,AMapLocationListener,AMap.OnMapClickListener,GeocodeSearch.OnGeocodeSearchListener{

    private AMap aMap;
    private MapView mapView;
    //定位发起端
    private AMapLocationClient aMapLocationClient;
    //定位参数
    private AMapLocationClientOption mapLocationClientOption;
    private double latitude;
    private double longitude;
    //定位监听器
    private LocationSource.OnLocationChangedListener listener;
    private boolean isFirst = true;

    //
    private GeocodeSearch geocodeSearch;
    //逆地理编码查询
    private RegeocodeQuery regeocodeQuery = null;

    private GeocodeQuery geocodeQuery = null;
    private TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions();
        setTitle("商家选址");
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        init();
        initLoc();
    }

    public void init(){

        if (aMap == null){
            aMap = mapView.getMap();

            UiSettings settings = aMap.getUiSettings();

            aMap.setLocationSource(this);

            settings.setMyLocationButtonEnabled(true);

            aMap.setMyLocationEnabled(true);

            //定位的小图标
            MyLocationStyle myLocationStyle = new MyLocationStyle();
//            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_camera_location));
            myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setMyLocationEnabled(true);
            //设置地图点击监听
            aMap.setOnMapClickListener(this);
            //设置地理编码监听
            geocodeSearch = new GeocodeSearch(this);
            geocodeSearch.setOnGeocodeSearchListener(this);

        }
    }


    /**
     * 定位
     */
    private void initLoc(){
        //初始化定位
        aMapLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        aMapLocationClient.setLocationListener(this);
        //初始化定位参数
        mapLocationClientOption = new AMapLocationClientOption();
        //定位模式：高精度
        mapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息
        mapLocationClientOption.setNeedAddress(true);
        //设置是否只定位一次，默认为false
        mapLocationClientOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mapLocationClientOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mapLocationClientOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mapLocationClientOption.setInterval(2000);
        //给定位客户端设置定位参数
        aMapLocationClient.setLocationOption(mapLocationClientOption);
        //启动定位
        aMapLocationClient.startLocation();

    }

    /**
     * 定位回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0){
                //定位成功回调信息。设置相关信息
                //获取当前定位结果来源。如网络定位结果，详见官方定位类型表
                aMapLocation.getLocationType();
                //获取维度
                aMapLocation.getLatitude();
                //获取精度
                aMapLocation.getLongitude();
                //获取精度信息
                aMapLocation.getAccuracy();
                //定位时间
                SimpleDateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                dateFormat.format(date);
                //地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getAddress();
                //国家、省、市、地图、街道、门牌号、城市编码，地区编码
                aMapLocation.getCountry();
                aMapLocation.getProvince();
                aMapLocation.getCity();
                aMapLocation.getDistrict();
                aMapLocation.getStreet();
                aMapLocation.getStreetNum();
                aMapLocation.getCityCode();
                aMapLocation.getAdCode();
                if (isFirst){
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude())));
                    //点击定位按钮  能够将地图的中心移动到定位点
                    listener.onLocationChanged(aMapLocation);
                    //添加图钉
//                    aMap.addMarker(getMarkerOptions(aMapLocation));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(aMapLocation.getCountry() + "" + aMapLocation.getProvince() + "" + aMapLocation.getCity() + "" + aMapLocation.getProvince() + "" + aMapLocation.getDistrict() + "" + aMapLocation.getStreet() + "" + aMapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    tvLocation.setText(aMapLocation.getAoiName());
                    isFirst= false;

                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());

                    //Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();

                }
            }
        }

    }

    //自定义图钉
    private MarkerOptions getMarkerOptions(AMapLocation aMapLocation){
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_camera_location));
        //位置
        options.position(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()));
        StringBuffer buffer = new StringBuffer();
        buffer.append(aMapLocation.getCountry()+""+aMapLocation.getProvince()+""+aMapLocation.getCity()
                +""+aMapLocation.getDistrict()+""+aMapLocation.getStreet()+""+aMapLocation.getStreetNum());
        //标题
        options.title(buffer.toString());
        //子标题
        options.snippet("测试");
        //设置多少帧舒心一次地图资源
        options.period(60);
        return options;


    }
    /**
     * 6.0权限 提示用户打开定位权限
     */

    public void permissions(){
        //6.0权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);//自定义的code
        }
    }

    /**
     * 地图点击事件
     */
    @Override
    public void onMapClick(LatLng latLng) {
        aMap.clear();
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        //创建覆盖物
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_camera_location));
        markerOptions.position(latLng);
        aMap.addMarker(markerOptions);
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        //根据地图点击获得的经纬度调用方法查询详细地址，得到结果在回调函数里
        onByLat(latLng);
    }


    /**
     * 根据经纬度获取地址
     *
     */
    public void onByLat(LatLng latLng){
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude,latLng.longitude);
        //设置查询参数  第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        regeocodeQuery = new RegeocodeQuery(latLonPoint,200,GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(regeocodeQuery);
    }

    /**
     *
     * 根据地址获得经纬度
     */
    public void onByAddress(String name,String city){
        //设置查询参数 name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
        geocodeQuery = new GeocodeQuery(name,city);
        geocodeSearch.getFromLocationNameAsyn(geocodeQuery);
    }

    //逆地理编码获取回调信息
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i==1000){
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            String formatAddress = regeocodeAddress.getFormatAddress();
            Toast.makeText(getApplicationContext(), formatAddress, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_LONG).show();
        }

    }
    //正地理编码获取回调信息
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        if (i == 1000){
            if (geocodeResult != null && geocodeResult.getGeocodeAddressList()!=null && geocodeResult.getGeocodeAddressList().size()>0){
                GeocodeAddress geocodeAddress= geocodeResult.getGeocodeAddressList().get(0);
                latitude = geocodeAddress.getLatLonPoint().getLatitude();
                longitude = geocodeAddress.getLatLonPoint().getLongitude();
            }else {
                Toast.makeText(getApplicationContext(), "地址未查询到", Toast.LENGTH_SHORT).show();
            }


        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        listener = onLocationChangedListener;
    }

    //停止定位
    @Override
    public void deactivate() {
        listener = null;
    }

    /**
     * 三个生命周期
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
/*        mLocationClient.stopLocation();
        mLocationClient.onDestroy();*/
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
}
