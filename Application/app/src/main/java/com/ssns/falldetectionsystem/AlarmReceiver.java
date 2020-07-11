package com.ssns.falldetectionsystem;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction()=="ALARM_ACTION") {
            Log.d("ALARM!!!", "ALARM");
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Intent stopIntent = new Intent(context, RingtonePlayingService.class);
            context.stopService(stopIntent);
            Intent startIntent = new Intent(context, RingtonePlayingService.class);
            startIntent.putExtra("ringtone-uri", ringtoneUri);
            context.startService(startIntent);
            Log.d("Alarm!!!", "Alarm triggered");
        }
        if(intent.getAction()=="YES_ACTION") {
            Log.d("Yes!!!", "Clicked yes");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(101);
            ActivityMonitoring.startTimer = 0;
            ActivityMonitoring.setAlarmFlag(true);
            Intent stopIntent = new Intent(context, RingtonePlayingService.class);
            context.stopService(stopIntent);
        }


    }
}
