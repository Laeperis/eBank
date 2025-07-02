package com.foxishangxian.ebank.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "transfer_record")
public class TransferRecord {
    @PrimaryKey
    @NonNull
    public String id;
    public String fromCard;
    public String toCard;
    public long time;
    public String fromUid;
    public String toUid;
    public double amount;
    public String fromUserId;
    public String toUserId;
    public String description;
    public String status;
    public String transferTime;

    public TransferRecord() {
        // 无参构造函数
    }
} 