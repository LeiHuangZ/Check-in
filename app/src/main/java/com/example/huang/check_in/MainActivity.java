package com.example.huang.check_in;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private VIPDaoUtils mVIPDaoUtils;
    private AudienceDaoUtils mAudienceDaoUtils;
    private SharedPreferences mEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mVIPDaoUtils = new VIPDaoUtils(this);
        mAudienceDaoUtils = new AudienceDaoUtils(this);
        mEditor = getSharedPreferences("ticket", MODE_PRIVATE);
    }

    @OnClick({R.id.tv_import_vip, R.id.tv_scan, R.id.tv_output, R.id.clear,R.id.tv_scan2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_import_vip://导入会员信息
                mVIPDaoUtils.deleteAll();//删除上一次导入的会员信息
                mAudienceDaoUtils.deleteAll();
                mEditor.edit().clear().apply();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            case R.id.tv_scan://开启检票页面
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
                break;
            case R.id.tv_scan2://开启检票页面
                startActivity(new Intent(MainActivity.this, Scan2Activity.class));
                break;
            case R.id.tv_output://导出检票信息
                //首先删除已经存在的文件
                boolean delete = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Tickets .xls").delete();
                if (delete) {
                    Log.e(TAG, "delete: success");
                }
                List<VIP> VIPList = mVIPDaoUtils.queryAllMeizi();
                List<Audience> audienceList = mAudienceDaoUtils.queryAllMeizi();
                if (VIPList.size() == 0 || audienceList.size() == 0) {
                    Toast.makeText(this, "没有检票信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean excel = new Utils().toExcel(this,VIPList, audienceList);
                if (excel) {
                    Toast.makeText(this, "文件已导出至" + Environment.getExternalStorageDirectory().getAbsolutePath() + "目录下", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "文件导出失败，请检查SD卡，重试", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.clear://清楚所有导入和导出的数据
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("此操作将清除所有门票信息（包括检票信息），确认清除？");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        boolean b = mVIPDaoUtils.deleteAll();
                        mAudienceDaoUtils.deleteAll();
                        boolean delete = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Tickets .xls").delete();
                        mEditor.edit().clear().apply();
                        getSharedPreferences("ticket", MODE_PRIVATE).edit().clear().apply();
                        if (delete || b) {
                            Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Uri uri = data.getData();
                    String path = new Utils().getDataColumn(this, uri, null, null);
                    File file = new File(path);
                    try {
                        Workbook workbook = Workbook.getWorkbook(file);
                        Sheet sheet = workbook.getSheet(0);
                        int rows = sheet.getRows();
                        int columns = sheet.getColumns();
                        Log.e(TAG, "onActivityResult: " + rows + "=========" + columns);
                        for (int i = 1; i < rows; i++) {
                            VIP mVIP = new VIP();
                            for (int j = 0; j < columns; j++) {
                                Cell cell = sheet.getCell(j, i);
                                String result = cell.getContents();
                                Log.e(TAG, "result: " + result);
                                if (result.equals("")) break;
                                switch (j) {
                                    case 0:
                                        mVIP.set_id(result);
                                        break;
                                    case 1:
                                        mVIP.setCard_num(result);
                                        break;
                                    case 2:
                                        mVIP.setName(result);
                                        break;
                                    case 3:
                                        mVIP.setPhone_num(result);
                                    case 4:
                                        mVIP.setIdentify(result);
                                    case 5:
                                        mVIP.setSeat(result);
                                }
                            }
                            if (mVIP.get_id() == null) break;
                            mVIPDaoUtils.insertMeiZhi(mVIP);
                        }
                        Sheet s2 = workbook.getSheet(1);
                        int rows2 = s2.getRows();
                        int columns2 = s2.getColumns();
                        for (int i = 1; i < rows2; i++) {
                            Audience audience = new Audience();
                            for (int j = 0; j < columns2; j++) {
                                Cell cell = s2.getCell(j, i);
                                String result = cell.getContents();
                                Log.e(TAG, "result: " + result);
                                if (result.equals("")) break;
                                switch (j) {
                                    case 0:
                                        audience.set_id(result);
                                        break;
                                    case 1:
                                        audience.setSeat(result);
                                        break;
                                }
                            }
                            if (audience.get_id() == null) break;
                            mAudienceDaoUtils.insertMeiZhi(audience);
                        }
                        workbook.close();
                        Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show();
                    } catch (BiffException | IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }

        }
    }
}
