package com.example.huang.check_in;

import android.app.Application;
import android.device.ScanDevice;

/**
 * 初始化扫描设备
 * Created by huang on 2017/10/20.
 */

public class MyApplication extends Application {

    private ScanDevice scanDevice;

    @Override
    public void onCreate() {
        super.onCreate();
        if (scanDevice != null) return;
        scanDevice = new ScanDevice();
    }

    public ScanDevice getScanDevice() {
        return scanDevice;
    }
}
