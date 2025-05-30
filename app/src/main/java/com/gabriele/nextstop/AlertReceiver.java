package com.gabriele.nextstop;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlertReceiver extends BroadcastReceiver {
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent fullScreenIntent = new Intent(context, AlertActivity.class);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "NextStop",
                    "Avvisi Fermata",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifiche con avviso visivo e sonoro");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // mostra a schermo bloccato

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Costruzione della notifica
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NextStop")
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(Color.parseColor("#4CAF50"))
                .setContentTitle("🚌 Sei vicino alla fermata!")
                .setContentText("NextStop ti sta avvisando, clicca per fermare la vibrazione \uD83D\uDCF3 ")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(123, builder.build());

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        AppState.getInstance().setVibrator(v);
        if (v != null && v.hasVibrator()) {
            long[] pattern = {0, 1000, 500, 1000, 500, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                v.vibrate(pattern, 0);
            }
        }
    }
}
//ADESSO FUNZIONA IN MODO CORRETTO , CAPIRE COME RIORGANIZZARE TUTTI I DIALOG ECC