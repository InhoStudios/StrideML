package net.thebitspud.accelerometertest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainActivity extends AppCompatActivity implements AccelerometerListener, SensorEventListener {
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private TextView mainText;

    private Gson gson;
    private ArrayList<Stride> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = findViewById(R.id.main_text);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        gson = new Gson();
        data = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AccelerometerManager.isSupported(this)) {
            AccelerometerManager.startListening(this);
        }
    }

    @Override
    public void onAccelerationChanged(float x, float y, float z) {

    }

    @Override
    public void onShake(float force) {

    }

    @Override
    public void onStop() {
        super.onStop();

        //Check device supported Accelerometer sensor or not
        if (AccelerometerManager.isListening()) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float lastX = 0, lastY = 0, lastZ = 0;

        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2] + (float) 9.8);
    }

    // display the current x,y,z accelerometer values

    private long ltShort = System.currentTimeMillis(),
            ltLong = System.currentTimeMillis();

    public void displayCurrentValues() {
        long now = System.currentTimeMillis();

        if(now > ltShort + 250) {
            ltShort = now;
            System.out.println("X = " + deltaX + " | Y = " + deltaY + " | Z = " + deltaZ);
            float xy = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
            data.add(new Stride(System.currentTimeMillis(), xy, deltaZ));
        }

        if(now > ltLong + 750) {
            ltLong = now;
            mainText.setText("X = " + deltaX + "\nY = " + deltaY + "\nZ = " + deltaZ);
            String toJson = gson.toJson(data);
            System.out.println(toJson);

            try (Writer writer = new FileWriter("Output.json")) {
                Gson gson = new GsonBuilder().create();
                gson.toJson(data, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

class Stride {
    long ts;
    float xy;
    float z;

    public Stride(long ts, float xy, float z) {
        this.ts = ts;
        this.xy = xy;
        this.z = z;
    }
}