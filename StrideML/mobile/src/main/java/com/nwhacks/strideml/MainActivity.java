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
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity implements AccelerometerListener, SensorEventListener {
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    //Data Layer
    Button talkButton;
    TextView textview;
    protected Handler myHandler;
    int receievedMessageNumber = 1;
    int sentMessageNumber = 1;
    //End of Data Layer Testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        talkButton = findViewById(R.id.talkButton);
        textview = findViewById(R.id.textview);

        myHandler = new Handler(new Handler.Callback(){
            public boolean handleMessage(Message msg){
                Bundle stuff = msg.getData();
                messageText(stuff.getString("messageText"));
                return true;
            }
        });

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        // end of Data Layer stuff

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // data layer shit
    public void messageText(String newinfo){
        if(newinfo.compareTo("") != 0){
            textview.append("\n" + newinfo);
        }
    }

    public class Receiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent){
            String message = "I just received a message from the watch";
            textview.setText(message);
        }
    }

    public void talkClick(View v){
        String message = "Sending Message...";
        textview.setText(message);

        new NewThread("/my_path",message).start();
    }

    public void sendMessage(String messageText){
        Bundle bundle = new Bundle();
        bundle.putString("messageText", messageText);
        Message msg = myHandler.obtainMessage();
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    class NewThread extends Thread{
        String path;
        String message;

        NewThread(String p, String m){
            path = p;
            message = m;
        }

        public void run(){
            Task<List<Node>> wearableList = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try{
                List<Node> nodes = Tasks.await(wearableList);
                for(Node node : nodes){
                    Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(),path,message.getBytes());

                    try{
                        Integer result = Tasks.await(sendMessageTask);
                        sendMessage("I just sent the wearable a message " + sentMessageNumber++);
                    } catch(ExecutionException e){
                        e.printStackTrace();
                    } catch(InterruptedException exception){
                        exception.printStackTrace();
                    }
                }
            } catch(ExecutionException e){
                e.printStackTrace();
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    // end of data layer shit

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

        Notification notification = new Notification.Builder(this)
                .setContentText(text)
                .setContentTitle("Stride")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(id ,notification);
    }

    public void displayCurrentValues() {
        long now = System.currentTimeMillis();

        String output = "X = " + Float.toString(deltaX)
                + " | Y = " + Float.toString(deltaY)
                + " | Z = " + Float.toString(deltaZ);

        if(now > lastTick + 1000) {
            System.out.println(output);
            sendNoti(output);
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