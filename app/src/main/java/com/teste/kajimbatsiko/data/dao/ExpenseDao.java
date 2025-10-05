package com.teste.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.teste.kajimbatsiko.data.rooms.DataExpenses;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insertExpense(DataExpenses depense);

    @Query("SELECT * FROM depense ORDER BY date DESC")
    List<DataExpenses> getAllExpense();

    @Query("SELECT IFNULL(SUM(montant), 0) FROM depense")
    Double getTotalExpense();

    @Query("SELECT * FROM depense WHERE category_id = :categoryId ORDER BY date DESC")
    List<DataExpenses> getExpenseByCategoryId(int categoryId);

    @Query("SELECT c.name FROM depense d INNER JOIN categorie c ON d.category_id = c.uid WHERE d.category_id = :categoryId")
    List<String> getCategoryNamesByExpense(int categoryId);
}
