package com.nwhacks.strideml;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainActivity extends WearableActivity implements AccelerometerListener, SensorEventListener {
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private int iteration = 0;
    private int maxIteration = 20;

    //data layer
    private TextView textView;
    Button talkButton;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;
    // end of init data layer
    private String dataOutput = "";
    private String finalData = "{\"data\":[";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        talkButton = findViewById(R.id.talkClick);

        talkButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String onClickMessage = "I just sent the handheld a message " + sentMessageNumber++;
                textView.setText(onClickMessage);

                String datapath = "/my_path";
                new SendMessage(datapath, onClickMessage).start();
            }
        });

        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public class Receiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent){
            String onMessageReceived = "I just received a message from the phone";
            textView.setText(onMessageReceived);
        }
    }

    class SendMessage extends Thread{
        String path;
        String message;

        SendMessage(String p, String m){
            path = p;
            message = m;
        }

        public void run(){
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

//Block on a task and get the result synchronously//

                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {

//Send the message///

                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {

                        Integer result = Tasks.await(sendMessageTask);

//Handle the errors//

                    } catch (ExecutionException exception) {

//TO DO//

                    } catch (InterruptedException exception) {

//TO DO//

                    }

                }

            } catch (ExecutionException exception) {

//TO DO//

            } catch (InterruptedException exception) {

//TO DO//

            }
        }
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

        // if the change is below 2, it is just plain noise
        if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;
        if (deltaZ < 2)
            deltaZ = 0;
    }

    // display the current x,y,z accelerometer values

    private long lastTick = System.currentTimeMillis();

    private void sendNoti(String text) {
        int id = 001;
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle("Stride")
                .setSmallIcon(R.drawable.ic_cc_checkmark)
                .extend(new NotificationCompat.WearableExtender())
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(id ,notification);
    }

    public void displayCurrentValues() {
        long now = System.currentTimeMillis();

        dataOutput = "{\"ts\" : " + lastTick + ",\"x\" : " + Float.toString(deltaX) + ", \"y\" : " + Float.toString(deltaY) + ", \"z\" : " + Float.toString(deltaZ) + "}";

        if(now > lastTick + 250) {
            iteration++;
            if(iteration > maxIteration){
                sendNoti(finalData);
                System.out.println(finalData);
                iteration = 0;
                finalData = "";
            }
            finalData = finalData + "," + dataOutput;
            lastTick = now;
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