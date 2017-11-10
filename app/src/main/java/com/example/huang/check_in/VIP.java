package com.example.huang.check_in;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * VIP客户实体类
 * Created by huang on 2017/10/18.
 */
@Entity
public class VIP {
    @Id
    private String _id;
    private String card_num;
    private String name;
    private String phone_num;
    @NotNull
    private String identify;
    private String seat;
    private boolean isCheck;
    private boolean again;//是否重复刷卡
    @Generated(hash = 1869359823)
    public VIP(String _id, String card_num, String name, String phone_num,
            @NotNull String identify, String seat, boolean isCheck, boolean again) {
        this._id = _id;
        this.card_num = card_num;
        this.name = name;
        this.phone_num = phone_num;
        this.identify = identify;
        this.seat = seat;
        this.isCheck = isCheck;
        this.again = again;
    }
    @Generated(hash = 2143396984)
    public VIP() {
    }
    public String get_id() {
        return this._id;
    }
    public void set_id(String _id) {
        this._id = _id;
    }
    public String getCard_num() {
        return this.card_num;
    }
    public void setCard_num(String card_num) {
        this.card_num = card_num;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhone_num() {
        return this.phone_num;
    }
    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }
    public String getIdentify() {
        return this.identify;
    }
    public void setIdentify(String identify) {
        this.identify = identify;
    }
    public String getSeat() {
        return this.seat;
    }
    public void setSeat(String seat) {
        this.seat = seat;
    }
    public boolean getIsCheck() {
        return this.isCheck;
    }
    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }
    public boolean getAgain() {
        return this.again;
    }
    public void setAgain(boolean again) {
        this.again = again;
    }

}
