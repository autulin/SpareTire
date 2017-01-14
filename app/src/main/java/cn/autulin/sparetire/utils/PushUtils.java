package cn.autulin.sparetire.utils;

import android.content.Context;


import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.yunba.android.manager.YunBaManager;

/**
 * Created by autulin on 2016/4/26.
 */
public class PushUtils {

    public static int TO_HOST = 0x01;
    public static int TO_SLAVE = 0x02;

    public static String genHostAlias(String topic) {
//        return id.replaceAll("\\-","") + "_host";
        return topic + "_host";
    }

    public static String getHostAlias(final Context context) {
        return PrefUtils.getTopic(context) + "_host";
    }

    public static String genSlaveAlias(String topic) {
//        return getDeviceID(context).replaceAll("\\-","") + "_slave";
        return topic + "_slave";
    }

    public static String getSlaveAlias(final Context context) {
//        return getDeviceID(context).replaceAll("\\-","") + "_slave";
        return PrefUtils.getTopic(context) + "_slave";
    }

    public static String genTopic(String id, String randNum) {
        return Tools.getHashCode(id + randNum);
    }

    public static void subscribe(final Context context, final String topic, final String alias) {
        YunBaManager.subscribe(context, topic, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                NotifyUtils.logE("已成功订阅" + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                if (throwable instanceof MqttException) {
                    MqttException ex = (MqttException) throwable;
                    String msg = "Subscribe failed with error code : " + ex.getReasonCode();
                    NotifyUtils.logE(msg);
                }
            }
        });

        YunBaManager.setAlias(context, alias, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                NotifyUtils.logE("设置Alias成功:" + alias);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                if (throwable instanceof MqttException) {
                    MqttException ex = (MqttException) throwable;
                    String msg = "setAlias failed with error code : " + ex.getReasonCode();
                    NotifyUtils.logE(msg);
                }
            }
        });
    }

    public static void pushSms(final Context context, String number, String person, String body, long date) {
        checkServiceStoped(context);
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TYPE_ID, Constants.MISSED_SMS);
        map.put(Constants.MSG, body);
        map.put(Constants.PHONE_NUM, number);
        map.put(Constants.NAME, person);
        map.put(Constants.DATE, date);
        JSONObject jsonObject = new JSONObject(map);
        String msg = jsonObject.toString();

        pushOut(context, msg, TO_HOST);
    }

    public static void pushMissedCall(final Context context, String name, String number, long date) {
        checkServiceStoped(context);
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TYPE_ID, Constants.MISSED_CALL);
        map.put(Constants.NAME, name);
        map.put(Constants.PHONE_NUM, number);
        map.put(Constants.DATE, date);
        JSONObject jsonObject = new JSONObject(map);
        String msg = jsonObject.toString();

        pushOut(context, msg, TO_HOST);
    }


    public static void publishBindMsg(final Context context) {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TYPE_ID, Constants.BINDED_OK);
        map.put(Constants.MSG, "ok");
        JSONObject jsonObject = new JSONObject(map);
        String msg = jsonObject.toString();
        pushOut(context, msg, TO_SLAVE);
    }

    public static void publishTest(final Context context, String msg, int to) {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TYPE_ID, Constants.TEST);
        map.put(Constants.MSG, msg);
        JSONObject jsonObject = new JSONObject(map);
        String str = jsonObject.toString();
        pushOut(context, str, to);
    }

    public static void pushOut(final Context context, String msg, int to) {
        String encryptMsg = null;
        String destenation = to == TO_HOST ? getHostAlias(context) : getSlaveAlias(context);

        try {
            encryptMsg = Tools.encrypt(context, msg);
            NotifyUtils.logE("将发送：" + encryptMsg);

            YunBaManager.publishToAlias(context, destenation, encryptMsg, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    NotifyUtils.logE("推送成功");
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    if (throwable instanceof MqttException) {
                        MqttException ex = (MqttException) throwable;
                        String msg = "getState failed with error code : " + ex.getReasonCode();
                        NotifyUtils.logE("推送失败:" + msg);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void checkServiceStoped(Context context) {
        if (YunBaManager.isStopped(context)) {
            YunBaManager.resume(context);
        }
    }

}
