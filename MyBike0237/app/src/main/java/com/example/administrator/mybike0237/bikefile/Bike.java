package com.example.administrator.mybike0237.bikefile;


/**
 * Created by Administrator on 2016/12/17.
 */
public class Bike {
    private String id;
    private String name;
    private double lat;
    private double lng;
    private int availBike;
    private int stopBike;
    private String address;
    private int pos;

    private String pinyin;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getStopBike() {
        return stopBike;
    }

    public void setStopBike(int stopBike) {
        this.stopBike = stopBike;
    }

    public int getAvailBike() {
        return availBike;
    }

    public void setAvailBike(int availBike) {
        this.availBike = availBike;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    @Override
    public String toString() {
        return "Bike{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", availBike='" + availBike + '\'' +
                ", stopBike='" + stopBike + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
