package com.nordicsemi.nrfUARTv2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.nordicsemi.nrfUARTv2.UartService;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


public class Main2Activity extends Activity  {

    private ImageButton Settings_button;
    private ImageButton OpenClose_button;
    private ImageButton Hold_button;
    private ImageButton SelectGate1_button;
    private ImageButton SelectGate2_button;
    private ImageButton SelectGate3_button;
    private ImageView ConnectionStatus;

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;

    List<BluetoothDevice> deviceList;
    public List<String> searchList;
    public String searchDevice;
    private ServiceConnection onService = null;
    Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 100000; //100 seconds
    private Handler mHandler;
    private boolean mScanning;
    public String deviceAddress;
    public boolean comStatus = false;
    public boolean isServiceConnected = false;


    private SharedPreferences sharedSettings;
    private final String SOUND="sound";
    private final String CURRENT_DOOR="current_door";
    private final String DOOR1_ADDRESS="door1_address";
    private final String DOOR2_ADDRESS="door2_address";
    private final String DOOR3_ADDRESS="door3_address";
    private final String DOOR1_PWD="door1_pwd";
    private final String DOOR2_PWD="door2_pwd";
    private final String DOOR3_PWD="door3_pwd";
    private final String DOOR1="door1";
    private final String DOOR2="door2";
    private final String DOOR3="door3";

    public boolean statusUpdate = false;
    public int reConnectAttempts = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        //Remove notification bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.main2);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.new_title);

        Settings_button = (ImageButton)findViewById(R.id.imageButton2);
        OpenClose_button = (ImageButton)findViewById(R.id.imageButton6);
        Hold_button = (ImageButton)findViewById(R.id.btnHold);
        SelectGate1_button = (ImageButton)findViewById(R.id.imageButton3);
        SelectGate2_button = (ImageButton)findViewById(R.id.imageButton4);
        SelectGate3_button = (ImageButton)findViewById(R.id.imageButton5);
        ConnectionStatus = new ImageView(this);
        ConnectionStatus = (ImageView)findViewById(R.id.imageView);

        sharedSettings = PreferenceManager.getDefaultSharedPreferences(this);

        writeSharedPreferences("gate1","found");
        //writeSharedPreferences(DOOR1_ADDRESS,"F1:C5:30:27:D2:4F");
        writeSharedPreferences(DOOR1_ADDRESS,"E6:0E:B2:BE:4F:8E");

        searchList = new ArrayList<String>();
        deviceAddress = "none";


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //Read the last used DOOR and try connecting to the same
        switch(readSharedPreferences(CURRENT_DOOR))
        {
            case DOOR1:

                Log.i(TAG, "DOOR1 selected");
                SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn_p);
                SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn);
                SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn);

                if(!readSharedPreferences(DOOR1_ADDRESS).equals(""))
                {
                    Log.i(TAG, "Checking DOOR1 availability");
                    checkDeviceAvailability(readSharedPreferences(DOOR1_ADDRESS));
                }
                else
                {
                    Toast.makeText(this, "Set Door1 configuration from settings", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "DOOR1 not configured");
                }
                break;

            case DOOR2:

                Log.i(TAG, "DOOR2 selected");
                SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn);
                SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn_p);
                SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn);

                if(!readSharedPreferences(DOOR2_ADDRESS).equals(""))
                {
                    Log.i(TAG, "Checking DOOR2 availability");
                    checkDeviceAvailability(readSharedPreferences(DOOR2_ADDRESS));
                }
                else
                {
                    Toast.makeText(this, "Set Door2 configuration from settings", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "DOOR2 not configured");
                }
                break;

            case DOOR3:

                Log.i(TAG, "DOOR3 selected");
                SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn);
                SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn);
                SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn_p);

                if(!readSharedPreferences(DOOR3_ADDRESS).equals(""))
                {
                    Log.i(TAG, "Checking DOOR3 availability");
                    checkDeviceAvailability(readSharedPreferences(DOOR3_ADDRESS));
                }
                else
                {
                    Toast.makeText(this, "Set Door3 configuration from settings", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "DOOR3 not configured");
                }
                break;

            default:

                Log.i(TAG, "Default selected");
                SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn_p);
                SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn);
                SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn);

                writeSharedPreferences(CURRENT_DOOR, DOOR1);
                if(!readSharedPreferences(DOOR1_ADDRESS).equals(""))
                {
                    checkDeviceAvailability(readSharedPreferences(DOOR1_ADDRESS));
                }
                else
                {
                    Toast.makeText(this, "Set Door1 configuration from settings", Toast.LENGTH_LONG).show();
                }

                break;
        }
        //service_init();
        //searchList.add(searchList.size(),"F1:C5:30:27:D2:4F");
        //checkDeviceAvailability("F1:C5:30:27:D2:4F");

