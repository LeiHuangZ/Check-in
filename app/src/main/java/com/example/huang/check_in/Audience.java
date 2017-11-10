package com.example.huang.check_in;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 普通观众的信息实体类
 * Created by huang on 2017/10/20.
 */
@Entity
public class Audience {
    @Id
    private String _id;
    @NotNull
    private String seat;
    private int count;
    @Generated(hash = 1720553987)
    public Audience(String _id, @NotNull String seat, int count) {
        this._id = _id;
        this.seat = seat;
        this.count = count;
    }
    @Generated(hash = 1927543667)
    public Audience() {
    }
    public String get_id() {
        return this._id;
    }
    public void set_id(String _id) {
        this._id = _id;
    }
    public String getSeat() {
        return this.seat;
    }
    public void setSeat(String seat) {
        this.seat = seat;
    }
    public int getCount() {
        return this.count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    
}
