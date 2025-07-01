package com.foxishangxian.ebank.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TransferRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String fromCard;
    public String toCard;
    public long time;
    public String fromUid;
    public String toUid;
    public double amount;
} 