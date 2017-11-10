package com.example.huang.check_in;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
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
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 1.会员卡读取
 */
public class ScanActivity extends AppCompatActivity {
    private final String TAG = ScanActivity.class.getSimpleName();
    @BindView(R.id.scan_result)
    TextView mScanResult;
    @BindView(R.id.scan_tv_count)
    TextView mScanTvCount;
    @BindView(R.id.scan_tb)
    Toolbar mScanTb;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mMFilters;
    private String[][] mTechLists;
    private VIPDaoUtils mDaoUtils;//数据库操作工具类
    private String UID;
    private VIP mVIP;
    private SharedPreferences mTicket;

    private int mI = 0;//记录VIP年卡数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);
        //初始化数据库操作工具类
        mDaoUtils = new VIPDaoUtils(this);
        //弹窗提醒导入门票信息
        dialog();

        initNFC();//初始化NFC

        //SP存放计票数信息
        mTicket = getSharedPreferences("ticket", MODE_PRIVATE);
        mI = Integer.parseInt(mTicket.getString("VIP", "0"));
        updateCount();

        setSupportActionBar(mScanTb);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle("年票检票");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
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

    private void initNFC() {
        //获取默认的NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "您的设备不支持NFC", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "请先在系统设置开启NFC", Toast.LENGTH_SHORT).show();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
        IntentFilter nDef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            nDef.addDataType("*/*");
            mMFilters = new IntentFilter[]{nDef,
                    new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                    new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),};

            mTechLists = new String[][]{new String[]{NfcF.class.getName()},
                    new String[]{MifareClassic.class.getName()},};
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(TAG, "initNFC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] id = tagFromIntent.getId();
        //get UID
        UID = Tools.Bytes2HexString(id, id.length);
        //query SQLite
        mVIP = mDaoUtils.queryMeiziById(UID);
        if (mVIP == null) {
            mScanResult.setText(R.string.not_found);
        } else {
            if (mVIP.getIsCheck()) {
                mScanResult.setText(R.string.again);
                upDateMeiZhi(true);
            } else {
                mScanResult.setText(R.string.welcome);
                mScanResult.append("\n会员号：");
                mScanResult.append(mVIP.getCard_num());
                mScanResult.append("\n姓名：");
                mScanResult.append(mVIP.getName());
                mScanResult.append("\n电话：");
                mScanResult.append(mVIP.getPhone_num());
                mScanResult.append("\n座位号：");
                mScanResult.append(mVIP.getSeat());
                mScanResult.append("\n身份证号：");
                mScanResult.append(mVIP.getIdentify());
                mI++;//更新计票数
                updateCount();
                upDateMeiZhi(false);
            }
        }
    }

    private void upDateMeiZhi(boolean again) {
        VIP mei = new VIP();
        mei.set_id(UID);
        mei.setName(mVIP.getName());
        mei.setIdentify(mVIP.getIdentify());
        mei.setSeat(mVIP.getIdentify());
        mei.setIsCheck(true);
        mei.setAgain(again);
        mDaoUtils.updateMeizi(mei);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mMFilters,
                    mTechLists);
        }
    }

    //关闭所有连接
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDaoUtils.close();

        //存储计票数
        SharedPreferences.Editor mEditor = mTicket.edit();
        mEditor.putString("VIP", "" + mI);
        mEditor.apply();
    }

    //更新计票数
    private void updateCount() {
        mScanTvCount.setText( "年票计数：" + mI);
    }
}
