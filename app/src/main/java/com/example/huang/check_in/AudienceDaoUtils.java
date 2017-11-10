package com.example.huang.check_in;

import android.content.Context;
import android.util.Log;

import com.ping.greendao.gen.AudienceDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 用于完成对Audience数据表的具体操作——ORM操作
 * Created by huang on 2017/10/18.
 */

public class AudienceDaoUtils {
    private final String TAG = AudienceDaoUtils.class.getSimpleName();
    private DaoManager mManager;

    public AudienceDaoUtils(Context context) {
        mManager = DaoManager.getInstance();
        mManager.init(context);
    }

    /**
     * 完成meizi记录的插入，如果表未创建，先创建Meizi表
     *
     * @param meizhi 1
     * @return flag
     */
    public boolean insertMeiZhi(Audience meizhi) {
        boolean flag = false;
        flag = mManager.getDaoSession().getAudienceDao().insert(meizhi) == -1 ? false : true;
        Log.i(TAG, "insertMeiZhi: " + flag + "-->" + meizhi.toString());
        return flag;
    }

    /**
     * 插入多条数据，在子线程操作
     *
     * @param meizhiList
     * @return
     */
    public boolean insertMultMeizi(final List<Audience> meizhiList) {
        boolean flag = false;
        try {
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (Audience meizhi : meizhiList) {
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
     *
     * @param meizi
     * @return
     */
    public boolean updateMeizi(Audience meizi) {
        boolean flag = false;
        try {
            mManager.getDaoSession().update(meizi);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除单条记录
     *
     * @param meizi
     * @return
     */
    public boolean deleteMeizi(Audience meizi) {
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().delete(meizi);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除所有记录
     *
     * @return
     */
    public boolean deleteAll() {
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().deleteAll(Audience.class);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查询所有记录
     *
     * @return
     */
    public List<Audience> queryAllMeizi() {
        return mManager.getDaoSession().loadAll(Audience.class);
    }

    /**
     * 根据主键id查询记录
     *
     * @param key
     * @return
     */
    public Audience queryMeiziById(String key) {
        return mManager.getDaoSession().load(Audience.class, key);
    }

    /**
     * 使用native sql进行查询操作
     */
    public List<Audience> queryMeiziByNativeSql(String sql, String[] conditions) {
        return mManager.getDaoSession().queryRaw(Audience.class, sql, conditions);
    }

    /**
     * 使用queryBuilder进行查询
     *
     * @return
     */
    public List<Audience> queryMeiziByQueryBuilder(long id) {
        QueryBuilder<Audience> queryBuilder = mManager.getDaoSession().queryBuilder(Audience.class);
        return queryBuilder.where(AudienceDao.Properties._id.eq(id)).list();
    }

    public void close() {
        mManager.closeConnection();
    }
}
