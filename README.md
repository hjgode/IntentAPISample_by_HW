# IntentAPISample_by_HW
The Intent API Sample code with 'small' changes

The API runs flawless on Android 4.x, 6.x and 7.x devices but then google strikes back.

        if(sdkVersion<26) {
            sendBroadcast(intent);
        }else {
            //for Android O above "gives W/BroadcastQueue: Background execution not allowed: receiving Intent"
            //either set targetSDKversion to 25 or use implicit broadcast
            sendImplicitBroadcast(getApplicationContext(), intent);
        }

No more simple global broadcasts in Android 8. You need to use package manager to grab the packages for an Intent and send explicit broadcast:

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
            ...
        }

Then comes Android 9 and you need to use a 'dynamic' broadcast:

        else{
            // to be compatible with Android 9 and later version for dynamic receiver
            ctxt.sendBroadcast(i);
        }
    }
   
Another weird things is with the Datacollections service. If you use "com.honeywell.sample.action.BARCODE_DATA" to register the broadcast receiver to get the barcode intent, you will get no or only once a callback. The Datacollection Service seems to use this Intent internally.
If you use another regitsration name, for example "com.honeywell.sample.action.MY_BARCODE_DATA", everything runs fine. Strange... :-(

# Additional changes

* added all known constant values from the Android SDK, see ConstantValues.java.

* added checkbox to show how to disable scanner. Note: this does not disable the scanner but the hardware trigger buttons do not fire the scanner, if disabled. The scanner can be fired at any time from code.

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