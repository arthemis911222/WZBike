package com.example.administrator.mybike0237;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.administrator.mybike0237.bikefile.Bike;
import com.example.administrator.mybike0237.bikefile.BikeAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/18.
 */
public class SelectBike extends Activity {
    private List<Bike> bikeList;
    private ListView bikeListView;
    private BikeAdapter adapter;
    private SearchView searchView;
    private MyApplication app = (MyApplication)MyApplication.getApp();
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);

        bikeList = new ArrayList<Bike>();
        bikeList = app.getBikeList();
        bikeListView = (ListView)findViewById(R.id.lv_bike);
        setAdapter();

        searchView = (SearchView)findViewById(R.id.sv_bike);

        btnBack = (ImageView)findViewById(R.id.iv_btnBack);

        initEvent();

    }

    //设置输入框更新监听和ListView滚动、点击监听
    public void initEvent(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });

        //滚动监听关闭软键盘
        bikeListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:// 滚动状态关闭软键盘
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸后滚动关闭软键盘
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

        bikeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0 && position < bikeList.size()+1){
                    finish();
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

                    Bike nowBike = (Bike)adapter.getItem(position-1);

                    Message msg = new Message();
                    msg.what = MyApplication.SELECT_TO_MAIN;
                    msg.obj = nowBike;
                    msg.arg1 = nowBike.getPos();
                    app.flushMain(msg);
                }
            }
        });

    }

    //根据输入检索城市
    private void filterData(String filterStr) {
        List<Bike> mSortList = new ArrayList<>();
        if (TextUtils.isEmpty(filterStr)) {
            mSortList = bikeList;
        } else {
            mSortList.clear();
            for (Bike sortModel : bikeList) {
                //String temp = toHanyuPingyin(filterStr);
                //中文输入判断，英文判断
                if (sortModel.getName().indexOf(filterStr) != -1 || sortModel.getName().startsWith(filterStr)
                        || sortModel.getPinyin().indexOf(filterStr.toUpperCase()) != -1 || sortModel.getPinyin().startsWith(filterStr.toUpperCase())) {
                    mSortList.add(sortModel);
                }
            }
        }
        adapter.updateListView(mSortList);
    }

    public void setAdapter(){
        adapter = new BikeAdapter(this,bikeList);
        bikeListView.addHeaderView(getLayoutInflater().inflate(R.layout.support_simple_spinner_dropdown_item,null));
        bikeListView.setAdapter(adapter);
    }

    //将中文转为英文，太卡待修改
    /*
    public String toHanyuPingyin(String name){
        Hanyu hanyu = new Hanyu();

        String strPinyin = hanyu.getStringPinYin(name);
        return strPinyin;
    }
    */

}
