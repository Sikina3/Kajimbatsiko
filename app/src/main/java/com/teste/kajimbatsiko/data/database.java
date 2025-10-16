package com.teste.kajimbatsiko.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.teste.kajimbatsiko.data.dao.CategoryDao;
import com.teste.kajimbatsiko.data.dao.Category_SavingDao;
import com.teste.kajimbatsiko.data.dao.ExpenseDao;
import com.teste.kajimbatsiko.data.dao.IncomeDao;
import com.teste.kajimbatsiko.data.dao.SavingDao;
import com.teste.kajimbatsiko.data.rooms.DataCategory;
import com.teste.kajimbatsiko.data.rooms.DataCategorySaving;
import com.teste.kajimbatsiko.data.rooms.DataExpenses;
import com.teste.kajimbatsiko.data.rooms.DataIncome;
import com.teste.kajimbatsiko.data.rooms.DataSaving;

@Database(
        entities = {DataExpenses.class, DataCategory.class, DataIncome.class, DataCategorySaving.class, DataSaving.class},
        version = 2,
        exportSchema = false
)
public abstract class database extends RoomDatabase {
    public abstract ExpenseDao expenseDao();
    public abstract IncomeDao incomeDao();
    public abstract CategoryDao categoryDao();
    public abstract Category_SavingDao category_savingDao();
    public abstract SavingDao savingDao();

    private static volatile database INSTANCE;

    public static database getDatabase(final Context context){
        if (INSTANCE == null) {
            synchronized (database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    database.class, "finance.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
