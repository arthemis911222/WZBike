package com.example.administrator.mybike0237;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import com.ant.liao.GifView;
import com.example.administrator.mybike0237.bikefile.Bike;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/20.
 */
public class StartActivity extends Activity {

    private static final int UPDATE_ALL = 1;
    private MyApplication app = (MyApplication)MyApplication.getApp();
    private GifView gif;

    private List<String> sqls = new ArrayList<>();

    private Handler mHandler = new Handler(){
        public  void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_ALL:
                    turnAction();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.start_layout);

        gif = (GifView)findViewById(R.id.gif_loading);
        gif.setGifImage(R.drawable.loading);

        //有网络就从网络读取数据，否则从数据库读取数据
        if (NetUtils.isConnected(StartActivity.this) != false) {
            initByNet();
        }else
        {
            readFromDatabase();
            turnAction();
            Toast.makeText(StartActivity.this,"网络挂了！", Toast.LENGTH_LONG).show();
        }

    }

    public void turnAction(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(StartActivity.this,MainActivity0237.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        },2*1000);
    }

    //申请一个子线程读取数据（数据来源分为网络和数据库）
    public void initByNet(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                readJson();
                app.getMyDB().insertOrUpdateDate(sqls);//更新数据库

                Message msg = new Message();
                msg.what = UPDATE_ALL;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    //读取网页上数据
    public void readJson(){
        String uri = "http://218.93.33.59:85/map/wzmap/ibikestation.asp";
        String ss = loadJson(uri);

        if(ss == null){
            readFromDatabase();
            return;
        }

        String s = ss.replaceAll("var ibike = ","");

        try {
            JSONObject json = new JSONObject(s);
            JSONArray bike = json.getJSONArray("station");

            int count = 0;
            for(int i=0;i < bike.length();i++){
                JSONObject temp = bike.getJSONObject(i);
                Bike item = new Bike();

                if(temp.getString("name").isEmpty() || temp.getString("lat") == "0" || temp.getString("lng") == "0"
                        || temp.getString("lng") == "1"){
                    app.getMyDB().delete(temp.getString("id"));//如果数据删去了，也更新数据库
                    continue;
                }

                String id = temp.getString("id");
                String name = temp.getString("name");
                String address = temp.getString("address");
                Double lat = Double.parseDouble(temp.getString("lat"));
                Double lng = Double.parseDouble(temp.getString("lng"));
                int a = Integer.parseInt(temp.getString("availBike"));
                int b = Integer.parseInt(temp.getString("capacity"));
                int result = b-a;

                item.setId(id);
                item.setName(name);
                item.setLat(lat);
                item.setLng(lng);
                item.setAvailBike(a);
                item.setAddress(address);
                item.setStopBike(result);
                item.setPos(count);
                item.setPinyin(toHanyuPingyin(name));
                count++;

                //Log.d("index",item.getName());
                app.getBikeList().add(item);

                sqls.add(app.getMyDB().sqlString(id,name,address,lat,lng,a,result));
            }

        } catch (JSONException e) {
            System.out.println("网站响应不是json格式，无法转化成JSONObject!");
            readFromDatabase();
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
                    .timeout(2000).ignoreContentType(true).execute();
            body = res.body();
        } catch (SocketTimeoutException e) {//erro???
            System.out.println("连接超时!!!");
            readFromDatabase();//无法从网络，就从数据库
        } catch (Exception e) {
            System.out.println("连接网址不对或读取流出现异常!!!");
            readFromDatabase();//无法从网络，就从数据库
        }
        return body;
    }

    //无法从网络，就从数据库
    public void readFromDatabase(){
        app.getMyDB().setList();
        app.setBikeList(app.getMyDB().getList());
    }

    //将中文转为英文
    public String toHanyuPingyin(String name){
        Hanyu hanyu = new Hanyu();

        String strPinyin = hanyu.getStringPinYin(name);
        return strPinyin;
    }

}
