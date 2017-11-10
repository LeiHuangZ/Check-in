package com.example.huang.check_in;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static android.content.Context.MODE_PRIVATE;

/**
 * 工具类
 * Created by huang on 2017/10/18.
 */

public class Utils {
    /**
     * 根据uri获取当前路径
     */
    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";//路径保存在downloads表中的_data字段
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * 导出生成excel文件，存放于SD卡中
     *
     */
    public boolean toExcel(Context context,List<VIP> list, List<Audience> list2) {
        // 准备设置excel工作表的标题
        String[] title = {"会员号", "姓名", "身份证", "座位号", "进场", "重复刷卡"};
        try {
            //判断SD卡是否存在
            if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                return false;
            }
            // 创建Excel工作薄
            // 在SD卡中，新建立一个名为tickets的jxl文件
            WritableWorkbook wwb = Workbook.createWorkbook(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tickets .xls"));
            // 添加第一个工作表并设置第一个Sheet的名字
            WritableSheet sheet = wwb.createSheet("年票检票信息", 0);
            Label label;
            for (int i = 0; i < title.length; i++) {
                label = new Label(i, 0, title[i]);
                // 将定义好的单元格添加到工作表中
                sheet.addCell(label);
            }
            /*
             * 保存数字到单元格，需要使用jxl.write.Number 必须使用其完整路径，否则会出现错误
			 */
            for (int i = 0; i < list.size(); i++) {
                VIP mVIP = list.get(i);
                //添加会员号
                label = new Label(0, i + 1, mVIP.get_id());
                sheet.addCell(label);
                //添加姓名
                label = new Label(1, i + 1, mVIP.getName());
                sheet.addCell(label);
                //添加身份证号
                label = new Label(2, i + 1, mVIP.getIdentify());
                sheet.addCell(label);
                //添加座位号
                label = new Label(3, i + 1, mVIP.getIdentify());
                sheet.addCell(label);
                String b;
                //进场
                b = mVIP.getIsCheck() ? "是" : "否";
                label = new Label(4, i + 1, b);
                sheet.addCell(label);
                //重复检票
                b = mVIP.getAgain() ? "是" : "否";
                label = new Label(5, i + 1, b);
                sheet.addCell(label);
            }

            label = new Label(5, list.size()+1, "检票数总计：");
            sheet.addCell(label);

            label = new Label(6, list.size()+1, context.getSharedPreferences("ticket", MODE_PRIVATE).getString("VIP", "0"));
            sheet.addCell(label);

            // 添加第二个工作表并设置第二个Sheet的名字
            WritableSheet sheet2 = wwb.createSheet("普通票检票信息", 1);
            Label label2;
            String[] title2 = {"票号", "座位", "检票次数"};
            for (int i = 0; i < title2.length; i++) {
                label2 = new Label(i, 0, title2[i]);
                // 将定义好的单元格添加到工作表中
                sheet2.addCell(label2);
            }
            /*
             * 保存数字到单元格，需要使用jxl.write.Number 必须使用其完整路径，否则会出现错误
			 */
            for (int i = 0; i < list2.size(); i++) {
                Audience audience = list2.get(i);
                //添加票号
                label2 = new Label(0, i + 1, audience.get_id());
                sheet2.addCell(label2);
                //添加座位号
                label2 = new Label(1, i + 1, audience.getSeat());
                sheet2.addCell(label2);
                int b;
                //进场
                b = audience.getCount();
                label2 = new Label(2, i + 1, b+"");
                sheet2.addCell(label2);
            }

            label2 = new Label(3, list2.size()+1, "检票数总计：");
            sheet2.addCell(label2);
            label2 = new Label(4, list2.size()+1, context.getSharedPreferences("ticket", MODE_PRIVATE).getString("Aud", "0"));
            sheet2.addCell(label2);
            wwb.write(); //写入数据
            wwb.close(); //关闭文件

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
