package com.teste.kajimbatsiko.data.rooms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "economie")
public class DataSaving {
    @PrimaryKey(autoGenerate = false)
    public String name;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "category_id")
    public int categoryId;
}
