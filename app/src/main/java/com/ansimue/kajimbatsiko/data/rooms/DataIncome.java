package com.ansimue.kajimbatsiko.data.rooms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "revenue")
public class DataIncome {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "montant")
    public double montant;

    @ColumnInfo(name = "titre_revenue")
    public String titre_revenue;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "user_id")
    public String userId; // Pour l'ID Firebase
}
