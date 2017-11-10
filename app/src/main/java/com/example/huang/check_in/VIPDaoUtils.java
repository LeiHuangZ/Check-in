package com.example.huang.check_in;

import android.content.Context;
import android.util.Log;

import com.ping.greendao.gen.VIPDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 用于完成对MeiZhi数据表的具体操作——ORM操作
 * Created by huang on 2017/10/18.
 */

public class VIPDaoUtils {
    private final String TAG = VIPDaoUtils.class.getSimpleName();
    private DaoManager mManager;

    public VIPDaoUtils(Context context){
        mManager = DaoManager.getInstance();
        mManager.init(context);
    }

    /**
     * 完成meizi记录的插入，如果表未创建，先创建Meizi表
     * @param meizhi 1
     * @return flag
     */
    public boolean insertMeiZhi(VIP meizhi){
        boolean flag = false;
        flag = mManager.getDaoSession().getVIPDao().insert(meizhi) == -1 ? false : true;
        Log.i(TAG, "insertMeiZhi: "+flag+"-->"+meizhi.toString());
        return flag;
    }

    /**
     * 插入多条数据，在子线程操作
     * @param meizhiList
     * @return
     */
    public boolean insertMultMeizi(final List<VIP> meizhiList) {
        boolean flag = false;
        try {
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (VIP meizhi : meizhiList) {
                        mManager.getDaoSession().insertOrReplace(meizhi);
                    }
                }
            });
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改一条数据
     * @param meizi
     * @return
     */
    public boolean updateMeizi(VIP meizi){
        boolean flag = false;
        try {
            mManager.getDaoSession().update(meizi);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除单条记录
     * @param meizi
     * @return
     */
    public boolean deleteMeizi(VIP meizi){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().delete(meizi);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除所有记录
     * @return
     */
    public boolean deleteAll(){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().deleteAll(VIP.class);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查询所有记录
     * @return
     */
    public List<VIP> queryAllMeizi(){
        return mManager.getDaoSession().loadAll(VIP.class);
    }

    /**
     * 根据主键id查询记录
     * @param key
     * @return
     */
    public VIP queryMeiziById(String key){
        return mManager.getDaoSession().load(VIP.class, key);
    }

    /**
     * 使用native sql进行查询操作
     */
    public List<VIP> queryMeiziByNativeSql(String sql, String[] conditions){
        return mManager.getDaoSession().queryRaw(VIP.class, sql, conditions);
    }

    /**
     * 使用queryBuilder进行查询
     * @return
     */
    public List<VIP> queryMeiziByQueryBuilder(long id){
        QueryBuilder<VIP> queryBuilder = mManager.getDaoSession().queryBuilder(VIP.class);
        return queryBuilder.where(VIPDao.Properties._id.eq(id)).list();
    }

    public void close(){
        mManager.closeConnection();
    }
}
