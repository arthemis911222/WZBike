package com.example.administrator.mybike0237;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ant.liao.GifView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.example.administrator.mybike0237.bikefile.Bike;
import com.example.administrator.mybike0237.offlineMap.OffMapActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity0237 extends Activity {
    private List<Bike> bikeList = new ArrayList<Bike>();
    private TextView tv_search;
    private MapView mapView;
    private MyApplication app = (MyApplication)MyApplication.getApp();
    private static final int UPDATE_VIEW = 1;//点击更新视图消息
    private static final int UPDATE_DATABASE = 2;//更新数据库消息
    private static final int DRAW_MARKER = 4;
    private static final int WRITE_COARSE_LOCATION_REQUEST_CODE = 1;//定位权限
    //private static final int UPDATE_MAPVIEW = 5;
    private LinearLayout ll_loading2;
    private GifView gf_loading2;

    private LinearLayout barSearch,btnTool;
    private Button btnnowLoc;
    private Button btn_find;
    private boolean mapviewFlag = true;//控件显示标志
    private boolean infoviewFlag = false;//信息窗显示标志
    private boolean mapviewAction = true;//点击地图相应是否开启

    private BitmapDescriptor bitmap1,bitmap2;

    //infoWindow
    private RelativeLayout rl_info;
    private TextView tvInfoName,tvInfoDis,tvInfoAvai,tvInfoStop;

    //查询需要
    private int iFanwei,iHuanche,iJieche;
    private TextView tv_fanwei,tv_huanche,tv_jieche;
    private LatLng findPoint;
    private int findPos;
    private int findFlag = 0;

    //定位
    private LocationClient locationClient = null;
    private BDLocationListener listener = new MyLocationListener();
    private BaiduMap baiduMap;

    private LatLng nowLocation = null;//定位点
    private Marker nowMarker = null;//定位marker
    private List<Marker> allMarker = new ArrayList<>();//自行车点marker集合

    //离线下载
    private ImageView ivBtnDown;

    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_VIEW:
                    //ll_loading2.setVisibility(View.GONE);
                    popInforWin((Marker)msg.obj);
                    break;
                case UPDATE_DATABASE:
                    updateDatabase((Bike)msg.obj);
                    break;
                case MyApplication.SELECT_TO_MAIN:
                    getFromSelect((Bike)msg.obj,msg.arg1);
                    break;
                case DRAW_MARKER:
                {
                    ll_loading2.setVisibility(View.GONE);
                    if(findFlag == 0 && findPoint != null){
                        //setMapOverlay(findPoint,findPos,true);
                        allMarker.get(findPos).setVisible(true);
                        setCenterLoc(findPoint);
                        popInforWin(allMarker.get(findPos));
                    }else if (findFlag == 0 && findPoint == null)
                        Toast.makeText(MainActivity0237.this,"没有找到……",Toast.LENGTH_LONG).show();

                }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main0237);

        //给app注册Handler
        app.setMainHandler(mhandler);

        setGPS();

        initView();//初始化空间和监听

        initBikeMarker();//申请一个线程打点

    }

    public void initBikeMarker(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i < bikeList.size();i++){
                    setMapOverlay(new LatLng(bikeList.get(i).getLat(),bikeList.get(i).getLng()),i,true);
                }
            }
        }).start();
    }

    //初始化
    public void initView() {
        bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.gps_img);
        bitmap2 = BitmapDescriptorFactory.fromResource(R.drawable.marker2);//相同点使用同一个 BitmapDescriptor 对象以节省内存空间

        bikeList = app.getBikeList();

        tv_search = (TextView)findViewById(R.id.tv_main_bike);

        mapView = (MapView) findViewById(R.id.bmapView);

        barSearch = (LinearLayout)findViewById(R.id.bar_search);
        btnTool = (LinearLayout)findViewById(R.id.ll_threeBtn);

        baiduMap = mapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));//地图缩放级别为17

        btn_find = (Button)findViewById(R.id.btn_find);

        tvInfoName = (TextView)findViewById(R.id.tv_info_name);
        tvInfoDis = (TextView)findViewById(R.id.tv_info_address);
        tvInfoAvai = (TextView)findViewById(R.id.tv_info_avai);
        tvInfoStop = (TextView)findViewById(R.id.tv_info_stop);
        rl_info = (RelativeLayout)findViewById(R.id.rl_infomation);

        iFanwei = iJieche = iHuanche = Integer.MAX_VALUE;
        tv_fanwei = (TextView)findViewById(R.id.tv_fanwei);
        tv_huanche = (TextView)findViewById(R.id.tv_huanche);
        tv_jieche = (TextView)findViewById(R.id.tv_jieche);

        ll_loading2 = (LinearLayout)findViewById(R.id.ll_loading2);
        gf_loading2 = (GifView)findViewById(R.id.gf_loading2);
        gf_loading2.setGifImage(R.drawable.loading2);

        ivBtnDown = (ImageView)findViewById(R.id.iv_btn_down);

        initEvent();
    }

    //定位
    public void setGPS(){
        //定位
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(listener);
        initLocation();
    }
    // 初始化定位参数
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=10*1000;
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        locationClient.setLocOption(option);
        locationClient.start();
    }

    // 定位监听
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation loc) {
            baiduMap.setMyLocationEnabled(true);
            //将获取的信息传给地图
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(loc.getRadius())
                    .direction(100)
                    .latitude(loc.getLatitude())
                    .longitude(loc.getLongitude())
                    .build();
            baiduMap.setMyLocationData(data);

            if (loc != null && (loc.getLocType() == 161 || loc.getLocType() == 66)) {
                //这里得到BDLocation就是定位出来的信息了

                nowLocation = new LatLng(loc.getLatitude(),loc.getLongitude());

                //LatLng point = new LatLng(loc.getLatitude(), loc.getLongitude());
                setMapOverlay(nowLocation,-1,false);
                baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nowLocation));

                mapView.refreshDrawableState();

                //locationClient.setOpenAutoNotifyMode(60*60*1000,5000,LocationClientOption.LOC_SENSITIVITY_LOW);

            } else {
                getPermission();
            }
        }

    }

    // 在地图上添加标注,分动画和无动画
    private void setMapOverlay(LatLng point,int pos,boolean flag) {

        if(pos == -1)
        {
            //MarkerOptions  option = new MarkerOptions().position(point).icon(bitmap1);
            //nowMarker = (Marker) (baiduMap.addOverlay(option));
            //drawBox();
        }
        else{
            MarkerOptions  option = new MarkerOptions().position(point).icon(bitmap2);
            if (flag) {
                // 生长动画
                option.animateType(MarkerOptions.MarkerAnimateType.grow);
            }
            Marker  mMarker = (Marker) (baiduMap.addOverlay(option));

            Bundle bundle = new Bundle();
            bundle.putInt("pos",pos);
            mMarker.setExtraInfo(bundle);

            allMarker.add(mMarker);
        }
    }

    //清除非定位的点
    public void removeAllMarker(){
        for(int i=0; i<allMarker.size();i++){
            //allMarker.get(i).remove();
            allMarker.get(i).setVisible(false);
        }
        //allMarker.clear();
    }

    //根据输入条件加覆盖物
    public void getNearBike(List<Bike> lst, LatLng now){
        int max = iFanwei;
        findPoint = null;
        findFlag = 0;
        for(int i = 0;i < lst.size();i++){
            Bike b = lst.get(i);
            double x = b.getLat();
            double y = b.getLng();
            LatLng p = new LatLng(x,y);
            int avaibike = b.getAvailBike();
            int stopbike = b.getStopBike();

            if(!SpatialRelationUtil.isCircleContainsPoint(now,max,p)){
                allMarker.get(i).setVisible(false);
                continue;
            }

            if(iHuanche == Integer.MAX_VALUE && iJieche == Integer.MAX_VALUE){
                //setMapOverlay(p,i,true);
                allMarker.get(i).setVisible(true);
                findFlag = 1;
            }else if(iHuanche == Integer.MAX_VALUE && iJieche < avaibike){
                max = (int) DistanceUtil.getDistance(nowLocation,p);
                findPoint = p;
                findPos = i;
            }else if(iJieche == Integer.MAX_VALUE && iHuanche < stopbike){
                max = (int) DistanceUtil.getDistance(nowLocation,p);
                findPoint = p;
                findPos = i;
            }else  if( iJieche < avaibike && iHuanche < stopbike){
                max = (int) DistanceUtil.getDistance(nowLocation,p);
                findPoint = p;
                findPos = i;
            }else {
                allMarker.get(i).setVisible(false);
            }
        }
    }

    //监听
    public void initEvent(){
        tv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity0237.this,SelectBike.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

        //点击按钮回到定位点
        btnnowLoc = (Button)findViewById(R.id.btn_nowLoc);
        btnnowLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCenterLoc(nowLocation);
            }
        });

        //点击地图监听
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                updateMapView();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        //点击marker监听
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!marker.isVisible())
                    return false;

                readOneData(marker);
                setCenterLoc(marker.getPosition());
                return true;
            }
        });

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setThreeBtnList();
            }
        });

        ivBtnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity0237.this,OffMapActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

    }

    //响应按钮点击，弹出对话框
    public void setThreeBtnList(){
        final LayoutInflater inflater = LayoutInflater.from(MainActivity0237.this);
        final View myview = inflater.inflate(R.layout.search_dialog,null);

        final EditText ev1 = (EditText)myview.findViewById(R.id.et_fanwei);
        final EditText ev2 = (EditText)myview.findViewById(R.id.et_huanche);
        final EditText ev3 = (EditText)myview.findViewById(R.id.et_jieche);

        if(iFanwei != Integer.MAX_VALUE)
            ev1.setText(""+iFanwei);
        if(iHuanche != Integer.MAX_VALUE)
            ev2.setText(""+iHuanche);
        if(iJieche != Integer.MAX_VALUE)
            ev3.setText(""+iJieche);

        AlertDialog dialog = new AlertDialog.Builder(MainActivity0237.this)
                .setTitle("查询条件设置")
                .setView(myview)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        iFanwei = initEdit(ev1,tv_fanwei,"范围：","米");
                        iHuanche = initEdit(ev2,tv_huanche,"还车：","辆");
                        iJieche = initEdit(ev3,tv_jieche,"借车：","辆");

                        if(infoviewFlag == true){
                            infoviewFlag = false;
                            mapviewAction = true;
                            rl_info.setVisibility(View.GONE);
                            rl_info.setAnimation(AnimationUtil.moveToViewBottom(btnnowLoc.getY()));
                            btnnowLoc.setAnimation(AnimationUtil.moveToViewBottom(btnnowLoc.getY()));
                        }

                        ll_loading2.setVisibility(View.VISIBLE);
                        ceshi();

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();

        dialog.show();

    }
    public int initEdit(EditText editText,TextView textView,String left,String right){
        String text = editText.getText().toString();
        Pattern p = Pattern.compile("[^0-9]");
        Matcher m = p.matcher(text);
        String res = m.replaceAll("").trim();

        if(res.isEmpty()){
            textView.setText(left+"无限制");
            return Integer.MAX_VALUE;
        }
        else {
            textView.setText(left + res + right);
            return Integer.parseInt(res);
        }
    }
    public void ceshi(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //removeAllMarker();

                getNearBike(bikeList,nowLocation);

                Message msg = new Message();
                msg.what = DRAW_MARKER;
                mhandler.sendMessage(msg);
            }
        }).start();
    }

    //点击地图控件消失，再点击出现
    public void updateMapView(){
        if(mapviewAction){
            if(mapviewFlag){
                barSearch.setVisibility(View.GONE);
                barSearch.setAnimation(AnimationUtil.moveToViewTop());
                btnTool.setVisibility(View.GONE);
                btnTool.setAnimation(AnimationUtil.moveToViewRight());
                mapviewFlag = false;
            }else {
                barSearch.setVisibility(View.VISIBLE);
                barSearch.setAnimation(AnimationUtil.moveToViewLocation());
                btnTool.setVisibility(View.VISIBLE);
                btnTool.setAnimation(AnimationUtil.moveToViewLocationRight());
                mapviewFlag = true;
            }
        }else {
            infoviewFlag = false;
            rl_info.setVisibility(View.GONE);
            rl_info.setAnimation(AnimationUtil.moveToViewBottom(btnnowLoc.getY()));
            btnnowLoc.setAnimation(AnimationUtil.moveToViewBottom(btnnowLoc.getY()));
            mapviewAction = true;
        }

    }

    //更新地图中心位置
    public void setCenterLoc(LatLng center){
        MapStatus mapStatus = new MapStatus.Builder().target(center).zoom(17).build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        //改变地图状态
        baiduMap.animateMapStatus(mMapStatusUpdate);
    }

    //跳出信息框
    public void popInforWin(Marker marker){
        Bundle bundle = marker.getExtraInfo();
        int pos = bundle.getInt("pos");
        //getMarkerInfo(pos);
        int distance = (int)DistanceUtil.getDistance(nowLocation,marker.getPosition());

        tvInfoName.setText(bikeList.get(pos).getName());
        tvInfoDis.setText(distance + "米 | " + bikeList.get(pos).getAddress());
        tvInfoAvai.setText("可借车："+bikeList.get(pos).getAvailBike());
        tvInfoStop.setText("停车位："+bikeList.get(pos).getStopBike());

        infoviewFlag = true;
        mapviewAction = false;

        rl_info.setVisibility(View.VISIBLE);
        rl_info.setAnimation(AnimationUtil.moveToViewLocationBottom(rl_info.getHeight()));
        btnnowLoc.setAnimation(AnimationUtil.moveToViewLocationBottom(rl_info.getHeight()));

    }

    //从网络读取单个数据，实现实时更新
    public void readOneData(Marker marker){
        Bundle bundle = marker.getExtraInfo();
        int pos = bundle.getInt("pos");
        String nowid = bikeList.get(pos).getId();

        //getMarkerInfo(pos);

        if (NetUtils.isConnected(MainActivity0237.this) != false) {
            readFromNet(nowid,pos,marker);
        }else
        {
           popInforWin(marker);
        }
    }
    public void readFromNet(final String nowid, final int pos, final Marker marker){
        //ll_loading2.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                readJson(nowid,pos);

                //向主线程传递更新视图消息
                Message msg1 = new Message();
                msg1.obj = marker;
                msg1.what = UPDATE_VIEW;
                mhandler.sendMessage(msg1);

            }
        }).start();
    }
    public void readJson(String nowid,int pos){
        String uri = "http://218.93.33.59:85/map/wzmap/ibikestation.asp?id="+nowid;
        String ss = loadJson(uri);

        if(ss == null){
            return;
        }

        String s = ss.replaceAll("var isinglebike = ","");

        try {
            JSONObject json = new JSONObject(s);
            JSONArray bike = json.getJSONArray("station");

            JSONObject temp = bike.getJSONObject(0);
            int a = Integer.parseInt(temp.getString("availBike"));
            int b = Integer.parseInt(temp.getString("capacity"));
            int result = b-a;

            bikeList.get(pos).setAvailBike(a);
            bikeList.get(pos).setStopBike(result);
            //同步更新app
            app.getBikeList().get(pos).setAvailBike(a);
            app.getBikeList().get(pos).setStopBike(result);

            //Log.d(temp.getString("availBike"),temp.getString("capacity"));

            //向数据库传递更新消息
            Message msg = new Message();
            msg.obj = bikeList.get(pos);
            msg.what = UPDATE_DATABASE;
            mhandler.sendMessage(msg);

        } catch (JSONException e) {
            System.out.println("网站响应不是json格式，无法转化成JSONObject!");
        }

    }
    public String loadJson (String url) {
        String body = null;
        try {
            Connection.Response res = Jsoup.connect(url)
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")
                    .timeout(1000).ignoreContentType(true).execute();
            body = res.body();
        } catch (SocketTimeoutException e) {//erro???
            System.out.println("连接超时!!!");
        } catch (Exception e) {
            System.out.println("连接网址不对或读取流出现异常!!!");
        }
        return body;
    }

    public void updateDatabase(Bike bike){
        app.getMyDB().update(bike.getId(),bike.getAvailBike(),bike.getStopBike());
    }

    public void getFromSelect(Bike bike,int pos){
        removeAllMarker();
        LatLng temp = new LatLng(bike.getLat(),bike.getLng());
        //setMapOverlay(temp,pos,true);
        allMarker.get(pos).setVisible(true);
        popInforWin(allMarker.get(pos));
        setCenterLoc(temp);
        infoviewFlag = true;
        mapviewAction = false;
    }

    @Override
    protected void onDestroy() {
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        //退出时销毁定位
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);

        super.onDestroy();
        mapView.onDestroy();
        mapView = null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    /*
    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        baiduMap.setMyLocationEnabled(true);
    }
    */

    //询问定位权限
    public void getPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        }
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        if(requestCode == WRITE_COARSE_LOCATION_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                //定位
                setGPS();
            }
        }
    }
}

//动画类
class AnimationUtil {
    private static final String TAG = AnimationUtil.class.getSimpleName();

    public static TranslateAnimation moveToViewTop() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.5f);
        mHiddenAction.setDuration(500);
        return mHiddenAction;
    }

    public static TranslateAnimation moveToViewLocation() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.5f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(500);
        return mHiddenAction;
    }

    public static TranslateAnimation moveToViewRight() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.5f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(500);
        return mHiddenAction;
    }

    public static TranslateAnimation moveToViewLocationRight() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(500);
        return mHiddenAction;
    }

    public static TranslateAnimation moveToViewBottom(float y) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.ABSOLUTE, 0);
        mHiddenAction.setDuration(500);
        return mHiddenAction;
    }

    public static TranslateAnimation moveToViewLocationBottom(int height) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE,
                height, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(500);
        return mHiddenAction;
    }
}
