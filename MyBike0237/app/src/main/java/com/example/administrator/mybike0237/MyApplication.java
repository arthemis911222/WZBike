package com.example.administrator.mybike0237;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.SDKInitializer;
import com.example.administrator.mybike0237.bikefile.Bike;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/19.
 */
public class MyApplication extends Application {
    private List<Bike> bikeList = new ArrayList<Bike>();
    private static Application app;
    private DatabaseHelper myDB;
    public static final int SELECT_TO_MAIN = 3;
    private Handler mhandler;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        myDB = new DatabaseHelper(this,"myDatabase",null,1);
        //百度地图需要
        SDKInitializer.initialize(getApplicationContext());
    }

    public List<Bike> getBikeList() {
        return bikeList;
    }

    public void setBikeList(List<Bike> bikeList) {
        this.bikeList = bikeList;
    }

    public static Application getApp() {
        return app;
    }

    public DatabaseHelper getMyDB() {
        return myDB;
    }

    public void setMainHandler(Handler handler){
        mhandler=handler;
    }

    public void flushMain(Message msg){
        mhandler.sendMessage(msg);
    }
}
