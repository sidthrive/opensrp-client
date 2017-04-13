package org.opensrp.bpm;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mylibrary.R;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sid on 4/5/17.
 */
public class MainBPM extends Activity{

    private static final String TAG = MainBPM.class.getSimpleName();

    private static final int HANDLER_SCAN = 101;
    private static final int HANDLER_CONNECTED = 102;
    private static final int HANDLER_DISCONNECT = 103;
    private static final int HANDLER_USER_STATUE = 104;
    /**
     * Id to identify permissions request.
     */
    private static final int REQUEST_PERMISSIONS = 0;
    /*
     * userId the identification of the user, could be the form of email address or mobile phone
     * number (mobile phone number is not supported temporarily). clientID and clientSecret, as the
     * identification of the SDK, will be issued after the iHealth SDK registration. please contact
     * louie@ihealthlabs.com for registration.
     */
    String userName = "";
    String clientId = "";
    String clientSecret = "";


    private ListView listview_scan;
    private ListView listview_connected;
    private SimpleAdapter sa_scan;
    private SimpleAdapter sa_connected;
    private TextView tv_discovery;
    private List<HashMap<String, String>> list_ScanDevices = new ArrayList<HashMap<String, String>>();
    private List<HashMap<String, String>> list_ConnectedDevices = new ArrayList<HashMap<String, String>>();
    private int callbackId;

    private static ArrayList<DeviceStruct> deviceStructList = new ArrayList<>();

    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_SCAN:
                    Bundle bundle_scan = msg.getData();
                    String mac_scan = bundle_scan.getString("mac");
                    String type_scan = bundle_scan.getString("type");
                    HashMap<String, String> hm_scan = new HashMap<String, String>();
                    hm_scan.put("mac", mac_scan);
                    hm_scan.put("type", type_scan);
                    list_ScanDevices.add(hm_scan);
                    updateViewForScan();
                    break;

                case HANDLER_CONNECTED:
                    Bundle bundle_connect = msg.getData();
                    String mac_connect = bundle_connect.getString("mac");
                    String type_connect = bundle_connect.getString("type");
                    HashMap<String, String> hm_connect = new HashMap<String, String>();
                    hm_connect.put("mac", mac_connect);
                    hm_connect.put("type", type_connect);
                    list_ConnectedDevices.add(hm_connect);
                    updateViewForConnected();
                    Log.e(TAG, "idps:" + iHealthDevicesManager.getInstance().getDevicesIDPS(mac_connect));
                    list_ScanDevices.remove(hm_connect);
                    updateViewForScan();
                    break;

                case HANDLER_DISCONNECT:
                    Bundle bundle_disconnect = msg.getData();
                    String mac_disconnect = bundle_disconnect.getString("mac");
                    String type_disconnect = bundle_disconnect.getString("type");
                    HashMap<String, String> hm_disconnect = new HashMap<String, String>();
                    hm_disconnect.put("mac", mac_disconnect);
                    hm_disconnect.put("type", type_disconnect);
                    list_ConnectedDevices.remove(hm_disconnect);

                    updateViewForConnected();

                    break;
                case HANDLER_USER_STATUE:
                    Bundle bundle_status = msg.getData();
                    String username = bundle_status.getString("username");
                    String userstatus = bundle_status.getString("userstatus");
                    String str = "username:" + username + " - userstatus:" + userstatus;
                    Toast.makeText(MainBPM.this, str, Toast.LENGTH_LONG).show();

                    break;

