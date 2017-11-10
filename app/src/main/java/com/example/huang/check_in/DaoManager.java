package com.example.huang.check_in;

import android.content.Context;

import com.ping.greendao.gen.DaoMaster;
import com.ping.greendao.gen.DaoSession;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * 创建数据库、创建数据库表、包含增删改查的操作以及数据库的升级
 * Created by huang on 2017/10/18.
 */

public class DaoManager {
    private static final String DB_NAME = "greendaotest";

    private Context context;

    //多线程中要共享的变量用volatile关键字修饰
    private volatile static DaoManager manager = new DaoManager();
    private static DaoMaster sDaoMaster;
    private static DaoMaster.DevOpenHelper sHelper;
    private static DaoSession sDaoSession;
    //单利模式获取操作数据库对象
    public static DaoManager getInstance(){
        return manager;
    }
    public void init(Context context){
        this.context = context;
    }

    /**
     * 判断是否有存在数据库，如果没有则创建
     * @return sDaoMaster
     */
    public DaoMaster getDaoMaster(){
        if (sDaoMaster == null){
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context,DB_NAME,null);
            sDaoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return sDaoMaster;
    }

    /**
     * 完成对数据库的添加、删除、修改、查询操作，仅仅是一个接口
     * @return sDaoSession
     */
    public DaoSession getDaoSession(){
        if (sDaoSession == null){
            if (sDaoMaster == null){
                sDaoMaster = getDaoMaster();
            }
            sDaoSession = sDaoMaster.newSession();
        }
        return sDaoSession;
    }

    /**
     * 打开输出日志，默认为关闭
     */
    public void setDebug(){
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    /**
     * 关闭所有的操作，数据开启后，使用完毕要关闭
     */
    public void closeConnection(){
        closeHelper();
        closeDaoSession();
    }

    private void closeHelper() {
        if (sHelper != null){
            sHelper.close();
            sHelper = null;
        }
    }

    private void closeDaoSession() {
        if (sDaoSession != null){
            sDaoSession.clear();
            sDaoSession = null;
        }
    }
}
