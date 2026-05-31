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
        version = 7,
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

    // Migration de la version 5 à 6 : Renommage de la colonne 'name' en 'nom' dans 'categorie'
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("PRAGMA foreign_keys=OFF");
            database.execSQL("CREATE TABLE IF NOT EXISTS `categorie_new` (" +
                    "`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`nom` TEXT, " +
                    "`icon` INTEGER NOT NULL)");
            database.execSQL("INSERT INTO `categorie_new` (`uid`, `nom`, `icon`) " +
                    "SELECT `uid`, `name`, `icon` FROM `categorie` ");
            database.execSQL("DROP TABLE `categorie`");
            database.execSQL("ALTER TABLE `categorie_new` RENAME TO `categorie`");
            database.execSQL("PRAGMA foreign_keys=ON");
        }
    };

    // Migration de la version 6 à 7 : Ajout de la colonne 'devise' dans 'categorie_economie'
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Ajout de la colonne devise dans categorie_economie si elle n'existe pas
            try {
                database.execSQL("ALTER TABLE categorie_economie ADD COLUMN devise REAL NOT NULL DEFAULT 0");
            } catch (Exception e) {
                // La colonne existe peut-être déjà
            }
        }
    };

    public static database getDatabase(final Context context){
        if (INSTANCE == null) {
            synchronized (database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    database.class, "finance.db")
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                            .fallbackToDestructiveMigration()
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}