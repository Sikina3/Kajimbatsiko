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
        version = 5,
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

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE depense ADD COLUMN user_id TEXT");
            database.execSQL("ALTER TABLE revenue ADD COLUMN user_id TEXT");
        }
    };

    // Migration de la version 4 à 5 : Ajout de user_id dans economie
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE economie ADD COLUMN user_id TEXT");
        }
    };

    public static database getDatabase(final Context context){
        if (INSTANCE == null) {
            synchronized (database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    database.class, "finance.db")
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}