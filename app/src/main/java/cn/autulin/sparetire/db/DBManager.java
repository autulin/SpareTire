package cn.autulin.sparetire.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.autulin.sparetire.utils.Constants;

/**
 * Created by autulin on 2016/5/4.
 */
public class DBManager {
    private MyDBHelper myDBHelper;
    private DataBaseChangeListener dataBaseChangeListener;
    private Context context;

    public DBManager(Context context) {
        this.context = context;
        myDBHelper = new MyDBHelper(context, "records.db", null, 3);
    }

    public void setOnDataBaseChange(DataBaseChangeListener dataBaseChange){
        this.dataBaseChangeListener = dataBaseChange;
    }

    public List<RecordItem> getRecordList() {
        List<RecordItem> list = new ArrayList<>();
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from Records order by date desc", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new RecordItem(
                        cursor.getInt(cursor.getColumnIndex(Constants.TYPE_ID)),
                        cursor.getString(cursor.getColumnIndex(Constants.NAME)),
                        cursor.getString(cursor.getColumnIndex(Constants.PHONE_NUM)),
                        cursor.getString(cursor.getColumnIndex(Constants.MSG)),
                        cursor.getLong(cursor.getColumnIndex(Constants.DATE))
                                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void insert(RecordItem recordItem){
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.execSQL("insert into Records (type_id, name, num, msg, date) values (?, ?, ? ,? ,?)",
                new String[]{String.valueOf(recordItem.getTypeID()),
                recordItem.getName(),
                recordItem.getNum(),
                recordItem.getMsg(),
                        String.valueOf(recordItem.getDate())});
        if (dataBaseChangeListener != null) {
            dataBaseChangeListener.onDataBaseChanged(getRecordList());
        }
    }


    public interface DataBaseChangeListener {
        void onDataBaseChanged(List<RecordItem> list);
    }
}
