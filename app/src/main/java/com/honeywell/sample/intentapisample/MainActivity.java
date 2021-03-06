package com.honeywell.sample.intentapisample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "IntentApiSample";

    private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.MY_BARCODE_DATA";
    //TODO: Do not use the below barcode data action string, it will not work or only once on first deploy
    //private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA";

    /**
     * Honeywell DataCollection Intent API
     * Claim scanner
     * Package Permissions:
     * "com.honeywell.decode.permission.DECODE"
     */
    private static final String ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER";
    /**
     * Honeywell DataCollection Intent API
     * Release scanner claim
     * Permissions:
     * "com.honeywell.decode.permission.DECODE"
     */
    private static final String ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER";
    /**
     * Honeywell DataCollection Intent API
     * Optional. Sets the scanner to claim. If scanner is not available or if extra is not used,
     * DataCollection will choose an available scanner.
     * Values : String
     * "dcs.scanner.imager" : Uses the internal scanner
     * "dcs.scanner.ring" : Uses the external ring scanner
     */
    private static final String EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER";
    /**
     * Honeywell DataCollection Intent API
     * Optional. Sets the profile to use. If profile is not available or if extra is not used,
     * the scanner will use factory default properties (not "DEFAULT" profile properties).
     * Values : String
     */
    private static final String EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE";
    /**
     * Honeywell DataCollection Intent API
     * Optional. Overrides the profile properties (non-persistent) until the next scanner claim.
     * Values : Bundle
     */
    private static final String EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES";

    private static final String EXTRA_CONTROL = "com.honeywell.aidc.action.ACTION_CONTROL_SCANNER";
    /*
        Extras
        "com.honeywell.aidc.extra.EXTRA_SCAN" (boolean): Set to true to start or continue scanning. Set to false to stop scanning. Most scenarios only need this extra, however the scanner can be put into other states by adding from the following extras.
        "com.honeywell.aidc.extra.EXTRA_AIM" (boolean): Specify whether to turn the scanner aimer on or off. This is optional; the default value is the value of EXTRA_SCAN.
        "com.honeywell.aidc.extra.EXTRA_LIGHT" (boolean): Specify whether to turn the scanner illumination on or off. This is optional; the default value is the value of EXTRA_SCAN.
        "com.honeywell.aidc.extra.EXTRA_DECODE" (boolean): Specify whether to turn the decoding operation on or off. This is optional; the default value is the value of EXTRA_SCAN
    */
    private static final String EXTRA_SCAN = "com.honeywell.aidc.extra.EXTRA_SCAN";

    private TextView textView;
    Button button;
    int sdkVersion=0;


    private BroadcastReceiver barcodeDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("IntentApiSample: ","onReceive");
            if (ACTION_BARCODE_DATA.equals(intent.getAction())) {
/*
These extras are available:
"version" (int) = Data Intent Api version
"aimId" (String) = The AIM Identifier
"charset" (String) = The charset used to convert "dataBytes" to "data" string
"codeId" (String) = The Honeywell Symbology Identifier
"data" (String) = The barcode data as a string
"dataBytes" (byte[]) = The barcode data as a byte array
"timestamp" (String) = The barcode timestamp
*/
                int version = intent.getIntExtra("version", 0);
                if (version >= 1) {
                    String aimId = intent.getStringExtra("aimId");
                    String charset = intent.getStringExtra("charset");
                    String codeId = intent.getStringExtra("codeId");
                    String data = intent.getStringExtra("data");
                    byte[] dataBytes = intent.getByteArrayExtra("dataBytes");
                    String dataBytesStr="";
                    if(dataBytes!=null && dataBytes.length>0)
                        dataBytesStr = bytesToHexString(dataBytes);
                    String timestamp = intent.getStringExtra("timestamp");
                    String text = String.format(
                            "Data:%s\n" +
                                    "Charset:%s\n" +
                                    "Bytes:%s\n" +
                                    "AimId:%s\n" +
                                    "CodeId:%s\n" +
                                    "Timestamp:%s\n",
                            data, charset, dataBytesStr, aimId, codeId, timestamp);
                    setText(text);
                }
            }
        }
    };

    private static void sendImplicitBroadcast(Context ctxt, Intent i) {
        PackageManager pm = ctxt.getPackageManager();
        List<ResolveInfo> matches = pm.queryBroadcastReceivers(i, 0);
        if (matches.size() > 0) {
            for (ResolveInfo resolveInfo : matches) {
                Intent explicit = new Intent(i);
                ComponentName cn =
                        new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                                resolveInfo.activityInfo.name);

                explicit.setComponent(cn);
                ctxt.sendBroadcast(explicit);
            }

        } else{
            // to be compatible with Android 9 and later version for dynamic receiver
            ctxt.sendBroadcast(i);
        }

    }

    private  void mysendBroadcast(Intent intent){

        if(sdkVersion<26) {
            sendBroadcast(intent);
        }else {
            //for Android O above "gives W/BroadcastQueue: Background execution not allowed: receiving Intent"
            //either set targetSDKversion to 25 or use implicit broadcast
            sendImplicitBroadcast(getApplicationContext(), intent);
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button)findViewById(R.id.button);
        button.setText("Start Scan");

        sdkVersion = android.os.Build.VERSION.SDK_INT;
        Log.d(TAG, "sdkVersion=" + sdkVersion+"\n");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mysendBroadcast(new Intent(EXTRA_CONTROL).putExtra(EXTRA_SCAN, true));
                //software defined decode timeout!
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        mysendBroadcast(new Intent(EXTRA_CONTROL).putExtra(EXTRA_SCAN, false));
                    }
                }, 3000);
            }
        });
        CheckBox checkBox=findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String controlMode=(b?ConstantValues.TRIGGER_CONTROL_MODE_AUTO_CONTROL:ConstantValues.TRIGGER_CONTROL_MODE_DISABLE);
                Bundle properties = new Bundle();
                properties.putString(ConstantValues.PROPERTY_TRIGGER_CONTROL_MODE, controlMode);
                mysendBroadcast(new Intent(ACTION_CLAIM_SCANNER)
                        .putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
                        .putExtra(EXTRA_PROFILE, "DEFAULT")// "MyProfile1")
                        .putExtra(EXTRA_PROPERTIES, properties)
                );
            }
        });
        claimScanner();
        Log.d("IntentApiSample: ", "onCreate");
    }
    @Override
    protected void onResume() {
        super.onResume();
//        IntentFilter intentFilter = new IntentFilter("hsm.RECVRBI");
        registerReceiver(barcodeDataReceiver, new IntentFilter(ACTION_BARCODE_DATA));
        claimScanner();
        Log.d("IntentApiSample: ", "onResume");
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(barcodeDataReceiver);
        releaseScanner();
        Log.d("IntentApiSample: ", "onPause");
    }
    private void claimScanner() {
        Log.d("IntentApiSample: ", "claimScanner");
        Bundle properties = new Bundle();
        properties.putBoolean("DPR_DATA_INTENT", true);
        properties.putString("DPR_DATA_INTENT_ACTION", ACTION_BARCODE_DATA);

        properties.putInt("TRIG_AUTO_MODE_TIMEOUT", 2);
        properties.putString("TRIG_SCAN_MODE", "readOnRelease"); //This works for Hardware Trigger only! If scan is started from code, the code is responsible for a switching off the scanner before a decode

        //change some barcode properties
        final String PROPERTY_UPC_A_ENABLE="DEC_UPCA_ENABLE";
        final String PROPERTY_UPC_E_ENABLED="DEC_UPCE0_ENABLED";

        properties.putBoolean(PROPERTY_UPC_A_ENABLE, true);
        properties.putBoolean(PROPERTY_UPC_E_ENABLED, true);

        //or, with use of custom ConstantValues class
        properties.putBoolean(ConstantValues.PROPERTY_UPC_A_CHECK_DIGIT_TRANSMIT_ENABLED, true);

        mysendBroadcast(new Intent(ACTION_CLAIM_SCANNER)
                .putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
                .putExtra(EXTRA_PROFILE, "DEFAULT")// "MyProfile1")
                .putExtra(EXTRA_PROPERTIES, properties)
        );
    }
    private void releaseScanner() {
        Log.d("IntentApiSample: ", "releaseScanner");
        mysendBroadcast(new Intent(ACTION_RELEASE_SCANNER));
    }
    private void setText(final String text) {
        if (textView != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(text);
                }
            });
        }
    }
    private String bytesToHexString(byte[] arr) {
        String s = "[]";
        if (arr != null) {
            s = "[";
            for (int i = 0; i < arr.length; i++) {
                s += "0x" + Integer.toHexString(arr[i]) + ", ";
            }
            s = s.substring(0, s.length() - 2) + "]";
        }
        return s;
    }
}