package cn.autulin.sparetire.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by autulin on 2016/5/4.
 */
public class MyDBHelper extends SQLiteOpenHelper {
    private Context context;
    public static final String CREATE_DB = "create table Records (" +
            "id integer primary key autoincrement," +
            "type_id integer," +
            "name text," +
            "num text," +
            "msg text," +
            "date integer)";
    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Records");
        onCreate(db);
    }
}
