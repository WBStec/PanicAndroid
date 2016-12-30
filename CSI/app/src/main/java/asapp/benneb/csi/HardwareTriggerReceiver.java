package asapp.benneb.csi;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;

public class HardwareTriggerReceiver extends BroadcastReceiver {
    private static final String TAG = HardwareTriggerReceiver.class.getName();
    //    private MultiClickEvent multiClickEvent;
    protected MultiClickEvent multiClickEvent;

    public HardwareTriggerReceiver() {
        resetEvent();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context,"bla", Toast.LENGTH_LONG).show();
        Log.e(">>>>>>>", "in onReceive of HWReceiver");
        String action = intent.getAction();
        if (!isCallActive(context) && (action.equals(ACTION_SCREEN_OFF) || action.equals(ACTION_SCREEN_ON))) {
            multiClickEvent.registerClick(System.currentTimeMillis());

            if(multiClickEvent.skipCurrentClick()){
                Log.e("*****", "skipped click");
                multiClickEvent.resetSkipCurrentClickFlag();
            }

            else if(multiClickEvent.canStartVibration()){
                Log.e("*****", "vibration started");
                try {
                    PanicAlert panicAlert = getPanicAlert(context);
                    panicAlert.vibrate();
//                Intent i = new Intent();
//                i.setClass(context, MainActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(i);


                    panicAlert.getGPS();
                    panicAlert.takePhoto();
                    //panicAlert.mms();
                }catch (Exception e)
                {
                    Log.d("123",e.getMessage());
                }

            }

            else if (multiClickEvent.isActivated()) {
                Log.e("*****", "alerts activated");
                onActivation(context);
                resetEvent();
            }
        }
    }

    protected void onActivation(Context context) {
        Log.e(">>>>>>>", "in onActivation of HWReceiver");
        // activateAlert(getPanicAlert(context));
    }

    void activateAlert(PanicAlert panicAlert) {
//        panicAlert.start();
        // panicAlert.activate();
    }

    protected void resetEvent() {
        multiClickEvent = new MultiClickEvent();
    }

    protected PanicAlert getPanicAlert(Context context) {
        return new PanicAlert(context,null);
    }

    private boolean isCallActive(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return manager.getMode() == AudioManager.MODE_IN_CALL;
    }
}