package example.com.mylibrary;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

/**
 * Created by sid on 5/2/17.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private static final String TAG = "MainActivityTest";
    @Rule
    public ActivityTestRule<MainBPMActivity> mainBPMActivityActivityTestRule= new ActivityTestRule<>(MainBPMActivity.class);
    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {
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
    private int cbId;

    @Test
    public void testConnect(){
        iHealthDevicesManager.getInstance().init(InstrumentationRegistry.getTargetContext());

        cbId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);

        Log.e(TAG, "testConnect: "+ cbId);
    }
}