//        if (!mBluetoothAdapter.isEnabled()) {
//            Log.i(TAG, "BT disabled..");
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        }
//        else {
//
//            service_init();
//            mHandler = new Handler();
//
//            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//                finish();
//            }
//
//            final BluetoothManager bluetoothManager =
//                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//            mBluetoothAdapter = bluetoothManager.getAdapter();
//
//            // Checks if Bluetooth is supported on the device.
//            if (mBluetoothAdapter == null) {
//                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//                finish();
//                return;
//            }
//            populateList();

//            if (btnConnectDisconnect.getText().equals("Connect")){
//
//                //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
//
//                Intent newIntent = new Intent(Main2Activity.this, DeviceListActivity.class);
//                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
//            } else {
//                //Disconnect button pressed
//                if (mDevice!=null)
//                {
//                    mService.disconnect();
//
//                }
//            }
//        }
//        Settings_button.setOnClickListener(this);
//        OpenClose_button.setOnClickListener(this);
//        Hold_button.setOnClickListener(this);
//        SelectGate1_button.setOnClickListener(this);
//        SelectGate2_button.setOnClickListener(this);
//        SelectGate3_button.setOnClickListener(this);

        Settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OpenSettings(v);
                Toast.makeText(getApplicationContext(),
                        "Settings", Toast.LENGTH_SHORT).show();
            }
        });

        OpenClose_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),
                        "OPenClose", Toast.LENGTH_SHORT).show();
                sendOpen();
            }
        });

        Hold_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendHold();

            }
        });

        SelectGate1_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                if(!(readSharedPreferences(CURRENT_DOOR).equals(DOOR1)))
                {
                    statusUpdate = false;
                    showToast("Door1 Selected");
                    Log.i(TAG, "DOOR1 selected Status 0");
                    if(mService != null)
                    {
                        mService.close();
                        setConnectionStatus(0);
                    }
                    setConnectionStatus(0);
                    isServiceConnected = false;
                    reConnectAttempts = 3;
                    SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn_p);
                    SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn);
                    SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn);

                    writeSharedPreferences(CURRENT_DOOR, DOOR1);

                    //if(mService != null)
                    //mService.disconnect();

                    if(!readSharedPreferences(DOOR1_ADDRESS).equals(""))
                    {
                        Log.i(TAG, "Checking DOOR1 availability");
                        checkDeviceAvailability(readSharedPreferences(DOOR1_ADDRESS));
                    }
                    else
                    {
                        showToast("Set Door1 configuration from settings");
                        Log.i(TAG, "DOOR1 not configured");

                    }

                }



                return true;
            }
        });

        SelectGate2_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                if(!(readSharedPreferences(CURRENT_DOOR).equals(DOOR2)))
                {
                    statusUpdate = false;
                    showToast("Door2 Selected");
                    Log.i(TAG, "DOOR2 selected Status 0");
                    if(mService != null)
                    {
                        mService.close();
                        setConnectionStatus(0);
                    }
                    setConnectionStatus(0);
                    isServiceConnected = false;
                    reConnectAttempts = 3;
                    SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn);
                    SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn_p);
                    SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn);

                    writeSharedPreferences(CURRENT_DOOR, DOOR2);

                    //if(mService != null)
                    //mService.disconnect();

                    if(!readSharedPreferences(DOOR2_ADDRESS).equals(""))
                    {
                        Log.i(TAG, "Checking DOOR2 availability");
                        checkDeviceAvailability(readSharedPreferences(DOOR2_ADDRESS));
                    }
                    else
                    {
                        showToast("Set Door2 configuration from settings");
                        Log.i(TAG, "DOOR2 not configured");

                    }

                }

                return true;

            }
        });

        SelectGate3_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(!(readSharedPreferences(CURRENT_DOOR).equals(DOOR3)))
                {
                    statusUpdate = false;
                    showToast("Door3 Selected");
                    Log.i(TAG, "DOOR3 selected Status 0");
                    if(mService != null)
                    {
                        mService.close();
                        setConnectionStatus(0);
                    }
                    setConnectionStatus(0);
                    isServiceConnected = false;
                    reConnectAttempts = 3;
                    SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn);
                    SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn);
                    SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn_p);

                    writeSharedPreferences(CURRENT_DOOR, DOOR3);

                    //if(mService != null)
                    //mService.disconnect();

                    if(!readSharedPreferences(DOOR3_ADDRESS).equals(""))
                    {
                        Log.i(TAG, "Checking DOOR3 availability");
                        checkDeviceAvailability(readSharedPreferences(DOOR3_ADDRESS));
                    }
                    else
                    {
                        showToast("Set Door3 configuration from settings");
                        Log.i(TAG, "DOOR3 not configured");

                    }

                }

                return true;
            }
        });


