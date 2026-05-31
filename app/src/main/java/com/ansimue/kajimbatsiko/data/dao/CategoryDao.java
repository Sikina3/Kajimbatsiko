package com.ansimue.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ansimue.kajimbatsiko.data.rooms.DataCategory;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categorie")
    List<DataCategory> getAllCategory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(DataCategory categories);

    @Update
    void updateCategory(DataCategory categories);

    @Delete
    void deleteCategory(DataCategory categorie);

    @Query("DELETE FROM categorie")
    void deleteAllCategories();

    @Query("SELECT icon FROM categorie where uid = :category")
    int getIconCategory(int category);

    @Query("SELECT nom FROM categorie where uid = :category")
    String getCategoryName(int category);
}
