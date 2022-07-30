package com.example.locationtrackertest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    // accelerometer, gyroscope sensors
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;

    // textViews
    private TextView txt_lat, txt_long;

    private TextView txt_accelX, txt_accelY, txt_accelZ;
    private TextView txt_gyroX, txt_gyroY, txt_gyroZ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTxtViews();
        initSensors();

        checkLocationPermissions();
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onCreate: Registered accelerometer listener");
        } else {
            Log.d(TAG, "Accelerometer Not Supported");
        }

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(gyroscope != null) {
            sensorManager.registerListener( this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onCreate: Registered gyroscope listener");
        } else {
            Log.d(TAG, "Gyroscope Not Supported");
        }
    }


    private void initTxtViews() {
        txt_lat = findViewById(R.id.loc_lat);
        txt_long = findViewById(R.id.loc_long);

        txt_accelX = findViewById(R.id.accelX);
        txt_accelY = findViewById(R.id.accelY);
        txt_accelZ = findViewById(R.id.accelZ);

        txt_gyroX = findViewById(R.id.gyroX);
        txt_gyroY = findViewById(R.id.gyroY);
        txt_gyroZ = findViewById(R.id.gyroZ);
    }

    private void checkLocationPermissions() {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); // request location
        } else {
            startService();  // request location permission
        }
    }

    void startService() {
        LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACT_LOC");
        registerReceiver(receiver, filter);
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                Toast.makeText(this, "This app requires location permissions to work", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            txt_accelX.setText("accelX: m/s²\n" + String.format("%3.2f", event.values[0]));
            txt_accelY.setText("accelY: m/s²\n" + String.format("%3.2f", event.values[1]));
            txt_accelZ.setText("accelZ: m/s²\n" + String.format("%3.2f", event.values[2]));
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            txt_gyroX.setText("gyroX: °\n" + String.format("%3.2f",event.values[0] * 180/Math.PI));
            txt_gyroY.setText("gyroY: °\n" + String.format("%3.2f",event.values[1] * 180/Math.PI));
            txt_gyroZ.setText("gyroZ: °\n" + String.format("%3.2f",event.values[2] * 180/Math.PI));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("ACT_LOC")) {
                double latitude = intent.getDoubleExtra("latitude", 0f);
                double longitude = intent.getDoubleExtra("longitude", 0f);

                txt_lat.setText("Latitude: " + String.format("%3.5f", latitude));
                txt_long.setText("Longitude: " + String.format("%3.5f", longitude));

                Toast.makeText(MainActivity.this, "Latitude is: " + latitude + "\nLongitude is: " + longitude, Toast.LENGTH_LONG).show();
            }
        }
    }
}