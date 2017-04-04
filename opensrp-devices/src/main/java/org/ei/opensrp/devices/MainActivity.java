package org.ei.opensrp.devices;

import android.app.Activity;
import android.content.pm.PackageManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import org.ei.opensrp.device.R;

import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS = 0;
    private int callBackId;
    TextView tv_status;
    int toolType = 67108864;
    private String MAC;
    private String devType;

    private iHealthDevicesCallback miIHealthDevicesCallback = new iHealthDevicesCallback() {
        @Override
        public void onScanDevice(String mac, String deviceType, int rssi, Map manufactureData) {
            Log.e(TAG, "onScanDevice: mac : " + mac + " devType : " + deviceType + " - rssi : " + rssi);

            setMAC(mac);
            setDevType(deviceType);
        }

        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID, Map manufactureData) {
            Log.e(TAG, "onDeviceConnectionStateChange: mac: "+ mac +" "+ deviceType +" "+ status + " " + errorID +" "+ manufactureData );
        }

        @Override
        public void onScanFinish(){
            tv_status.setText("Pairing End");
            connectDevice();
        }
    };

    private static ArrayList<DeviceStruct> deviceStructList = new ArrayList<>();


    static {
        Field[] fields = iHealthDevicesManager.class.getFields();
        for (Field field : fields){
            String fieldname = field.getName();
            if (fieldname.contains("DISCOVERY_")){
                DeviceStruct struct = new DeviceStruct();
                struct.name = fieldname.substring(10);

                try {
                    struct.type = field.getLong(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                deviceStructList.add(struct);
            }
        }
    }




    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public String getDevType() {
        return devType;
    }

    public String getMAC() {
        return MAC;
    }

    private static class DeviceStruct {
        String name;
        long type;
        boolean selected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_status = (TextView) findViewById(R.id.tv_status);

        iHealthDevicesManager.getInstance().init(this);

        callBackId = iHealthDevicesManager.getInstance().registerClientCallback(miIHealthDevicesCallback);

//        iHealthDevicesManager.getInstance().startDiscovery(toolType);
        checkPermissions();
        autoPaired();

    }

    private void autoPaired() {
        long discoveryType = 0;

//        type for BP7 67108864
        int toolType = 67108864;

        iHealthDevicesManager.getInstance().startDiscovery(toolType);
        tv_status.setText("Pairing...");


    }

    public void connectDevice(){
        String userName = "";
        Log.e(TAG, "connectDevice: "+ iHealthDevicesManager.getInstance() );
        boolean result = iHealthDevicesManager.getInstance().connectDevice(userName, getMAC(), getDevType() );
        Log.e(TAG, "autoPaired: " + result );

    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        iHealthDevicesManager.getInstance().unRegisterClientCallback(callBackId);

        iHealthDevicesManager.getInstance().destroy();
    }

    private void checkPermissions() {
        StringBuilder tempRequest = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            tempRequest.append(android.Manifest.permission.WRITE_EXTERNAL_STORAGE + ",");
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            tempRequest.append(android.Manifest.permission.RECORD_AUDIO + ",");
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tempRequest.append(android.Manifest.permission.ACCESS_FINE_LOCATION + ",");
        }
        if (tempRequest.length() > 0) {
            tempRequest.deleteCharAt(tempRequest.length() - 1);
            ActivityCompat.requestPermissions(this, tempRequest.toString().split(","), REQUEST_PERMISSIONS);
        }
    }



}
