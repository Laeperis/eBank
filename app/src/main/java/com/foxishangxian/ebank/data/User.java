package com.foxishangxian.ebank.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String uid; // 唯一标识符
    public String userCode;

    public String username;
    public String password;
    public String email;
    public String phone;
    public boolean isLoggedIn;
    public String avatarUri; // 头像图片的本地uri，可为空
    public boolean isAdmin; // 是否为管理员

    // 预留字段：邮箱验证、手机验证
    public boolean isEmailVerified = false;
    public boolean isPhoneVerified = false;

    public User(@NonNull String uid, String username, String password, String email, String phone, String userCode) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.userCode = userCode;
        this.isLoggedIn = false;
        this.avatarUri = null;
        this.isAdmin = false;
    }
} 