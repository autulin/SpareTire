package cn.autulin.sparetire.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.autulin.sparetire.R;
import cn.autulin.sparetire.utils.NotifyUtils;

public class ChooseRoleActivity extends AppCompatActivity {

    Button hostBindBtn,slaveBindBtn;

    public static final int CAMERA_PERMISSION_REQUEST = 0x01;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        hostBindBtn = (Button) findViewById(R.id.host_bind_btn);
        slaveBindBtn = (Button) findViewById(R.id.slave_bind_btn);

        hostBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();//需要摄像头权限
            }
        });
        slaveBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChooseRoleActivity.this, SlaveBindActivity.class));
                finish();
            }
        });

    }

    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode,     String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//            finish();

                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }
        } else {
            startActivity(new Intent(ChooseRoleActivity.this, HostBindActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NotifyUtils.showToast(this, "成功获取权限");

                startActivity(new Intent(ChooseRoleActivity.this, HostBindActivity.class));
                finish();
            } else {
                NotifyUtils.showToast(this, "获取相机权限失败");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
