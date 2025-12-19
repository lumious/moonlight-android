package com.limelight.ui.gamepad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view for displaying gyroscope sensor data
 */
public class GyroView extends View implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private Paint paint;
    private float rotationX = 0;
    private float rotationY = 0;
    private float rotationZ = 0;
    private boolean isStarted = false;

    public GyroView(Context context) {
        super(context);
        init();
    }

    public GyroView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GyroView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(30f);
        paint.setAntiAlias(true);
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        if (sensorManager != null) {
            gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }

    public void start() {
        if (sensorManager != null && gyroSensor != null && !isStarted) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
            isStarted = true;
        }
    }

    public void release() {
        if (sensorManager != null && isStarted) {
            sensorManager.unregisterListener(this);
            isStarted = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            rotationX = event.values[0];
            rotationY = event.values[1];
            rotationZ = event.values[2];
            invalidate(); // Trigger redraw
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw gyroscope data
        canvas.drawText("Gyroscope Data:", 20, 40, paint);
        canvas.drawText("X: " + String.format("%.2f", rotationX) + " rad/s", 20, 80, paint);
        canvas.drawText("Y: " + String.format("%.2f", rotationY) + " rad/s", 20, 120, paint);
        canvas.drawText("Z: " + String.format("%.2f", rotationZ) + " rad/s", 20, 160, paint);
        
        // Draw a simple visual representation
        int centerX = width / 2;
        int centerY = height - 50;
        
        // Draw circle to represent orientation
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        canvas.drawCircle(centerX, centerY, 30, paint);
        
        // Draw indicator based on rotation
        float indicatorX = centerX + (rotationY * 20);
        float indicatorY = centerY - (rotationX * 20);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(indicatorX, indicatorY, 10, paint);
    }
}
