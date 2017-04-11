package org.opensrp.bpm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mylibrary.R;
/**
 * Created by sid on 4/5/17.
 */
public class MainBPM extends Activity{

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
}
