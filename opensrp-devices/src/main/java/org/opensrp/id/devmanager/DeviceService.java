package org.opensrp.id.devmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by sid on 4/25/17.
 */
public class DeviceService extends Service {

    Thread t = new PeriodicUpdate();

    @Override
    public void onCreate(){
        super.onCreate();
        // start thread
        t.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (!t.isAlive()){
            t.start();
        }
        return Service.START_REDELIVER_INTENT;
    }
}