                default:
                    break;
            }
        }
    };
    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onScanDevice(String mac, String deviceType, int rssi, Map manufactorData) {
            Log.i(TAG, "onScanDevice - mac:" + mac + " - deviceType:" + deviceType + " - rssi:" + rssi + " -manufactorData:" + manufactorData);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            msg.what = HANDLER_SCAN;
            msg.setData(bundle);
            myHandler.sendMessage(msg);

            connectToDev(mac, deviceType);

        }

        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID, Map manufactorData) {
            Log.e(TAG, "mac:" + mac + " deviceType:" + deviceType + " status:" + status + " errorid:" + errorID + " -manufactorData:" + manufactorData);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            if (status == iHealthDevicesManager.DEVICE_STATE_CONNECTED) {
                msg.what = HANDLER_CONNECTED;
            } else if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                msg.what = HANDLER_DISCONNECT;
            }
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

        @Override
        public void onUserStatus(String username, int userStatus) {
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            bundle.putString("userstatus", userStatus + "");
            Message msg = new Message();
            msg.what = HANDLER_USER_STATUE;
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

        @Override
        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
        }

        @Override
        public void onScanFinish() {
            tv_discovery.setText("discover finish");
        }

    };

    private void connectToDev(String mac, String deviceType) {
        Log.e(TAG, "connectToDev: username "+ userName );
        Log.e(TAG, "connectToDev: mac "+mac );
        Log.e(TAG, "connectToDev: deviceType "+ deviceType );
        boolean req = iHealthDevicesManager.getInstance().connectDevice(userName, mac, deviceType);
        if (!req) {
            Toast.makeText(MainBPM.this, "Haven’t permission to connect this device or the mac is not valid", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onCreate(Bundle bundle){
        setContentView(R.layout.main_bpm);

        super.onCreate(bundle);

        boolean btEnabled = isBTEnabled();

        Button bt_measure = (Button) findViewById(R.id.toMeasurements);
        Button bt_device = (Button) findViewById(R.id.toDevice);
        bt_device.setVisibility(View.GONE);
        if (!btEnabled){

            bt_measure.setEnabled(false);
            bt_measure.setText("DISABLED");
        } else {
            bt_measure.setText("START");
            bt_measure.setEnabled(true);
            bt_measure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainBPM.this, "Start Measurements..", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Toast.makeText(MainBPM.this, "BPM Module Ready", Toast.LENGTH_SHORT).show();

        checkBTStatus();

        // from main
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        if (fab != null) {
//            fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            });
//        }

        Log.e(TAG, "Model:" + Build.MODEL + " api:" + Build.VERSION.SDK_INT + " version:" + Build.VERSION.RELEASE);


//        findViewById(R.id.btn_discorvery).setOnClickListener(this);
//        findViewById(R.id.btn_stopdiscorvery).setOnClickListener(this);
//        findViewById(R.id.btn_Certification).setOnClickListener(this);

        tv_discovery = (TextView) findViewById(R.id.tv_discovery);
        listview_scan = (ListView) findViewById(R.id.list_scan);
        listview_connected = (ListView) findViewById(R.id.list_connected);
        if (list_ConnectedDevices != null)
            list_ConnectedDevices.clear();
        if (list_ScanDevices != null)
            list_ScanDevices.clear();
        sa_scan = new SimpleAdapter(this, this.list_ScanDevices, R.layout.bp_listview_baseview,
                new String[]{
                        "type", "mac"
                },
                new int[]{
                        R.id.tv_type, R.id.tv_mac
                });

        listview_scan.setAdapter(sa_scan);
        listview_scan.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                HashMap<String, String> hm = list_ScanDevices.get(position);
                String type = hm.get("type");
                String mac = hm.get("mac");
                Log.i(TAG, "mac = " + mac);
                boolean req = iHealthDevicesManager.getInstance().connectDevice(userName, mac, type);
                if (!req) {
                    Toast.makeText(MainBPM.this, "Haven’t permission to connect this device or the mac is not valid", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*
         * Initializes the iHealth devices manager. Can discovery available iHealth devices nearby
         * and connect these devices through iHealthDevicesManager.
         */
        iHealthDevicesManager.getInstance().init(this);

        /*
         * Register callback to the manager. This method will return a callback Id.
        */

        callbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);


        checkPermissions();
        SharedPreferences mySharedPreferences = getSharedPreferences("preference", MODE_PRIVATE);
        long discoveryType = mySharedPreferences.getLong("discoveryType", 0);
        for (DeviceStruct struct : deviceStructList) {
            struct.isSelected = ((discoveryType & struct.type) != 0);
        }

        startDiscovery();
    }

    private void checkBTStatus() {
        BroadcastReceiver bState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED))
                {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state)
                    {
                        case BluetoothAdapter.STATE_CONNECTED:
                        {
                            //Do something you need here
                            System.out.println("Connected");
                            Toast.makeText(MainBPM.this, "CONNECTED", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        default:
                            System.out.println("Default");
                            break;
                    }
                }
            }
        };
    }

    private boolean isBTEnabled() {
        boolean status;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if (mBluetoothAdapter == null ){
             Toast.makeText(MainBPM.this, "Device not support Bluetooth", Toast.LENGTH_SHORT).show();
             status = false;
         } else {
             if (!mBluetoothAdapter.isEnabled()){
                 Toast.makeText(MainBPM.this, "Bluetooth is Not Enabled", Toast.LENGTH_SHORT).show();
                 status = false;
             } else {
                 status = true;
             }

         }
        return status;
    }

    private void toMeasurements(){
        Toast.makeText(MainBPM.this, "Start ...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
         * When the Activity is destroyed , need to call unRegisterClientCallback method to
         * unregister callback
         */
        iHealthDevicesManager.getInstance().unRegisterClientCallback(callbackId);
        /*
         * When the Activity is destroyed , need to call destroy method of iHealthDeivcesManager to
         * release resources
         */
        iHealthDevicesManager.getInstance().destroy();
    }


    private void updateViewForScan() {
        sa_scan.notifyDataSetChanged();
        ViewGroup.LayoutParams params = listview_scan.getLayoutParams();
        params.height = dp2px(list_ScanDevices.size() * 48 + 5);
        listview_scan.setLayoutParams(params);
    }

    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void updateViewForConnected() {
        sa_connected = new SimpleAdapter(this, this.list_ConnectedDevices, R.layout.bp_listview_baseview,
                new String[]{
                        "type", "mac"
                },
                new int[]{
                        R.id.tv_type, R.id.tv_mac
                });

        listview_connected.setAdapter(sa_connected);
        listview_connected.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                HashMap<String, String> hm = list_ConnectedDevices.get(position);
                String type = hm.get("type");
                String mac = hm.get("mac");
                Intent intent = new Intent();
                intent.putExtra("mac", mac);
                if (iHealthDevicesManager.TYPE_BP7.equals(type)) {
                    intent.setClass(MainBPM.this, BP7.class);
                    startActivity(intent);

                }
            }
        });
        sa_connected.notifyDataSetChanged();
    }

    private static class DeviceStruct {
        String name;
        long type;
        boolean isSelected;
    }

    private void startDiscovery() {
        long discoveryType = 67108864;
//        long discoveryType = 0;
//        for (DeviceStruct struct : deviceStructList) {
//            if (struct.isSelected) {
//                discoveryType |= struct.type;
//            }
//        }
        SharedPreferences mySharedPreferences = getSharedPreferences("preference", MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putLong("discoveryType", discoveryType);
        editor.apply();
        if (discoveryType != 0) {
            iHealthDevicesManager.getInstance().startDiscovery(discoveryType);
            tv_discovery.setText("discovering...");
        }
    }

    private void checkPermissions() {
        StringBuilder tempRequest = new StringBuilder();

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            tempRequest.append(Manifest.permission.WRITE_EXTERNAL_STORAGE + ",");
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            tempRequest.append(Manifest.permission.RECORD_AUDIO + ",");
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            tempRequest.append(Manifest.permission.ACCESS_FINE_LOCATION + ",");
//        }
//        if (tempRequest.length() > 0) {
//            tempRequest.deleteCharAt(tempRequest.length() - 1);
//            ActivityCompat.requestPermissions(this, tempRequest.toString().split(","), REQUEST_PERMISSIONS);
//        }
    }




}