//        SelectGate2_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn);
//                SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn_p);
//                SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn);
//
//            }
//        });
//
//        SelectGate3_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                SelectGate1_button.setBackgroundResource(R.drawable.bg_gate_btn);
//                SelectGate2_button.setBackgroundResource(R.drawable.bg_gate_btn);
//                SelectGate3_button.setBackgroundResource(R.drawable.bg_gate_btn_p);
//            }
//        });

    }

    public void writeSharedPreferences(String key, String value) {

        SharedPreferences.Editor editor = sharedSettings.edit();
        editor.putString(key, value);
        editor.commit();
        Log.i(TAG, "Saved "+key+" "+value);

    }

    public String readSharedPreferences(String key) {

        Log.i(TAG, "read "+key);
        return sharedSettings.getString(key, "");

    }

    public void sendOpen()
    {
        if(comStatus) {
            String message = "nepO#321";
            byte[] value;
            try {
                //send data to service
                value = message.getBytes("UTF-8");
                mService.writeRXCharacteristic(value);
                Log.i(TAG, "Sent Open");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            showToast("Comms not ready");
            Log.i(TAG, "Notconnected");
        }

    }

    public void sendHold()
    {
        if(comStatus) {
            String message = "dloH#321";
            byte[] value;
            try {
                //send data to service
                value = message.getBytes("UTF-8");
                mService.writeRXCharacteristic(value);
                Log.i(TAG, "Sent Hold");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else
        {
            showToast("Comms not ready");
            Log.i(TAG, "Notconnected");
        }

    }
    public void showToast(String s)
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
    public void addAuthDevices(String address)
    {
        searchList.add(searchList.size(),address);

    }

    private boolean checkDeviceAvailability(String address)
    {
        //searchList.add(searchList.size(),address);
        searchDevice = address;
        Log.i(TAG, "Searching for " + address);
        //searchList.add(searchList.size(),"F1:C5:30:27:D2:4F");
        if(mScanning)
        {
            scanLeDevice(false);
            mHandler = null;

            Log.i(TAG, "Currently scanning from cDa ");
            if(mDevice != null)
            {
                mDevice = null;
            }
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.i(TAG, "BT disabled..");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {

            Log.i(TAG, "BLE service initializing ");
            service_init();
            mHandler = new Handler();

            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            }

            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                finish();
                return false;
            }

            Log.i(TAG, "Populating list ");
            populateList();

            return true;
        }
        return true;
    }
    private void populateList() {
        /* Initialize device list container */
        Log.i(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        devRssiValues = new HashMap<String, Integer>();

        Log.i(TAG, "Start scanning from popolate list");
        scanLeDevice(true);

    }

    //We will start scanning for BLE devices upon start up
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            Log.i(TAG, "Scanning enabled");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Log.i(TAG, "Scanning timeout");
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.i(TAG, "Scanning started from scanLeDevice");

        } else {

            Log.i(TAG, "Forced Scan stop from scanLeDevice");
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

        }

    }

    //After scanning mode is activated we will implement this method for its ongoing scan results
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "Device found "+device+" "+rssi);
                                    addDevice(device,rssi);
                                }
                            });

                        }
                    });
                }
            };

    //Will add devices to the device list which detected from leScan Call back
    private void addDevice(BluetoothDevice device, int rssi) {

        boolean deviceFound = false;

        //for (BluetoothDevice listDev : deviceList) {

            //If the same device already found, we will not add it again
            if (searchDevice.equals(device.getAddress())) {
                Log.i(TAG, "Device Match Occured "+device+" "+rssi);
                deviceFound = true;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.i(TAG, "Scan stopped from addDevice due to device match");

            }
        //}

        //populate the deviceList here. this list will later evaluated
        devRssiValues.put(device.getAddress(), rssi);

        if (!deviceFound) {

            //If the device address is listed under settings, then we add the device to the list
            //if(searchList.get(0) == null)
            Log.i(TAG, "Not a Matching device "+device+" "+rssi);
            if(searchDevice == null)
            {
                Log.i(TAG,"No Door has configured yet. Need at least one address");
                Toast.makeText(getApplicationContext(),
                        "Configure a Door from Settings", Toast.LENGTH_SHORT).show();
            }
            //if(device.getAddress().equals(searchList.get(0)))

            //Changed a lot here moved to else caluse
//            if(device.getAddress().equals(searchDevice))
//            {
//                deviceList.add(device);
//                deviceAddress = device.getAddress();
//                //Update the status image on UI
//                setConnectionStatus(1);
//
//                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
//
//                Log.d(TAG, "Selected Door.address==" + mDevice + "mserviceValue" + mService);
//                Toast.makeText(getApplicationContext(),mDevice.getName()+ " - connecting", Toast.LENGTH_SHORT).show();
//                mService.connect(deviceAddress);
//                isServiceConnected = true;
//            }

        }
        else
        {
            if(device.getAddress().equals(searchDevice))
            {
                deviceList.add(device);
                deviceAddress = device.getAddress();
                //Update the status image on UI
                setConnectionStatus(1);
                Log.i(TAG,"Set Status 1");
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                //service_init();
                if(mServiceConnection != null)
                    LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());

                Log.i(TAG, "Selected Door.address==" + mDevice + "mserviceValue" + mService);
                Toast.makeText(getApplicationContext(),mDevice.getName()+ " - connecting", Toast.LENGTH_SHORT).show();
                mService.connect(deviceAddress);
                isServiceConnected = true;
                Log.i(TAG,"Service connected");
            }
        }
    }




    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                setConnectionStatus(0);
                Log.i(TAG,"Status 0 due to service connection fail exiting..");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            if(mService != null)
            mService = null;
            Log.i(TAG,"Status 0 due to service disconnected..");
            setConnectionStatus(0);
        }
    };

    private void service_init() {

        Log.i(TAG,"Service initializing from Service_init");
        if(mServiceConnection != null) {
            Intent bindIntent = new Intent(this, UartService.class);
            bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

            //LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
            Log.i(TAG,"Service bound successfully");
        }
        else
        {
            Log.i(TAG,"Service bound fail from Service_init");
        }
    }


    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            byte[] initChar = {'0','1'};
            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //byte[] initChar = {'0','1'};
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        Log.i(TAG, "Set Status 2 due to UART coonection");
                        setConnectionStatus(2);
                        //mService.writeRXCharacteristic(initChar);
                        //Log.i(TAG, "Sent Init");
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        Log.i(TAG, "Set Status 0 due to UART disconection");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        setConnectionStatus(0);

                        if(reConnectAttempts > 0)
                        {
                            Log.i(TAG, "Attempt reconnect");
                            mService.connect(deviceAddress);
                            isServiceConnected = true;
                            Log.i(TAG,"Service connected");
                            reConnectAttempts --;
                        }
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                Log.d(TAG, "UART_SERVICE_DISCOVERED");
                mService.enableTXNotification();
                Log.i(TAG, "Set Status 3 due to UART service found");

