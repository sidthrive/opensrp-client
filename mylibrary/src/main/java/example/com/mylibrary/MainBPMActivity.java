package example.com.mylibrary;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;

import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;

public class MainBPMActivity extends Activity {

//    private String uname;
//    private java.lang.String mac;
//    private String type;

    String uname = "anudroid.apk06@gmail.com";
    String clientId = "708bde5b65884f8d9e579e33e66e8e80";
    String clientSecret = "38ff62374a0d4aacadaf0e4fb4ed1931";
    long discoveryType = 67108864;


    private iHealthDevicesCallback mIHealthDevicesCallback = new iHealthDevicesCallback() {
    };
    private int callbackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iHealthDevicesManager.getInstance().init(this);

        callbackId = iHealthDevicesManager.getInstance().registerClientCallback(mIHealthDevicesCallback);

        iHealthDevicesManager.getInstance().sdkUserInAuthor(MainBPMActivity.this, uname, clientId, clientSecret, callbackId);
//        boolean req = iHealthDevicesManager.getInstance().connectDevice(uname, mac, type);
    }
}
