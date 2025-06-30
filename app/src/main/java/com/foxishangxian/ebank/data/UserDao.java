package com.foxishangxian.ebank.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User getUserByUid(String uid);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    User getLoggedInUser();

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("UPDATE users SET isLoggedIn = 0")
    void logoutAll();

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);
} 