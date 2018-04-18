package com.cxem_car;

import java.lang.ref.WeakReference;
import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class ActivityAccelerometer extends Activity implements SensorEventListener, OnClickListener {

    private SensorManager mSensorManager;
    private Sensor mAccel;
    private cBluetooth bl = null;
    private ToggleButton LightButton;

    private int xAxis = 0;
    private int yAxis = 0;
    private int motorLeft = 0;
    private int motorRight = 0;
    private String address;            // MAC-address from settings (MAC-àäðåñ óñòðîéñòâà èç íàñòðîåê)
    private boolean show_Debug;        // show debug information (from settings) (îòîáðàæåíèå îòëàäî÷íîé èíôîðìàöèè (èç íàñòðîåê))
    private boolean BT_is_connect;    // bluetooh is connected (ïåðåìåííàÿ äëÿ õðàíåíèÿ èíôîðìàöèè ïîäêëþ÷åí ëè Bluetooth)
    private int xMax;                // limit on the X axis from settings (ïðåäåë ïî îñè X, ìàêñèìàëüíîå çíà÷åíèå äëÿ ØÈÌ (0-10), ÷åì áîëüøå, òåì áîëüøå íóæíî íàêëîíÿòü Android-óñòðîéñòâî)
    private int yMax;                // limit on the Y axis from settings (ïðåäåë ïî îñè Y, ìàêñèìàëüíîå çíà÷åíèå äëÿ ØÈÌ (0-10))
    private int yThreshold;        // minimum value of PWM from settings (ìèíèìàëüíîå çíà÷åíèå ØÈÌ (ïîðîã íèæå êîòîðîãî íå âðàùàåòñÿ äâèãàòåëü))
    private int pwmMax;                // maximum value of PWM from settings (ìàêñèìàëüíîå çíà÷åíèå ØÈÌ èç íàñòðîåê)
    private int xR;                    // pivot point from settings (òî÷êà ðàçâîðîòà èç íàñòðîåê)
    private String commandLeft;        // command symbol for left motor from settings (ñèìâîë êîìàíäû ëåâîãî äâèãàòåëÿ èç íàñòðîåê)
    private String commandRight;    // command symbol for right motor from settings (ñèìâîë êîìàíäû ïðàâîãî äâèãàòåëÿ èç íàñòðîåê)
    private String command1, command2, command3, command4;            // command symbol for optional command from settings (for example - horn) (ñèìâîë êîìàíäû äëÿ äîï. êàíàëà (çâóêîâîé ñèãíàë) èç íàñòðîåê)
    private ToggleButton btnCmd1, btnCmd2, btnCmd3, btnCmd4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        address = (String) getResources().getText(R.string.default_MAC);
        xMax = Integer.parseInt((String) getResources().getText(R.string.default_xMax));
        xR = Integer.parseInt((String) getResources().getText(R.string.default_xR));
        yMax = Integer.parseInt((String) getResources().getText(R.string.default_yMax));
        yThreshold = Integer.parseInt((String) getResources().getText(R.string.default_yThreshold));
        pwmMax = Integer.parseInt((String) getResources().getText(R.string.default_pwmMax));
        commandLeft = (String) getResources().getText(R.string.default_commandLeft);
        commandRight = (String) getResources().getText(R.string.default_commandRight);
        command1 = (String) getResources().getText(R.string.default_command1);
        command2 = (String) getResources().getText(R.string.default_command2);
        command3 = (String) getResources().getText(R.string.default_command3);
        command4 = (String) getResources().getText(R.string.default_command4);

        loadPref();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        bl = new cBluetooth(this, mHandler);
        bl.checkBTState();

        btnCmd1 = this.findViewById(R.id.btnCommand1);
        btnCmd1.setOnClickListener(this);
        btnCmd2 = this.findViewById(R.id.btnCommand2);
        btnCmd2.setOnClickListener(this);
        btnCmd3 = this.findViewById(R.id.btnCommand3);
        btnCmd3.setOnClickListener(this);
        btnCmd4 = this.findViewById(R.id.btnCommand4);
        btnCmd4.setOnClickListener(this);

        mHandler.postDelayed(sRunnable, 600000);
        //finish();
    }

    public void onClick(View v) {
        String sendCommand = "";
        switch (v.getId()) {
            case R.id.btnCommand1:
                if (btnCmd1.isChecked()) {
                    sendCommand = String.valueOf(command1 + "1\r");
                } else {
                    sendCommand = String.valueOf(command1 + "0\r");
                }
                break;
            case R.id.btnCommand2:
                if (btnCmd2.isChecked()) {
                    sendCommand = String.valueOf(command2 + "1\r");
                } else {
                    sendCommand = String.valueOf(command2 + "0\r");
                }
                break;
            case R.id.btnCommand3:
                if (btnCmd3.isChecked()) {
                    sendCommand = String.valueOf(command3 + "1\r");
                } else {
                    sendCommand = String.valueOf(command3 + "0\r");
                }
                break;
            case R.id.btnCommand4:
                if (btnCmd4.isChecked()) {
                    sendCommand = String.valueOf(command4 + "1\r");
                } else {
                    sendCommand = String.valueOf(command4 + "0\r");
                }
                break;
            default:
                break;
        }
        if (!sendCommand.isEmpty() && BT_is_connect)
            bl.sendData(sendCommand);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ActivityAccelerometer> mActivity;

        public MyHandler(ActivityAccelerometer activity) {
            mActivity = new WeakReference<ActivityAccelerometer>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ActivityAccelerometer activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case cBluetooth.BL_NOT_AVAILABLE:
                        Log.d(cBluetooth.TAG, "Bluetooth is not available. Exit");
                        Toast.makeText(activity.getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                        activity.finish();
                        break;
                    case cBluetooth.BL_INCORRECT_ADDRESS:
                        Log.d(cBluetooth.TAG, "Incorrect MAC address");
                        Toast.makeText(activity.getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
                        break;
                    case cBluetooth.BL_REQUEST_ENABLE:
                        Log.d(cBluetooth.TAG, "Request Bluetooth Enable");
                        BluetoothAdapter.getDefaultAdapter();
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        activity.startActivityForResult(enableBtIntent, 1);
                        break;
                    case cBluetooth.BL_SOCKET_FAILED:
                        Toast.makeText(activity.getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
                        //activity.finish();
                        break;
                }
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

    private final static Runnable sRunnable = new Runnable() {
        public void run() {
        }
    };

    public void onSensorChanged(SensorEvent e) {
        String directionL = "";
        String directionR = "";
        String cmdSendL, cmdSendR;
        float xRaw, yRaw;        // RAW-value from Accelerometer sensor (RAW-çíà÷åíèå îò àêñåëëåðîìåòðà)

        WindowManager windowMgr = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int rotationIndex = windowMgr.getDefaultDisplay().getRotation();
        if (rotationIndex == 1 || rotationIndex == 3) {            // detect 90 or 270 degree rotation (îïðåäåëÿåì ïîâîðîò óñòðîéñòâà íà 90 èëè 270 ãðàäóñîâ)
            xRaw = -e.values[1];
            yRaw = e.values[0];
        } else {
            xRaw = e.values[0];
            yRaw = e.values[1];
        }

        xAxis = Math.round(xRaw * pwmMax / xR);
        yAxis = Math.round(yRaw * pwmMax / yMax);

        if (xAxis > pwmMax) xAxis = pwmMax;
        else if (xAxis < -pwmMax)
            xAxis = -pwmMax;        // negative - tilt right (îòðèö. çíà÷åíèå - íàêëîí âïðàâî)

        if (yAxis > pwmMax) yAxis = pwmMax;
        else if (yAxis < -pwmMax)
            yAxis = -pwmMax;        // negative - tilt forward (îòðèö. çíà÷åíèå - íàêëîí âïåðåä)
        else if (yAxis >= 0 && yAxis < yThreshold) yAxis = 0;
        else if (yAxis < 0 && yAxis > -yThreshold) yAxis = 0;

        if (xAxis > 0) {        // if tilt to left, slow down the left engine (åñëè âëåâî, òî òîðìîçèì ëåâûé ìîòîð)
            motorRight = yAxis;
            if (Math.abs(Math.round(xRaw)) > xR) {
                motorLeft = Math.round((xRaw - xR) * pwmMax / (xMax - xR));
                motorLeft = Math.round(-motorLeft * yAxis / pwmMax);
                //if(motorLeft < -pwmMax) motorLeft = -pwmMax;
            } else motorLeft = yAxis - yAxis * xAxis / pwmMax;
        } else if (xAxis < 0) {        // tilt to right (íàêëîí âïðàâî)
            motorLeft = yAxis;
            if (Math.abs(Math.round(xRaw)) > xR) {
                motorRight = Math.round((Math.abs(xRaw) - xR) * pwmMax / (xMax - xR));
                motorRight = Math.round(-motorRight * yAxis / pwmMax);
                //if(motorRight > -pwmMax) motorRight = -pwmMax;
            } else motorRight = yAxis - yAxis * Math.abs(xAxis) / pwmMax;
        } else if (xAxis == 0) {
            motorLeft = yAxis;
            motorRight = yAxis;
        }

        if (motorLeft > 0) {            // tilt to backward (íàêëîí íàçàä)
            directionL = "-";
        }
        if (motorRight > 0) {        // tilt to backward (íàêëîí íàçàä)
            directionR = "-";
        }
        motorLeft = Math.abs(motorLeft);
        motorRight = Math.abs(motorRight);

        if (motorLeft > pwmMax) motorLeft = pwmMax;
        if (motorRight > pwmMax) motorRight = pwmMax;

        cmdSendL = String.valueOf(commandLeft + directionL + motorLeft + "\r");
        cmdSendR = String.valueOf(commandRight + directionR + motorRight + "\r");

        if (BT_is_connect) bl.sendData(cmdSendL + cmdSendR);

        TextView textX = (TextView) findViewById(R.id.textViewX);
        TextView textY = (TextView) findViewById(R.id.textViewY);
        TextView mLeft = (TextView) findViewById(R.id.mLeft);
        TextView mRight = (TextView) findViewById(R.id.mRight);
        TextView textCmdSend = (TextView) findViewById(R.id.textViewCmdSend);

        if (show_Debug) {
            textX.setText(String.valueOf("X:" + String.format("%.1f", xRaw) + "; xPWM:" + xAxis));
            textY.setText(String.valueOf("Y:" + String.format("%.1f", yRaw) + "; yPWM:" + yAxis));
            mLeft.setText(String.valueOf("MotorL:" + directionL + "." + motorLeft));
            mRight.setText(String.valueOf("MotorR:" + directionR + "." + motorRight));
            textCmdSend.setText(String.valueOf("Send:" + cmdSendL.toUpperCase(Locale.getDefault()) + cmdSendR.toUpperCase(Locale.getDefault())));
        } else {
            textX.setText("");
            textY.setText("");
            mLeft.setText("");
            mRight.setText("");
            textCmdSend.setText("");
        }

    }

    private void loadPref() {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        address = mySharedPreferences.getString("pref_MAC_address", address);            // the first time we load the default values (ïåðâûé ðàç çàãðóæàåì äåôîëòíîå çíà÷åíèå)
        xMax = Integer.parseInt(mySharedPreferences.getString("pref_xMax", String.valueOf(xMax)));
        xR = Integer.parseInt(mySharedPreferences.getString("pref_xR", String.valueOf(xR)));
        yMax = Integer.parseInt(mySharedPreferences.getString("pref_yMax", String.valueOf(yMax)));
        yThreshold = Integer.parseInt(mySharedPreferences.getString("pref_yThreshold", String.valueOf(yThreshold)));
        pwmMax = Integer.parseInt(mySharedPreferences.getString("pref_pwmMax", String.valueOf(pwmMax)));
        show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
        commandLeft = mySharedPreferences.getString("pref_commandLeft", commandLeft);
        commandRight = mySharedPreferences.getString("pref_commandRight", commandRight);
        command1 = mySharedPreferences.getString("pref_command1", command1);
        command2 = mySharedPreferences.getString("pref_command2", command2);
        command3 = mySharedPreferences.getString("pref_command3", command3);
        command4 = mySharedPreferences.getString("pref_command4", command4);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BT_is_connect = bl.BT_Connect(address, false);
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bl.BT_onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadPref();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }
}