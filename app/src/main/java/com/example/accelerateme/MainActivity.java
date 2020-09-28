package com.example.accelerateme;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private TextView mInfoText;
    private Button mButton;

    private boolean isActive = false;
    private  boolean isCountDown = false;

    float accelerationY;
    float accelerationX;
    float accelerationZ;

    List<String> values = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoText = findViewById(R.id.activity_main_info_txt);
        LinearLayout mLayout = findViewById(R.id.activity_main_layout);
        mButton = findViewById(R.id.activity_main_btn);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mLayout.setKeepScreenOn(true);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActive = !isActive;
                if (isActive){
                    mButton.setText("Stop Reading");
                }else{
                    mButton.setText("Start Reading");
                }
            }
        });


    }


    private void writeToFile(String content) {

        Date currentTime = Calendar.getInstance().getTime();
        String strDate = currentTime.toString();

        if (isStoragePermissionGranted())
        {
            try {
                File file = new File( Environment.getExternalStorageDirectory() + "/Download/test.csv");

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter writer = new FileWriter(file);
                writer.append(strDate).append(" : ").append(content);
                writer.flush();
                writer.close();

            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public void changeText(){
        mInfoText.setText("Mamie est tombé!");
        mInfoText.setBackgroundColor(RED);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mInfoText.setText("Coucou Mamie!");
                mInfoText.setBackgroundColor(WHITE);
            }
        }, 2000);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public final void onSensorChanged(SensorEvent event) {

        accelerationY = event.values[1];
        accelerationX = event.values[0];
        accelerationZ = event.values[2];

        int threshold = 15;

        if ((accelerationX> threshold || accelerationY > threshold || accelerationZ > threshold) && isActive == true && isCountDown == false){

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            isCountDown = true;
            changeText();
            writeValues();
        }
    }

    protected void writeValues(){
        if (isCountDown){
            new CountDownTimer(5000, 100) {
                @Override
                public void onTick(long l) {
                    values.add("X : " + accelerationX + " Y : " + accelerationY + " Z : " + accelerationZ );
                }

                @Override
                public void onFinish() {
                    isCountDown = false;
                    writeToFile(values.toString());
                    System.out.println(values);
                }
            }.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    public boolean isStoragePermissionGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, SYSTEM_ALERT_WINDOW, READ_EXTERNAL_STORAGE
                }, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
