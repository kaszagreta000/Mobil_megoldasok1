package com.example.compass;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ImageView img_compass;
    TextView txt_azimuth;
    int mAzimuth;
    private SensorManager mSensorManager;
    private Sensor mRotaionV, mAccelerometer, mMagnetometer;
    float[] rMat = new float[9];
    float[] orientation = new float[9];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean haveSensor = false;
    private boolean haveSensor2 = false;
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        img_compass = (ImageView) findViewById(R.id.img_compass);
        txt_azimuth = (TextView) findViewById(R.id.txt_azimuth);

        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) { //ellenorizzuk hogy melyik erzekelo kapja meg az adatokat
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0])+360)%360); //atvaltjuk fokokra --> iranyszog
        }if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }

        if(mLastMagnetometerSet && mLastAccelerometerSet){ //ha mar kaptunk ertekeket --> tudunk azokbol szamolni
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0])+360)%360);
        }

        mAzimuth = Math.round(mAzimuth); //biztosan atadasa az erteknek
        img_compass.setRotation(-mAzimuth); //kep forgatasa

        String where = "NO";

        //melyik egtaj fele nezunk
        if((mAzimuth >= 350) || (mAzimuth <= 10)){
            where = "N";
        }
        if((mAzimuth < 350) && (mAzimuth > 280)){
            where = "NW";
        }
        if((mAzimuth <= 280) && (mAzimuth > 260)){
            where = "W";
        }
        if((mAzimuth <= 260) && (mAzimuth > 190)){
            where = "SW";
        }
        if((mAzimuth <= 190) && (mAzimuth > 170)){
            where = "S";
        }
        if((mAzimuth <= 170) && (mAzimuth > 100)){
            where = "SE";
        }
        if((mAzimuth <= 100) && (mAzimuth > 80)){
            where = "E";
        }
        if((mAzimuth <= 80) && (mAzimuth > 10)){
            where = "NE";
        }
        if((mAzimuth < 350) && (mAzimuth > 280)){
            where = "NW";
        }

        txt_azimuth.setText(mAzimuth + "° " + where); //kiiratas (elso sor)
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void start(){
        //lecsekkoljuk hogy van e ez a harom tipusu erzekelo
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null){ //forgasi vektor
            if((mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) || //magneses mezo
                    (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null)){ //gyorsulasmero
                noSensorAlert();
            }else{
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                haveSensor = mSensorManager.registerListener(this, mAccelerometer,
                        SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this, mMagnetometer,
                        SensorManager.SENSOR_DELAY_UI);
            }
        }else{
            mRotaionV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR); //forgatasvektor
            haveSensor = mSensorManager.registerListener(this, mRotaionV,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorAlert(){ //amikor nincs erzekelo
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("You device doesn't support the compass.")
                .setCancelable(false)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); //bezaras gomb --> vegeztunk ezzel az activityvel (bezarjuk az alkalmazast)
                    }
                });
    }

    public void stop(){
        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mMagnetometer);
        }else{
            if(haveSensor){
                mSensorManager.unregisterListener(this, mRotaionV);
            }
        }
    }

    @Override
    protected void onPause(){ //akkor áll meg amikor az app "postazasi" allapotba kerul
        super.onPause();
        stop(); //leallittatjuk
    }

    @Override
    protected void onResume(){ //ujrainditjuk
        super.onResume();
        start(); //el is kezdjuk ujra
    }
}