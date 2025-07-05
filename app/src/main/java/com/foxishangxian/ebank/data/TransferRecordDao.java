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

    // 获取今日收益（今日转入的金额）
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transfer_record WHERE toUid = :userId AND date(time/1000, 'unixepoch') = date('now')")
    double getTodayEarnings(String userId);

    // 获取累计收益（所有转入的金额）
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transfer_record WHERE toUid = :userId")
    double getTotalEarnings(String userId);
} 