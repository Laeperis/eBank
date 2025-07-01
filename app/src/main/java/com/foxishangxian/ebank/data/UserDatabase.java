package com.foxishangxian.ebank.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {User.class, BankCard.class, TransferRecord.class}, version = 5, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    private static volatile UserDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract BankCardDao bankCardDao();
    public abstract TransferRecordDao transferRecordDao();

    public static UserDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UserDatabase.class, "user_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 