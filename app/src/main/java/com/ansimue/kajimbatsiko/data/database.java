package com.ansimue.kajimbatsiko.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ansimue.kajimbatsiko.data.dao.CategoryDao;
import com.ansimue.kajimbatsiko.data.dao.Category_SavingDao;
import com.ansimue.kajimbatsiko.data.dao.ExpenseDao;
import com.ansimue.kajimbatsiko.data.dao.IncomeDao;
import com.ansimue.kajimbatsiko.data.dao.NotificationDao;
import com.ansimue.kajimbatsiko.data.dao.SavingDao;
import com.ansimue.kajimbatsiko.data.rooms.DataCategory;
import com.ansimue.kajimbatsiko.data.rooms.DataCategorySaving;
import com.ansimue.kajimbatsiko.data.rooms.DataExpenses;
import com.ansimue.kajimbatsiko.data.rooms.DataIncome;
import com.ansimue.kajimbatsiko.data.rooms.DataSaving;
import com.ansimue.kajimbatsiko.data.rooms.NotificationItem;

@Database(
        entities = {
                DataExpenses.class,
                DataCategory.class,
                DataIncome.class,
                DataCategorySaving.class,
                DataSaving.class,
                NotificationItem.class
        },
        version = 3,
        exportSchema = false
)
public abstract class database extends RoomDatabase {
    public abstract ExpenseDao expenseDao();
    public abstract IncomeDao incomeDao();
    public abstract CategoryDao categoryDao();
    public abstract Category_SavingDao category_savingDao();
    public abstract SavingDao savingDao();
    public abstract NotificationDao notificationDao();

    private static volatile database INSTANCE;

    // Migration de la version 2 à 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT, " +
                    "`message` TEXT, " +
                    "`timestamp` INTEGER NOT NULL, " +
                    "`type` TEXT, " +
                    "`is_read` INTEGER NOT NULL, " +
                    "`icon_res` INTEGER NOT NULL)");
        }
    };

    public static database getDatabase(final Context context){
        if (INSTANCE == null) {
            synchronized (database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    database.class, "finance.db")
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}