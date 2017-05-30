package example.com.mylibrary;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;

import java.util.Map;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

//    private final int callbackId;
    iHealthDevicesCallback mIHealthDevicesCallback = new iHealthDevicesCallback() {
        @Override
        public void onScanDevice(String s, String s1, int i) {
            super.onScanDevice(s, s1, i);
        }

        @Override
        public void onScanDevice(String s, String s1, int i, Map map) {
            super.onScanDevice(s, s1, i, map);
        }

        @Override
        public void onScanFinish() {
            super.onScanFinish();
        }

        @Override
        public void onDeviceConnectionStateChange(String s, String s1, int i, int i1) {
            super.onDeviceConnectionStateChange(s, s1, i, i1);
        }

        @Override
        public void onDeviceConnectionStateChange(String s, String s1, int i, int i1, Map map) {
            super.onDeviceConnectionStateChange(s, s1, i, i1, map);
        }

        @Override
        public void onUserStatus(String s, int i) {
            super.onUserStatus(s, i);
        }

        @Override
        public void onDeviceNotify(String s, String s1, String s2, String s3) {
            super.onDeviceNotify(s, s1, s2, s3);
        }
    };

    public ApplicationTest() {
        super(Application.class);

        iHealthDevicesManager.getInstance().init(getApplication());
//        callbackId = iHealthDevicesManager.getInstance().registerClientCallback(mIHealthDevicesCallback);
    }


}