//                mService.writeRXCharacteristic(initChar);
//                Log.i(TAG, "Sent Init");
//                mService.writeRXCharacteristic(initChar);
//                Log.i(TAG, "Sent Init");
//                mService.writeRXCharacteristic(initChar);
//                Log.i(TAG, "Sent Init");
                setConnectionStatus(3);

            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

                            //Log.d(TAG, "UART_WELCOME_RECEIVED");
                            if(statusUpdate) {
                                Log.i(TAG, "Set Status 4 upon UART welcome");
                                statusUpdate = false;
                            }
                            setConnectionStatus(4);
                            //listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                            //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
                Log.i(TAG, "No UART service supported Set status 0");
                setConnectionStatus(0);
            }


        }
    };

        private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
//        if (mState == UART_PROFILE_CONNECTED) {
//            Intent startMain = new Intent(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startMain);
//            showMessage("nRFUART's running in background.\n             Disconnect to exit");
//        }
//        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

//                            if(mBluetoothAdapter != null) {
//                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                            }
//
//                            if(mServiceConnection != null && isServiceConnected )
//                                unbindService(mServiceConnection);
//
//
//                            if (mDevice!=null && mService !=null)
//                            {
//                                if(isServiceConnected)
//                                mService.disconnect();
//
//                            }
//
//                            Log.d(TAG, "onBackOk()");
//                            unregister();
//
//                            if(mService != null) {
//                                mService.stopSelf();
//                                mService = null;
//                            }
                            Log.i(TAG, "exit 0");
                            //System.exit(0);

                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
//        }
    }

    public void unregister()
    {

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
    }
