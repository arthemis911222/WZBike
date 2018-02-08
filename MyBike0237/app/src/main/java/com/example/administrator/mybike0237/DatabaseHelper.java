package com.example.administrator.mybike0237;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.administrator.mybike0237.bikefile.Bike;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/21.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private List<Bike> list = null;
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE mytable(id TEXT PRIMARY KEY, name TEXT, address TEXT, lat DOUBLE, lng DOUBLE, avaibike INTEGER, stopbike INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void update(String id,int avaibike,int stopbike){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("id",id);
        v.put("avaibike",avaibike);
        v.put("stopbike",stopbike);
        db.update("mytable",v,"id=?",new String[]{id});
    }
    public void update(String id,String name,String address,double lat,double lng,int avaibike,int stopbike){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("id",id);
        v.put("name",name);
        v.put("address",address);
        v.put("lat",lat);
        v.put("lng",lng);
        v.put("avaibike",avaibike);
        v.put("stopbike",stopbike);
        db.update("mytable",v,"id=?",new String[]{id});
    }

    public void delete(String id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete("mytable", "id=?", new String[]{id});
    }

    public String sqlString(String id,String name,String address,double lat,double lng,int avaibike,int stopbike){
        SQLiteDatabase db = getWritableDatabase();

        if(select(id).getCount() != 0)
        {
            String s = "UPDATE mytable SET avaibike="+avaibike+",stopbike="+stopbike+" WHERE id="+id;
            return s;
        }

        String s = "INSERT INTO mytable(id,name,address,lat,lng,avaibike,stopbike) VALUES('"+ id +"' ,'" + name + "', '"+address+"', "+lat+","+ lng+", "+
                avaibike + ","+stopbike+")";

        return s;

    }

    public Cursor select(String _id){
        String[] Values = {_id};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery("select * from mytable where id=?",new String[]{_id});
        return cur;
    }

    public void setList(){
        list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor result = db.rawQuery("SELECT id,name,address,lat,lng,avaibike,stopbike FROM mytable",null);
        result.moveToFirst();
        while(!result.isAfterLast()){
            Bike temp = new Bike();
            temp.setId(result.getString(0));
            temp.setName(result.getString(1));
            temp.setAddress(result.getString(2));
            temp.setLat(result.getDouble(3));
            temp.setLng(result.getDouble(4));
            temp.setAvailBike(result.getInt(5));
            temp.setStopBike(result.getInt(6));
            temp.setPos(list.size());
            list.add(temp);
            result.moveToNext();
        }
        result.close();

    }

    public List<Bike> getList() {
        return list;
    }

    public void insertOrUpdateDate(List<String> sqls){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for(int i=0;i<sqls.size();i++){
                db.execSQL(sqls.get(i));
            }
            db.setTransactionSuccessful();
        }catch ( Exception e ){
            e.printStackTrace();
        }finally {
            //结束事务
            db.endTransaction();
            db.close();
        }
    }
}
