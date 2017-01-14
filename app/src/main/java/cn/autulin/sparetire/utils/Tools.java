package cn.autulin.sparetire.utils;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.scottyab.aescrypt.AESCrypt;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Created by autulin on 2016/4/26.
 */
public class Tools {

    public synchronized static String getLocalDeviceID(final Context context) {

        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        UUID uuid;

        try {
            if (!"9774d56d682e549c".equals(androidId)) {
                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            } else {
                final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String result = uuid.toString();
//        sp.edit().putString(PREFS_DEVICE_ID, result).commit();
        return result;
    }

    public static String getDateString(long src) {
        Date date = new Date(src);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }



    private static final int YEAR = 365 * 24 * 60 * 60;// 年
    private static final int MONTH = 30 * 24 * 60 * 60;// 月
    private static final int DAY = 24 * 60 * 60;// 天
    private static final int HOUR = 60 * 60;// 小时
    private static final int MINUTE = 60;// 分钟
    /**
     * 根据时间戳获取描述性时间，如3分钟前，1天前
     *
     * @param timestamp
     *            时间戳 单位为毫秒
     * @return 时间字符串
     */
    public static String getTimeStringFromNow(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeGap = (currentTime - timestamp) / 1000;// 与现在时间相差秒数
        String timeStr = null;
        if (timeGap > DAY) {// 1天以上
            timeStr = getDateString(timestamp);
        } else if (timeGap > HOUR) {// 1小时-24小时
            timeStr = timeGap / HOUR + "小时前";
        } else if (timeGap > MINUTE) {// 1分钟-59分钟
            timeStr = timeGap / MINUTE + "分钟前";
        } else {// 1秒钟-59秒钟
            timeStr = "刚刚";
        }
        return timeStr;
    }

    public static String getHashCode(String src) {
        String result = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(src.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String generateRandomNum(int length) {
        Random random = new Random();
        double pross = (1 + random.nextDouble()) * Math.pow(10, length);
        String fixLenthString = String.valueOf(pross);
        return fixLenthString.substring(1, length + 1);
    }

    public static String decrypt(final Context context, String str) throws Exception {
//        return decrypt(PrefUtils.getPassword(context), str);
//        Encryption encryption = new Encryption(Encryption.getDefaultCipher() ,"2134123412341234");
        String passwd = PrefUtils.getPassword(context);
        NotifyUtils.logE("解密，获取的密码是："+passwd+"，文本是"+str);
        return AESCrypt.decrypt(passwd, str);
    }

    public static String encrypt(final Context context, String str) throws Exception {
//        return encrypt(PrefUtils.getPassword(context), str);
//        Encryption encryption = new Encryption(Encryption.getDefaultCipher() ,"2134123412341234");
        String passwd = PrefUtils.getPassword(context);
        NotifyUtils.logE("加密，获取的密码是："+passwd+"，文本是"+str);
        return AESCrypt.encrypt(passwd, str);
    }

}
