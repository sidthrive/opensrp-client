package com.example.mylibrary;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by sid on 4/5/17.
 */
public class MainBPM extends Activity{

    @Override
    public void onCreate(Bundle bundle){
        setContentView(R.layout.main_bpm);

        super.onCreate(bundle);

        Toast.makeText(MainBPM.this, "BPM Module", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
