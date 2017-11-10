package com.example.huang.check_in;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.device.ScanDevice;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 1.会员卡读取
 * 2.二维码扫描
 */
public class Scan2Activity extends AppCompatActivity {
    private final String TAG = Scan2Activity.class.getSimpleName();
    @BindView(R.id.scan_result)
    TextView mScanResult;
    @BindView(R.id.scan_tv_count)
    TextView mScanTvCount;
    @BindView(R.id.scan2_tb)
    Toolbar mScan2Tb;
    private VIPDaoUtils mDaoUtils;//数据库操作工具类
    private ScanDevice mScanDevice;
    private AudienceDaoUtils audienceDaoUtils;
    private Audience mAudience;
    private boolean stopFlag = false;
    private SharedPreferences mTicket;
    private int mJ = 0;//记录纸质票数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan2);
        ButterKnife.bind(this);
        //初始化数据库操作工具类
        mDaoUtils = new VIPDaoUtils(this);
        audienceDaoUtils = new AudienceDaoUtils(this);
        //弹窗提醒导入门票信息
        dialog();

        //2.1获取二维码扫描设备
        mScanDevice = ((MyApplication) this.getApplication()).getScanDevice();

        //SP存放计票数信息
        mTicket = getSharedPreferences("ticket", MODE_PRIVATE);
        mJ = Integer.parseInt(mTicket.getString("Aud", "0"));
        mScanTvCount.setText("单场门票计数：" + mJ);
        updateCount();

        setSupportActionBar(mScan2Tb);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("单场票检票");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("请确认您是否已经导入门票信息");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        assert window != null;
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.dialog_style);
        dialog.setCancelable(false);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        TextView message = (TextView) dialog.findViewById(android.R.id.message);
        assert message != null;
        message.setTextSize(26);

    }

    //更新数据库中的普通门票观众的信息
    private void upDateAudience(int count) {
        Audience audience = new Audience();
        audience.set_id(mAudience.get_id());
        audience.setSeat(mAudience.getSeat());
        audience.setCount(count);
        audienceDaoUtils.updateMeizi(audience);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mScanDevice != null) {
            mScanDevice.openScan();
            mScanDevice.setOutScanMode(0);//接收广播
        }
        //2.2 注册receiver
        IntentFilter filter = new IntentFilter();
        String SCAN_ACTION = "scan.rcv.message";
        filter.addAction(SCAN_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScanDevice != null) {
            mScanDevice.stopScan();
            mScanDevice.closeScan();
            mScanDevice = null;
        }
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] barocode = intent.getByteArrayExtra("barocode");
            String id = new String(barocode);
            Log.i(TAG, "onReceive: " + id);
            mAudience = audienceDaoUtils.queryMeiziById(id);
            if (mAudience != null) {
                mScanResult.setText("检票通过！");
                mJ++;//更新计票数
                updateCount();
                upDateAudience(mAudience.getCount()+1);
            } else {
                mScanResult.setText("无效门票！");
            }
            mScanDevice.stopScan();
            stopFlag = true;
        }
    };

    //关闭所有连接
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDaoUtils.close();

        //存储计票数
        SharedPreferences.Editor mEditor = mTicket.edit();
        mEditor.putString("Aud", "" + mJ);
        mEditor.apply();
    }

    //更新计票数
    private void updateCount() {
        mScanTvCount.setText("单场票计数：" + mJ);
    }

    @OnClick(R.id.scan)
    public void onViewClicked() {//扫描二维码
        if (!stopFlag) {
            mScanDevice.stopScan();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mScanDevice.startScan();
        stopFlag = false;
    }
}
