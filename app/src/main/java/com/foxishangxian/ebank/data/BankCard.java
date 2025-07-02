package com.foxishangxian.ebank.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.annotation.NonNull;
import java.util.UUID;

@Entity(tableName = "bank_card")
public class BankCard {
    @PrimaryKey
    @NonNull
    public String id;
    public String userId;
    public String cardType; // 卡片种类
    public String cardNumber; // 银行卡号
    public double balance; // 人民币余额
    public String startDate; // 启用日期
    public String endDate;   // 到期日期
    public double limitPerDay; // 当日限额
    public String phone; // 绑定手机号
    public String password; // 银行卡密码

    public BankCard() {
        // 无参构造函数
    }

    @Ignore
    public BankCard(String userId, String cardType, String cardNumber, double balance, String startDate, String endDate, double limitPerDay, String phone, String password) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.balance = balance;
        this.startDate = startDate;
        this.endDate = endDate;
        this.limitPerDay = limitPerDay;
        this.phone = phone;
        this.password = password;
    }
} 