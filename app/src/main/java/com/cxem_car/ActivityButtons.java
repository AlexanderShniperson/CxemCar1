package com.cxem_car;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ActivityButtons extends Activity implements OnClickListener {

    private cBluetooth bl = null;
    private ToggleButton LightButton;

    private Button btn_forward, btn_backward, btn_left, btn_right;

    private int motorLeft = 0;
    private int motorRight = 0;
    private String address;            // MAC-address from settings (MAC-àäðåñ óñòðîéñòâà èç íàñòðîåê)
    private boolean BT_is_connect;    // bluetooh is connected (ïåðåìåííàÿ äëÿ õðàíåíèÿ èíôîðìàöèè ïîäêëþ÷åí ëè Bluetooth)
    private int pwmBtnMotorLeft;    // left PWM constant value from settings (ïîñòîÿííîå çíà÷åíèå ØÈÌ äëÿ ëåâîãî äâèãàòåëÿ èç íàñòðîåê)
    private int pwmBtnMotorRight;    // right PWM constant value from settings (ïîñòîÿííîå çíà÷åíèå ØÈÌ äëÿ ïðàâîãî äâèãàòåëÿ èç íàñòðîåê)
    private String commandLeft;        // command symbol for left motor from settings (ñèìâîë êîìàíäû ëåâîãî äâèãàòåëÿ èç íàñòðîåê)
    private String commandRight;    // command symbol for right motor from settings (ñèìâîë êîìàíäû ïðàâîãî äâèãàòåëÿ èç íàñòðîåê)
    private String command1, command2, command3, command4;            // command symbol for optional command from settings (for example - horn) (ñèìâîë êîìàíäû äëÿ äîï. êàíàëà (çâóêîâîé ñèãíàë) èç íàñòðîåê)
    private ToggleButton btnCmd1, btnCmd2, btnCmd3, btnCmd4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buttons);

        address = (String) getResources().getText(R.string.default_MAC);
        pwmBtnMotorLeft = Integer.parseInt((String) getResources().getText(R.string.default_pwmBtnMotorLeft));
        pwmBtnMotorRight = Integer.parseInt((String) getResources().getText(R.string.default_pwmBtnMotorRight));
        commandLeft = (String) getResources().getText(R.string.default_commandLeft);
        commandRight = (String) getResources().getText(R.string.default_commandRight);
        command1 = (String) getResources().getText(R.string.default_command1);
        command2 = (String) getResources().getText(R.string.default_command2);
        command3 = (String) getResources().getText(R.string.default_command3);
        command4 = (String) getResources().getText(R.string.default_command4);

        loadPref();

        bl = new cBluetooth(this, mHandler);
        bl.checkBTState();

        btn_forward = (Button) findViewById(R.id.forward);
        btn_backward = (Button) findViewById(R.id.backward);
        btn_left = (Button) findViewById(R.id.left);
        btn_right = (Button) findViewById(R.id.right);

        btn_forward.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    motorLeft = pwmBtnMotorLeft;
                    motorRight = pwmBtnMotorRight;
                    if (BT_is_connect)
                        bl.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight + motorRight + "\r"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    motorLeft = 0;
                    motorRight = 0;
                    if (BT_is_connect)
                        bl.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight + motorRight + "\r"));
                }
                return false;
            }
        });

        btn_left.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    motorLeft = -pwmBtnMotorLeft;
                    motorRight = pwmBtnMotorRight;
                    if (BT_is_connect)
                        bl.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight + motorRight + "\r"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    motorLeft = 0;
                    motorRight = 0;
                    if (BT_is_connect)
                        bl.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight + motorRight + "\r"));
                }
                return false;
            }
        });

        btn_right.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    motorLeft = pwmBtnMotorLeft;
                    motorRight = -pwmBtnMotorRight;
                    if (BT_is_connect)
                        bl.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight + motorRight + "\r"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    motorLeft = 0;
                    motorRight = 0;
                    if (BT_is_connect)
                        bl.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight + motorRight + "\r"));
                }
                return false;
            }
        });

        btn_backward.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    motorLeft = -pwmBtnMotorLeft;
                    motorRight = -pwmBtnMotorRight;
                    if (BT_is_connect)
                        bl.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight + motorRight + "\r"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    motorLeft = 0;
                    motorRight = 0;
                    if (BT_is_connect)
                        bl.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight + motorRight + "\r"));
                }
                return false;
            }
        });

        btnCmd1 = this.findViewById(R.id.btnCommand1);
        btnCmd1.setOnClickListener(this);
        btnCmd2 = this.findViewById(R.id.btnCommand2);
        btnCmd2.setOnClickListener(this);
        btnCmd3 = this.findViewById(R.id.btnCommand3);
        btnCmd3.setOnClickListener(this);
        btnCmd4 = this.findViewById(R.id.btnCommand4);
        btnCmd4.setOnClickListener(this);

        mHandler.postDelayed(sRunnable, 600000);

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
        private final WeakReference<ActivityButtons> mActivity;

        public MyHandler(ActivityButtons activity) {
            mActivity = new WeakReference<ActivityButtons>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ActivityButtons activity = mActivity.get();
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

    private void loadPref() {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        address = mySharedPreferences.getString("pref_MAC_address", address);            // the first time we load the default values (ïåðâûé ðàç çàãðóæàåì äåôîëòíîå çíà÷åíèå)
        pwmBtnMotorLeft = Integer.parseInt(mySharedPreferences.getString("pref_pwmBtnMotorLeft", String.valueOf(pwmBtnMotorLeft)));
        pwmBtnMotorRight = Integer.parseInt(mySharedPreferences.getString("pref_pwmBtnMotorRight", String.valueOf(pwmBtnMotorRight)));
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        bl.BT_onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadPref();
    }
}