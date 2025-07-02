package com.foxishangxian.ebank.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface TransferRecordDao {
    @Insert
    void insert(TransferRecord record);

    @Update
    void update(TransferRecord record);

    @Delete
    void delete(TransferRecord record);

    @Query("SELECT * FROM transfer_record ORDER BY time DESC")
    List<TransferRecord> getAllTransferRecords();

    @Query("SELECT * FROM transfer_record ORDER BY time DESC")
    List<TransferRecord> getAllRecords();
} 