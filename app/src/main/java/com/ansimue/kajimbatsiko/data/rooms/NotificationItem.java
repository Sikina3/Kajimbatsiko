package com.ansimue.kajimbatsiko.data.rooms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "type")
    public String type; // "expense", "alert", "summary"

    @ColumnInfo(name = "is_read")
    public boolean isRead;

    @ColumnInfo(name = "icon_res")
    public int iconRes;
}