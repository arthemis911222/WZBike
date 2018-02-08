package com.example.administrator.mybike0237.bikefile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.administrator.mybike0237.R;

import java.util.List;

/**
 * Created by Administrator on 2016/12/18.
 */
public class BikeAdapter extends BaseAdapter {
    private List<Bike> list = null;
    private Context context;

    public BikeAdapter(Context context,List<Bike> lst){
        this.list = lst;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_select_bike,null);
        }
        TextView tv1 = (TextView)convertView.findViewById(R.id.tv_name);
        TextView tv2 = (TextView)convertView.findViewById(R.id.tv_address);
        TextView tv3 = (TextView)convertView.findViewById(R.id.tv_avaibike);
        TextView tv4 = (TextView)convertView.findViewById(R.id.tv_stopbike);

        tv1.setText(list.get(position).getName());
        tv2.setText(list.get(position).getAddress());
        tv3.setText("可借车数："+list.get(position).getAvailBike());
        tv4.setText("可停车位："+list.get(position).getStopBike());

        return convertView;
    }

    public void updateListView(List<Bike> lst){
        this.list = lst;
        notifyDataSetChanged();
    }
}
