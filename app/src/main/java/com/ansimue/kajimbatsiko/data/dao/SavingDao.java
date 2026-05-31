package com.ansimue.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ansimue.kajimbatsiko.data.rooms.DataSaving;

import java.util.List;

@Dao
public interface SavingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSaving(DataSaving economie);

    @Update
    void updateSaving(DataSaving economie);

    @Delete
    void deleteSaving(DataSaving economie);

    @Query("DELETE FROM economie")
    void deleteAllSavings();

    @Query("SELECT * FROM economie WHERE user_id = :userId ORDER BY " +
            "substr(date, 7, 4) || '-' || " +
            "CASE substr(date, 4, 2) " +
            "  WHEN 'janvier' THEN '01' WHEN 'février' THEN '02' " +
            "  WHEN 'mars' THEN '03' WHEN 'avril' THEN '04' " +
            "  WHEN 'mai' THEN '05' WHEN 'juin' THEN '06' " +
            "  WHEN 'juillet' THEN '07' WHEN 'août' THEN '08' " +
            "  WHEN 'septembre' THEN '09' WHEN 'octobre' THEN '10' " +
            "  WHEN 'novembre' THEN '11' WHEN 'décembre' THEN '12' " +
            "  ELSE '00' END || '-' || " +
            "substr('00' || substr(date, 1, 2), -2, 2) DESC")
    List<DataSaving> getAllSaving(String userId);

    @Query("SELECT IFNULL(SUM(montant), 0) FROM economie WHERE category_id = :category AND user_id = :userId")
    Double getTotalSaving(int category, String userId);

    @Query("SELECT IFNULL(SUM(montant), 0) FROM economie WHERE user_id = :userId")
    Double getTotalAllSaving(String userId);

    @Query("SELECT * FROM economie WHERE category_id = :categoryId AND user_id = :userId ORDER BY " +
            "substr(date, 7, 4) || '-' || " +
            "CASE substr(date, 4, 2) " +
            "  WHEN 'janvier' THEN '01' WHEN 'février' THEN '02' " +
            "  WHEN 'mars' THEN '03' WHEN 'avril' THEN '04' " +
            "  WHEN 'mai' THEN '05' WHEN 'juin' THEN '06' " +
            "  WHEN 'juillet' THEN '07' WHEN 'août' THEN '08' " +
            "  WHEN 'septembre' THEN '09' WHEN 'octobre' THEN '10' " +
            "  WHEN 'novembre' THEN '11' WHEN 'décembre' THEN '12' " +
            "  ELSE '00' END || '-' || " +
            "substr('00' || substr(date, 1, 2), -2, 2) DESC")
    List<DataSaving> getSavingByCategoryId(int categoryId, String userId);
}
