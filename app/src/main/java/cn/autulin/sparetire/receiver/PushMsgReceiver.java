package cn.autulin.sparetire.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.autulin.sparetire.db.DBManager;
import cn.autulin.sparetire.db.RecordItem;
import cn.autulin.sparetire.ui.MyApplication;
import cn.autulin.sparetire.ui.SlaveBindActivity;
import cn.autulin.sparetire.utils.Constants;
import cn.autulin.sparetire.utils.NotifyUtils;
import cn.autulin.sparetire.utils.PrefUtils;
import cn.autulin.sparetire.utils.Tools;
import io.yunba.android.manager.YunBaManager;

public class PushMsgReceiver extends BroadcastReceiver {

    private final static String REPORT_MSG_SHOW_NOTIFICARION = "1000";
    private final static String REPORT_MSG_SHOW_NOTIFICARION_FAILED = "1001";

    public PushMsgReceiver() {
    }

    private DBManager dbManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {

            String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
            String msg = intent.getStringExtra(YunBaManager.MQTT_MSG);

            NotifyUtils.logE("收到推送信息：" + msg);
            try {
                handleMsg(context, msg);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
//            StringBuilder showMsg =  new StringBuilder();
//            showMsg.append("Received message from server: ").append(YunBaManager.MQTT_TOPIC)
//                    .append(" = ").append(topic).append(" ")
//                    .append(YunBaManager.MQTT_MSG).append(" = ").append(msg);
//            NotifyUtils.logE(showMsg.toString());
//            boolean flag = NotifyUtils.showNotification(context, topic, msg);
//            //上报显示通知栏状态， 以方便后台统计
//            if (flag) YunBaManager.report(context, REPORT_MSG_SHOW_NOTIFICARION, topic);
//            else  YunBaManager.report(context, REPORT_MSG_SHOW_NOTIFICARION_FAILED, topic);

            // send msg to app

        } else if (YunBaManager.PRESENCE_RECEIVED_ACTION.equals(intent.getAction())) {
            //msg from presence.
            String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
            String payload = intent.getStringExtra(YunBaManager.MQTT_MSG);
            StringBuilder showMsg = new StringBuilder();
            showMsg.append("Received message presence: ").append(YunBaManager.MQTT_TOPIC)
                    .append(" = ").append(topic).append(" ")
                    .append(YunBaManager.MQTT_MSG).append(" = ").append(payload);
            Log.d("DemoReceiver", showMsg.toString());

        }
    }

    private void handleMsg(Context context, String msg) throws UnsupportedEncodingException {
        if (dbManager == null) {
            MyApplication myApplication = (MyApplication) context.getApplicationContext();
            dbManager = myApplication.getDbManager();
        }
        JSONObject jsonObject = null;
        String tmp;
        String decryptedMsg = null;
        RecordItem recordItem;
        try {
            decryptedMsg = Tools.decrypt(context, msg);
            NotifyUtils.logE("解析后的推送信息：" + decryptedMsg);
            jsonObject = new JSONObject(decryptedMsg);
            switch (jsonObject.getInt(Constants.TYPE_ID)) {
                case Constants.BINDED_OK:
                    if (jsonObject.getString(Constants.MSG).equals("ok") && PrefUtils.getIdentify(context) != PrefUtils.IDENTIFY_HOST) {
                        NotifyUtils.showNotification(context, null, "绑定成功");

                        //发送广播到Activity通知绑定成功
                        Intent intent = new Intent();
                        intent.setAction(SlaveBindActivity.BORADCAST_ACTION_BINDED_OK);
                        context.sendBroadcast(intent);
                    }
                    break;
                case Constants.MISSED_CALL:
                    recordItem = new RecordItem(Constants.MISSED_CALL, jsonObject.getString(Constants.NAME), jsonObject.getString(Constants.PHONE_NUM), "", jsonObject.getLong(Constants.DATE));
//                    tmp = jsonObject.getString(Constants.NAME) + " "
//                            + jsonObject.getString(Constants.PHONE_NUM) + " "
//                            + Tools.getDateString(jsonObject.getLong(Constants.DATE));
                    NotifyUtils.showNotification(context, "来自 " + recordItem.getName() + recordItem.getNum() + " 的电话", "您的备用机有新的未接来电");
                    dbManager.insert(recordItem);
                    break;
                case Constants.MISSED_SMS:
                    recordItem = new RecordItem(Constants.MISSED_SMS, jsonObject.getString(Constants.NAME), jsonObject.getString(Constants.PHONE_NUM), jsonObject.getString(Constants.MSG), jsonObject.getLong(Constants.DATE));
//                    tmp = jsonObject.getString(Constants.NAME) + "\n" + jsonObject.getString(Constants.MSG) + "\n"
//                            + jsonObject.getString(Constants.PHONE_NUM) + "\n"
//                            + Tools.getDateString(jsonObject.getLong(Constants.DATE));
                    NotifyUtils.showNotification(context,"来自 " + recordItem.getName() + recordItem.getNum() + " " + recordItem.getMsg() ,"您的备用机有新的信息");
                    dbManager.insert(recordItem);
                    break;
                case Constants.NEW_VERSION:
                    // TODO: 2016/4/28
                    break;
                case Constants.NOTICE:
                    // TODO: 2016/4/28
                    break;
                case Constants.TEST:
                    NotifyUtils.showNotification(context, jsonObject.getString(Constants.MSG), "测试消息");

            }

        } catch (JSONException e) {
            e.printStackTrace();
            NotifyUtils.logE(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
