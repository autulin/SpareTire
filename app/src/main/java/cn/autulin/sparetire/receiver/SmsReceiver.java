package cn.autulin.sparetire.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import cn.autulin.sparetire.utils.PrefUtils;
import cn.autulin.sparetire.utils.PushUtils;

public class SmsReceiver extends BroadcastReceiver {
    public SmsReceiver() {
    }

    private static final String TAG = "SmsReceiver";

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PrefUtils.getIdentify(context) == PrefUtils.IDENTIFY_HOST) {
            return;
        }


        this.context = context;
        new Handler().postDelayed(run, 3000); //需要等一段时间从数据库读取，因为短信写入到数据库需要一定的时间

        /*这个方法不用访问数据库，但获取不到号码的备注*/
//        Bundle bundle = intent.getExtras();
//        Log.d(TAG, bundle.toString());
//        Object[] smsObj = (Object[]) bundle.get("pdus");
//        SmsMessage sms = null;
//        StringBuilder sb = new StringBuilder();  //长短信需要拼接
//        for (Object object : smsObj) {
//            sms = SmsMessage.createFromPdu((byte[]) object);
//            sb.append(sms.getDisplayMessageBody());
//        }
//
//        Log.e(TAG, "number:" + sms.getOriginatingAddress()
//                + "   body:" + sb.toString()
//                + "  time:" + sms.getTimestampMillis()
//                );

//        action(context);
    }

    private Uri SMS_INBOX = Uri.parse("content://sms/");
    private static final String[] PROJECT = new String[]{
            "address",
            "person",
            "date",
            "body"
    };

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            action(context);
        }
    };

    private void action(final Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(SMS_INBOX, PROJECT, null, null, "date DESC");
        if (null == cur)
            return;
        if (cur.moveToFirst()) {
//            int read = cur.getInt(cur.getColumnIndex("read"));

            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));
            long date = cur.getLong(cur.getColumnIndex("date"));
            Log.e(TAG, "number:" + number
                    + "  person:" + name
                    + "   body:" + body
                    + "  time:" + date
            );
            PushUtils.pushSms(context, number, name, body, date);
        }

        cur.close();
    }

}