package com.easemob.videocall.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;

import java.util.concurrent.atomic.AtomicBoolean;

public class Config {
    public static final String ACTION_CONFIG_CHANGE = "gt.research.androidbase.ACTION_CONFIG_CHANGE";
    public static final String KEY_CHANGED_PARTS = "KEY_CHANGED_PARTS";

    private int mId;
    //just for convenient
    private AtomicBoolean mHasComsumed = new AtomicBoolean(true);
    //for multithreading
    private final ThreadLocal<ArrayMap<String, Object>> mTransactionMap = new ThreadLocal<>();
    private LocalBroadcastManager mBroadcastManager;

    //model
    private final ArrayMap<String, Object> mConfigs = new ArrayMap<>();

    public Config(int id) {
        mId = id;
    }

    //multiple changes
    public Config beginTransaction(Context context) {
        ArrayMap<String, Object> map = mTransactionMap.get();
        if (null == map) {
            map = new ArrayMap<>();
        }
        map.clear();
        return this;
    }

    public Config commit(final Context context) {
        final ArrayMap<String, Object> map = mTransactionMap.get();
        if (null == map) {
            return this;
        }
        //for multi-threading
        ConfigManager.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConfigs.putAll(map);
                notifyChange(context, (String[]) map.keySet().toArray());
            }
        });
        return this;
    }

    public Config set(final Context context, final String name, final Object object) {
        ConfigManager.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConfigs.put(name, object);
                notifyChange(context, name);
            }
        });
        return this;
    }

    public Object get(Context context, String name) {
        //constrain on A
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Wrong Thread");
        }
        return mConfigs.get(name);
    }

    //KVO
    private Config notifyChange(Context context, String... names) {
        LocalBroadcastManager broadcastManager = getBroadcastManager(context);
        Intent intent = new Intent(ACTION_CONFIG_CHANGE + mId);
        intent.setPackage("gt.research.androidbase");
        intent.putExtra(KEY_CHANGED_PARTS, names);
        broadcastManager.sendBroadcast(intent);
        mHasComsumed.set(false);
        return this;
    }

    private LocalBroadcastManager getBroadcastManager(Context context) {
        if (null == mBroadcastManager) {
            mBroadcastManager = LocalBroadcastManager.getInstance(context);
        }
        return mBroadcastManager;
    }

    public boolean shouldUpdate() {
        return !mHasComsumed.getAndSet(true);
    }

    public String[] getAllNames() {
        return (String[]) mConfigs.keySet().toArray();
    }
}

