package com.foxishangxian.ebank.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TransferRecordDao {
    @Insert
    void insert(TransferRecord record);

    @Query("SELECT * FROM TransferRecord ORDER BY time DESC")
    List<TransferRecord> getAllRecords();
} 