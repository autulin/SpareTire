package cn.autulin.sparetire.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

import cn.autulin.sparetire.ui.MainActivity;
import cn.autulin.sparetire.R;
import io.yunba.android.manager.YunBaManager;

/**
 * Created by autulin on 2016/4/26.
 */
public class NotifyUtils {
    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }

    public static Boolean showNotification(final Context context, String msg, String title) {
        try {
            Uri alarmSound = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            long[] pattern = {500, 500, 500};


            NotificationCompat.BigTextStyle bigTextStyle= new NotificationCompat.BigTextStyle(); //可扩展通知
            bigTextStyle.bigText(msg);



            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title).setContentText(msg)
                    .setSound(alarmSound).setVibrate(pattern).setAutoCancel(true).setStyle(bigTextStyle);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, MainActivity.class);
            if (!isEmpty(title))
                resultIntent.putExtra(YunBaManager.MQTT_TOPIC, title);
            if (!isEmpty(msg))
                resultIntent.putExtra(YunBaManager.MQTT_MSG, msg);
            // The stack builder object will contain an artificial back stack
            // for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out
            // of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);

//            RemoteViews remoteViews = new RemoteViews()

            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            Random r = new Random();
            mNotificationManager.notify(r.nextInt(), mBuilder.build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static void showToast(final Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void logE(String msg){
        Log.e("test",msg);
    }
}
