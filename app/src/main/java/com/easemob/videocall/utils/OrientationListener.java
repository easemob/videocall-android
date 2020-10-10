package com.easemob.videocall.utils;


import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.provider.Settings;

public class OrientationListener implements SensorEventListener {
    private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private OnOrientationChangeListener mListener;

    public OrientationListener(OnOrientationChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
            return;
        }

        float[] values = event.values;
        float x = values[0];
        float y = values[1];

        int newOrientation;
        if (x < 4.5 && x >= -4.5 && y >= -4.5) {
            newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (x >= 4.5 && y < 4.5 && y >= -4.5) {
            newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (x <= -4.5 && y < 4.5 && y >= -4.5) {
            newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }else {
            newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }

        if (mOrientation != newOrientation) {
            if (mListener != null) {
                mListener.orientationChanged(newOrientation);
            }
            mOrientation = newOrientation;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface OnOrientationChangeListener {
        void orientationChanged(int newOrientation);
    }
}

