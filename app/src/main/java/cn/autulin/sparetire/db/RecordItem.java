package cn.autulin.sparetire.db;


/**
 * Created by autulin on 2016/5/4.
 */
public class RecordItem {
    private int typeID;
    private String name, num, msg;
    private long date;

    public RecordItem(int typeID, String name, String num, String msg, long date) {
        this.typeID = typeID;
        this.name = name;
        this.num = num;
        this.msg = msg;
        this.date = date;
    }


    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getName() {
        if (name.equals("null")) {
            return "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return typeID + name + num + msg + date;
    }
}
