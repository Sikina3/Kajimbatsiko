package com.teste.kajimbatsiko.utils;

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

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.home;

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

    // Vérifie si la permission POST_NOTIFICATIONS est accordée
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Versions < Android 13
    }

    // Envoi notification pour une nouvelle dépense
    @SuppressLint("MissingPermission")
    public void sendExpenseNotification(String titre, double montant, String categoryName) {
        if (!hasNotificationPermission()) return;

        PendingIntent pendingIntent = createPendingIntent();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.money)
                .setContentTitle("Nouvelle dépense enregistrée")
                .setContentText(titre + " - Ar " + formatMontant(montant) + " (" + categoryName + ")")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(getNotificationId(), builder.build());
    }

    // NotificationDao d'alerte budget (dépenses > revenus)
    public void sendBudgetAlertNotification(double totalExpense, double totalIncome) {
        if (!hasNotificationPermission()) return;

        PendingIntent pendingIntent = createPendingIntent();
        double deficit = totalExpense - totalIncome;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.money)
                .setContentTitle("ALERTE BUDGET")
                .setContentText("Vos dépenses dépassent vos revenus de Ar " + formatMontant(deficit))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Vos dépenses (Ar " + formatMontant(totalExpense) +
                                ") dépassent vos revenus (Ar " + formatMontant(totalIncome) +
                                "). Vous êtes en déficit de Ar " + formatMontant(deficit) + "."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(0xFFFF0000); // Rouge

        notificationManager.notify(999, builder.build()); // ID fixe pour l'alerte budget
    }

    // NotificationDao de résumé quotidien
    public void sendDailySummaryNotification(double totalExpenseToday, int transactionCount) {
        if (!hasNotificationPermission()) return;

        PendingIntent pendingIntent = createPendingIntent();
        String message = transactionCount > 0
                ? "Vous avez dépensé Ar " + formatMontant(totalExpenseToday) + " aujourd'hui (" + transactionCount + " transaction(s))"
                : "Aucune dépense enregistrée aujourd'hui";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.money)
                .setContentTitle("Résumé du jour")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(998, builder.build());
    }

    // Création d'un PendingIntent vers l'activité home
    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(context, home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    // Formatage montant
    private String formatMontant(double montant) {
        return String.format("%,.0f", montant).replace(",", " ");
    }

    // ID unique pour chaque notification
    private int getNotificationId() {
        return (int) System.currentTimeMillis();
    }
}
