package com.ansimue.kajimbatsiko.utils;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.ansimue.kajimbatsiko.R;
import com.ansimue.kajimbatsiko.home;

import java.util.Locale;

public class NotificationHelper {

    private static final String CHANNEL_ID = "kajimbatsiko_channel";
    private static final String CHANNEL_NAME = "Kajimbatsiko Notifications";
    private static final String CHANNEL_DESC = "Notifications pour vos finances";

    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void sendExpenseNotification(String titre, double montant, String categoryName) {
        if (!hasNotificationPermission()) return;
        showNotification("Dépense enregistrée", titre + " : Ar " + formatMontant(montant) + " (" + categoryName + ")", R.drawable.money);
    }

    @SuppressLint("MissingPermission")
    public void sendIncomeNotification(String titre, double montant) {
        if (!hasNotificationPermission()) return;
        showNotification("Nouveau revenu", titre + " : Ar " + formatMontant(montant), R.drawable.money);
    }

    @SuppressLint("MissingPermission")
    public void sendSavingNotification(String titre, double montant) {
        if (!hasNotificationPermission()) return;
        showNotification("Épargne ajoutée", titre + " : Ar " + formatMontant(montant), R.drawable.money);
    }

    @SuppressLint("MissingPermission")
    public void sendBudgetAlertNotification(double totalExpense, double totalIncome) {
        if (!hasNotificationPermission()) return;

        double deficit = totalExpense - totalIncome;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.money)
                .setContentTitle("⚠️ ALERTE BUDGET")
                .setContentText("Déficit de Ar " + formatMontant(deficit))
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Attention, vos dépenses dépassent vos revenus !"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(createPendingIntent())
                .setAutoCancel(true)
                .setColor(0xFFFF0000);

        notificationManager.notify(999, builder.build());
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message, int icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createPendingIntent())
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(context, home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private String formatMontant(double montant) {
        return String.format(Locale.FRENCH, "%,.0f", montant).replace(",", " ");
    }
}