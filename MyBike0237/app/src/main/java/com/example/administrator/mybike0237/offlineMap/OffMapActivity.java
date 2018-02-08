package com.example.administrator.mybike0237.offlineMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.example.administrator.mybike0237.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/12/24.
 */
public class OffMapActivity extends Activity{

    private RelativeLayout btn_download;
    private TextView tv_downinfo,tv_city;
    private ImageView btn_back;
    private int downFlag = 0;//记录下载状态,0是未下载，1是停止下载，2是正在下载，3是下载完成
    private MKOfflineMap mOfflineMap;
    private OfflineMapCity downCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_layout);

        tv_city = (TextView)findViewById(R.id.tv_cityname);
        tv_downinfo = (TextView)findViewById(R.id.tv_downinfo);
        btn_download = (RelativeLayout)findViewById(R.id.btn_download);
        btn_back = (ImageView)findViewById(R.id.btn_back);

        initOfflineMap();
        initData();
        initEvent();
    }

    private void initOfflineMap() {
        mOfflineMap = new MKOfflineMap();
        // 设置监听
        mOfflineMap.init(new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int type, int state)
            {
                switch (type)
                {
                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
                        // 离线地图下载更新事件类型
                        MKOLUpdateElement update = mOfflineMap.getUpdateInfo(state);
                        if (downCity.getCityCode() == state)
                        {
                            downCity.setProgress(update.ratio);
                            downFlag = 2;

                            if(downCity.getProgress() == 100){
                                downFlag = 3;
                                tv_downinfo.setText("下载完成");
                            }else {
                                tv_downinfo.setText(downCity.getProgress()+"%");
                            }
                        }
                        break;
                    case MKOfflineMap.TYPE_NEW_OFFLINE:
                        // 有新离线地图安装
                        //Log.e(TAG, "TYPE_NEW_OFFLINE");
                        break;
                    case MKOfflineMap.TYPE_VER_UPDATE:
                        // 版本更新提示
                        break;
                }

            }
        });
    }

    private void initData() {
        // 获取城市可更新列表
        ArrayList<MKOLSearchRecord> offCityList = mOfflineMap.searchCity(tv_city.getText().toString());

        // 获得所有已经下载的城市列表
        ArrayList<MKOLUpdateElement> allUpdateInfo = mOfflineMap.getAllUpdateInfo();

        // 设置所有数据的状态
        MKOLSearchRecord record = offCityList.get(0);
        downCity = new OfflineMapCity();
        downCity.setCityName(record.cityName);
        downCity.setCityCode(record.cityID);

        if(allUpdateInfo != null){
            MKOLUpdateElement ele = allUpdateInfo.get(0);
            if(ele.cityID == record.cityID){
                downCity.setProgress(ele.ratio);

                if(ele.ratio == 100){
                    downFlag = 3;
                    tv_downinfo.setText("已经下载");
                }else if(ele.ratio > 0){
                    downFlag = 1;
                    tv_downinfo.setText("停止下载");
                }
            }
        }

    }

    private void initEvent() {
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cityId = downCity.getCityCode();

                if(downFlag == 0 || downFlag == 1){
                    addToDownloadQueue(cityId);
                }else if (downFlag == 2){
                    removeTaskFromQueue(cityId);
                }

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void removeTaskFromQueue(int cityId) {
        mOfflineMap.pause(cityId);
        downFlag = 1;
        tv_downinfo.setText("停止下载");
    }

    public void addToDownloadQueue(int cityId) {
        mOfflineMap.start(cityId);
        downFlag = 2;
        tv_downinfo.setText("正在下载");
    }

    @Override
    protected void onDestroy() {
        mOfflineMap.destroy();
        super.onDestroy();
    }
}
