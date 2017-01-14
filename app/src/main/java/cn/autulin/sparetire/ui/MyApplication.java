package cn.autulin.sparetire.ui;

import android.app.Application;

import cn.autulin.sparetire.db.DBManager;
import io.yunba.android.manager.YunBaManager;


/**
 * Created by autulin on 2016/4/23.
 */
public class MyApplication extends Application {

    private DBManager dbManager;
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化推送SDK
        YunBaManager.start(this);



    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public DBManager getDbManager() {
        if(dbManager != null){
            return dbManager;
        } else {
            return dbManager = new DBManager(this);
        }

    }
}
