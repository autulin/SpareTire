package cn.autulin.sparetire.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.autulin.sparetire.db.DBManager;
import cn.autulin.sparetire.db.RecordItem;
import cn.autulin.sparetire.R;
import cn.autulin.sparetire.utils.Constants;
import cn.autulin.sparetire.utils.NotifyUtils;
import cn.autulin.sparetire.utils.PrefUtils;
import cn.autulin.sparetire.utils.PushUtils;
import cn.autulin.sparetire.utils.Tools;
import io.yunba.android.manager.YunBaManager;


public class MainActivity extends AppCompatActivity implements DBManager.DataBaseChangeListener {
    private int currentRole;
    private RecyclerView recyclerView;

    private TextView localStatus, remoteStatus;
    private LinearLayout statusll;
    private ContentAdapter contentAdapter;

    private DBManager dbManager;
    private List<RecordItem> list;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_main);
        currentRole = PrefUtils.getIdentify(this);
        switch (currentRole) {
            case PrefUtils.IDENTIFY_HOST:
//                initStatusView();
                initHostView();
                break;
            case PrefUtils.IDENTIFY_SLAVE:
                initStatusView();
                break;
            default:
                startActivity(new Intent(MainActivity.this, ChooseRoleActivity.class));
                finish();
                NotifyUtils.showToast(this, "未识别任何角色");
        }


    }

    private void initStatusView() {
        statusll = (LinearLayout) findViewById(R.id.status_ll);
        statusll.setVisibility(View.VISIBLE);
        localStatus = (TextView) findViewById(R.id.status_local);
        remoteStatus = (TextView) findViewById(R.id.status_remote);
        if (!YunBaManager.isStopped(this)) {
            localStatus.setText("本机服务状态：OK");
        } else {
            localStatus.setText("本机服务状态：已停止（点击恢复）");
        }
        localStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YunBaManager.resume(getApplicationContext());
                localStatus.setText("本机服务状态：OK");
            }
        });

        final String alias = currentRole == PrefUtils.IDENTIFY_HOST ? PushUtils.getSlaveAlias(this) : PushUtils.getHostAlias(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                YunBaManager.getState(MainActivity.this, alias, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        JSONObject result = iMqttToken.getResult();
                        try {
                            final String status = result.getString("status");
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    remoteStatus.setText("远端状态：" + status);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        NotifyUtils.showToast(MainActivity.this, "获取远端状态失败");
                    }
                });
            }
        }).start();

    }

    private void initHostView() {
        MyApplication myApplication = (MyApplication) getApplication();
        dbManager = myApplication.getDbManager();


        recyclerView = (RecyclerView) findViewById(R.id.record_list);
        dbManager.setOnDataBaseChange(this);
        list = dbManager.getRecordList();

        contentAdapter = new ContentAdapter(this,list);
        recyclerView.setAdapter(contentAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    public void getSomeAction(final int position) {
        new AlertDialog.Builder(this).setTitle("选择行动").setItems(new String[]{"呼叫", "短信"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + list.get(position).getNum()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + list.get(position).getNum()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                }
            }
        }).setNegativeButton("取消", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, AboutActivity.class));
//                NotifyUtils.showToast(this, "暂时没有");
                break;
            case R.id.action_test:
                if (PrefUtils.getIdentify(this) == PrefUtils.IDENTIFY_SLAVE) {
                    PushUtils.publishTest(this, "this is a test", PushUtils.TO_HOST);
                } else
                    PushUtils.publishTest(this, "this is a test", PushUtils.TO_SLAVE);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataBaseChanged(List<RecordItem> list) {
        if (contentAdapter != null) {
            NotifyUtils.logE("onDataBaseChanged");
            this.list.clear();
            this.list.addAll(list);
            contentAdapter.notifyDataSetChanged();
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView phoneNum, msgTV;
        CardView cardView;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.listitem_record, parent, false));
            phoneNum = (TextView) itemView.findViewById(R.id.phone_num);
            msgTV = (TextView) itemView.findViewById(R.id.msg_text);
            cardView = (CardView) itemView.findViewById(R.id.car_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSomeAction(getAdapterPosition());
                }
            });
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<RecordItem> list;
        private Context context;

        public ContentAdapter(Context context, List<RecordItem> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final RecordItem recordItem = list.get(position);
            holder.phoneNum.setText(recordItem.getNum() + " " + recordItem.getName() + " " + Tools.getTimeStringFromNow(recordItem.getDate()));

            if (recordItem.getTypeID() == Constants.MISSED_CALL) {
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.missedCall));
                holder.msgTV.setText("未接来电");
            } else {
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.sms));
                holder.msgTV.setText(recordItem.getMsg());
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
