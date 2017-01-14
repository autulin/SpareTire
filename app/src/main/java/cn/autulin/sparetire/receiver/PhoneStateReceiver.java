package cn.autulin.sparetire.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import cn.autulin.sparetire.utils.PrefUtils;
import cn.autulin.sparetire.utils.PushUtils;

public class PhoneStateReceiver extends BroadcastReceiver {
    public PhoneStateReceiver() {
    }

    private static int lastCallState = TelephonyManager.CALL_STATE_IDLE;
    private static final String TAG = "PhoneStateReceiver";
    private static final String[] PROJECT = new String[]{
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.NEW,
            CallLog.Calls.DATE
    };
    private long mMissedCallDate = 0;

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PrefUtils.getIdentify(context) == PrefUtils.IDENTIFY_HOST) {
            return;
        }
        this.context = context;
        String action = intent.getAction();
        Log.d(TAG, action);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int currentCallState = telephonyManager.getCallState();
        Log.d(TAG, "currentCallState=" + currentCallState);

        if (lastCallState == TelephonyManager.CALL_STATE_RINGING &&
                currentCallState == TelephonyManager.CALL_STATE_IDLE) { //未接来电
//            sendMsg(context);
            new Handler().postDelayed(run, 3000); //需要等一段时间从数据库读取，因为系统来电写入到数据库需要一定的时间
        }
        lastCallState = currentCallState;
    }

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            sendMsg(context);
        }
    };

    private void sendMsg(Context mContext) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) { //Manifest.permission.READ_CALL_LOG权限要api大于16
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        Cursor cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                PROJECT, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

//        NotifyUtils.showNotification(context,"有电话来了","调试");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                if (type == CallLog.Calls.MISSED_TYPE) {
                    Log.v(TAG, "missed type");
                    if (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW)) == 1) {
                        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                        String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                        long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                        if (date != mMissedCallDate) {
                            Log.d(TAG, " not the same missed!" + date);
                            mMissedCallDate = date;
                            PushUtils.pushMissedCall(mContext, name, number, date);
                        } else {
                            Log.d(TAG, " The same missed call, ignore it!");
                        }
                    }
                }
            }
            cursor.close();
        }
    }

}
