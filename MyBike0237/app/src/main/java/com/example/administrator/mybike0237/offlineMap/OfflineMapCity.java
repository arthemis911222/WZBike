package com.example.administrator.mybike0237.offlineMap;

/**
 * Created by Administrator on 2016/12/24.
 */
public class OfflineMapCity {
    private String cityName;
    private int cityCode;
    private int progress;//下载进度

    public OfflineMapCity() {
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
