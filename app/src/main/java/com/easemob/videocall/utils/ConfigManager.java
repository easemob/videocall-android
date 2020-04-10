package com.easemob.videocall.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;


public class ConfigManager {
    private static volatile ConfigManager sInstance;

    public static ConfigManager getInstance() {
        if (null == sInstance) {
            synchronized (ConfigManager.class) {
                if (null == sInstance) {
                    sInstance = new ConfigManager();
                }
            }
        }
        return sInstance;
    }

    private ConfigManager() {
    }
    private final SparseArray<Config> mConfigs = new SparseArray<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public Config getConfig(int id) {
        synchronized (mConfigs) {
            Config config = mConfigs.get(id);
            if (null == config) {
                config = new Config(id);
            }
            mConfigs.put(id, config);
            return config;
        }
    }
    public void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
