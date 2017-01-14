package cn.autulin.sparetire.ui;

import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import cn.autulin.sparetire.R;
import cn.autulin.sparetire.utils.NotifyUtils;
import cn.autulin.sparetire.utils.PrefUtils;
import cn.autulin.sparetire.utils.PushUtils;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class HostBindActivity extends AppCompatActivity implements QRCodeView.Delegate {

    private QRCodeView mQRCodeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_host_bind);
        mQRCodeView = (ZXingView) findViewById(R.id.zxingview);
        mQRCodeView.setResultHandler(this);
        mQRCodeView.startSpot();

    }


    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        NotifyUtils.logE(result);
        vibrate();
        String id = null;
        String rand = null;
        String topic = null;
        try {
            JSONObject object = new JSONObject(result);
            id = object.getString("id");
            rand = object.getString("rand");
        } catch (JSONException e) {
            e.printStackTrace();
            NotifyUtils.showToast(HostBindActivity.this, "Json解析错误");
        }
        if (id != null && rand != null) {
            PrefUtils.setID(this, id);
            PrefUtils.storeRand(this, rand);
            topic = PushUtils.genTopic(id, rand);
        }

        if (topic != null) {
            PushUtils.subscribe(this, topic, PushUtils.genHostAlias(topic));

            PrefUtils.setIdentify(this, PrefUtils.IDENTIFY_HOST);
            PrefUtils.storeTopic(this, topic);

            //publish a message mark binded
            PushUtils.publishBindMsg(this);

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }


    @Override
    public void onScanQRCodeOpenCameraError() {
        NotifyUtils.showToast(this, "打开相机出错");
    }
}
