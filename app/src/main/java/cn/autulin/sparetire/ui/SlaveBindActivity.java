package cn.autulin.sparetire.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.autulin.sparetire.R;
import cn.autulin.sparetire.utils.NotifyUtils;
import cn.autulin.sparetire.utils.PrefUtils;
import cn.autulin.sparetire.utils.PushUtils;
import cn.autulin.sparetire.utils.Tools;
import cn.bingoogolapple.qrcode.core.DisplayUtils;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;

public class SlaveBindActivity extends AppCompatActivity {

    ImageView qrcodeIV;

    public final static String BORADCAST_ACTION_BINDED_OK = "cn.autulin.bindedok";

    public static final int PHONE_PERMISSION_REQUEST = 0x02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave_bind);
        qrcodeIV = (ImageView) findViewById(R.id.qrcode);


        String id = Tools.getLocalDeviceID(this);
        PrefUtils.setID(this, id);
        String rand = PrefUtils.getRand(this);
        if (rand == null) {
            rand = Tools.generateRandomNum(6);
            PrefUtils.storeRand(this, rand);
        }

        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("rand", rand);
        JSONObject jsonObject = new JSONObject(map);
        String src = jsonObject.toString();


        QRCodeEncoder.encodeQRCode(src, DisplayUtils.dp2px(SlaveBindActivity.this, 600), Color.parseColor("#000000"), new QRCodeEncoder.Delegate() {
            @Override
            public void onEncodeQRCodeSuccess(Bitmap bitmap) {
                qrcodeIV.setImageBitmap(bitmap);
            }

            @Override
            public void onEncodeQRCodeFailure() {
                NotifyUtils.showToast(SlaveBindActivity.this, "生成英文二维码失败");
            }
        });

        String topic = PushUtils.genTopic(id, rand);
        PrefUtils.storeTopic(this, topic);
        PushUtils.subscribe(this, topic, PushUtils.genSlaveAlias(topic));
//        PrefUtils.setIdentify(this, PrefUtils.IDENTIFY_SLAVE);


        IntentFilter filter = new IntentFilter();
        filter.addAction(BORADCAST_ACTION_BINDED_OK);
        registerReceiver(broadcastReceiver, filter); //动态注册监听，接收绑定成功后PushMsgReceiver发来的消息
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BORADCAST_ACTION_BINDED_OK)) {
                checkPermission();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver); //取消监听
    }

    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode,     String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//            finish();

                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, PHONE_PERMISSION_REQUEST);
//            }
        } else {
            PrefUtils.setIdentify(this, PrefUtils.IDENTIFY_SLAVE);
            startActivity(new Intent(SlaveBindActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PHONE_PERMISSION_REQUEST) {
            if (grantResults.length == permissions.length) {
                NotifyUtils.showToast(this, "成功获取权限");

                PrefUtils.setIdentify(this, PrefUtils.IDENTIFY_SLAVE);
                startActivity(new Intent(SlaveBindActivity.this, MainActivity.class));
                finish();
            } else {
                NotifyUtils.showToast(this, "获取权限失败");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
