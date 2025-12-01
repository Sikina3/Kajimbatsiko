package com.teste.kajimbatsiko.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.teste.kajimbatsiko.data.rooms.NotificationItem;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insertNotification(NotificationItem notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 50")
    List<NotificationItem> getAllNotifications();

    @Query("SELECT * FROM notifications WHERE is_read = 0 ORDER BY timestamp DESC")
    List<NotificationItem> getUnreadNotifications();

    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    int getUnreadCount();

    @Update
    void updateNotification(NotificationItem notification);

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    void markAsRead(int notificationId);

    @Query("UPDATE notifications SET is_read = 1")
    void markAllAsRead();

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    void deleteNotification(int notificationId);

    @Query("DELETE FROM notifications")
    void deleteAllNotifications();
}