//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//
//            //Settings Menu button
//            case R.id.imageButton2:
//
//                OpenSettings(v);
//                Toast.makeText(getApplicationContext(),
//                        "Settings", Toast.LENGTH_SHORT).show();
//                break;
//
//            //Gate1 button
//            case R.id.imageButton3:
//
//                ((ImageButton)findViewById(R.id.imageButton3)).setBackgroundResource(R.drawable.bg_gate_btn_p);
//                ((ImageButton)findViewById(R.id.imageButton4)).setBackgroundResource(R.drawable.bg_gate_btn);
//                ((ImageButton)findViewById(R.id.imageButton5)).setBackgroundResource(R.drawable.bg_gate_btn);
//
//                ((ImageView)findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_1);
//
//                break;
//
//            //Gate2 button
//            case R.id.imageButton4:
//
//                ((ImageButton)findViewById(R.id.imageButton3)).setBackgroundResource(R.drawable.bg_gate_btn);
//                ((ImageButton)findViewById(R.id.imageButton4)).setBackgroundResource(R.drawable.bg_gate_btn_p);
//                ((ImageButton)findViewById(R.id.imageButton5)).setBackgroundResource(R.drawable.bg_gate_btn);
//
//                ((ImageView)findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_2);
//                //SelectGate1_button.setImageResource(R.drawable.bg_gate_btn);
//                //SelectGate2_button.setImageResource(R.drawable.bg_gate_btn_p);
//                //SelectGate3_button.setImageResource(R.drawable.bg_gate_btn);
//                break;
//
//            //gate3 Button
//            case R.id.imageButton5:
//
//                ((ImageButton)findViewById(R.id.imageButton3)).setBackgroundResource(R.drawable.bg_gate_btn);
//                ((ImageButton)findViewById(R.id.imageButton4)).setBackgroundResource(R.drawable.bg_gate_btn);
//                ((ImageButton)findViewById(R.id.imageButton5)).setBackgroundResource(R.drawable.bg_gate_btn_p);
//
//                ((ImageView)findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_3);
//                //SelectGate1_button.setImageResource(R.drawable.bg_gate_btn);
//                //SelectGate2_button.setImageResource(R.drawable.bg_gate_btn);
//                //SelectGate3_button.setImageResource(R.drawable.bg_gate_btn_p);
//                break;
//
//            //Hold Button
//            case R.id.btnHold:
//
//                ((ImageView)findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_4);
//                break;
//
//
//            //Open Close button
//            case R.id.imageButton6:
//
//
//                try {
//                   Thread.sleep(100);
//                }
//                catch(Exception e){
//                }
//
//
//
//                Toast.makeText(getApplicationContext(),
//                        "OpenClose", Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }

    public void setConnectionStatus(int status)
    {

        byte[] initChar = {'0','1'};

        switch (status) {

            case 0:
                ((ImageView) findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_0);
                comStatus = false;
                break;
            case 1:
                ((ImageView) findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_1);
                comStatus = false;
                break;
            case 2:
                ((ImageView) findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_2);
                comStatus = false;
                break;
            case 3:
                ((ImageView) findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_3);
                comStatus = false;
                try {
                    Thread.sleep(500);
                }catch(Exception e)
                 {

                }
                mService.writeRXCharacteristic(initChar);
                Log.i(TAG, "Sent Init");
                mService.writeRXCharacteristic(initChar);
                Log.i(TAG, "Sent Init");
                mService.writeRXCharacteristic(initChar);
                Log.i(TAG, "Sent Init");
                break;
            case 4:
                ((ImageView) findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_4);
                comStatus = true;
                break;
            default:
                ((ImageView) findViewById(R.id.imageView)).setBackgroundResource(R.drawable.bg_label_status_0);
                comStatus = false;
                break;
        }

    }
    public void OpenSettings(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }





    //Handle application lifecycle
    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");

        if(mService != null)
        {
            mService.close();
            setConnectionStatus(0);
        }

        if(mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.i(TAG, "Le Scan stopeed");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "on destroy");
        if(mBluetoothAdapter != null) {
            //mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);

        }
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        //if(mServiceConnection != null)
        //unbindService(mServiceConnection);

        if(mService != null) {
            Log.i(TAG, "stop service upon destroy");
            mService.stopSelf();
            mService = null;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        //onBackPressed();
        if(mService != null)
        {
            mService.close();
            mService.stopSelf();
            setConnectionStatus(0);
        }
        finish();
        //if(mHandler != null && mBluetoothAdapter != null)
        //scanLeDevice(false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(mBluetoothAdapter != null) {

            if (!mBluetoothAdapter.isEnabled()) {
                Log.i(TAG, "onResume - BT not enabled yet");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
        }

    }


}


























































