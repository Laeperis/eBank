package com.foxishangxian.ebank.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface BankCardDao {
    @Insert
    void insert(BankCard card);

    @Update
    void update(BankCard card);

    @Delete
    void delete(BankCard card);

    @Query("SELECT * FROM bank_card WHERE userId = :userId")
    List<BankCard> getCardsByUserId(String userId);

    @Query("SELECT * FROM bank_card WHERE phone = :phone")
    List<BankCard> getCardsByPhone(String phone);

    @Query("SELECT * FROM bank_card WHERE id = :id")
    BankCard getCardById(String id);

    @Query("SELECT * FROM bank_card WHERE cardNumber = :cardNumber LIMIT 1")
    BankCard getCardByNumber(String cardNumber);

    @Query("SELECT * FROM bank_card")
    List<BankCard> getAllCards();
} 