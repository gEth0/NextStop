package com.gabriele.nextstop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.widget.Toast;
import android.Manifest;
import androidx.core.content.ContextCompat;

public class CustomAlertDialogs {
    public static void showTimerConfirm(Context context, String stopName, DialogInterface.OnClickListener positiveAction){
        new AlertDialog.Builder(context).setTitle(stopName)
                .setMessage("Vuoi attivare il timer dinamico?\nLascia l'applicazione in background e ti sveglierà quando sarai quasi arrivato\nBuon riposino!⏰")
                .setPositiveButton("Attiva",positiveAction)
                .setNegativeButton("Annulla",null).show();
    }

    @SuppressLint("MissingPermission")
    public static void showStopDialog(Context context, Vibrator vibrator){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("\uD83D\uDCCD Sei vicino alla fermata!");
        builder.setPositiveButton("Ferma vibrazione \uD83D\uDCF3 ", (dialog, which) -> {
            if (vibrator != null) {
                vibrator.cancel();
                context.stopService(new Intent(context, ServizioTracciamento.class));
                Toast.makeText(context," ⛔ Servizio disattivato\n Sei arrivato a destinazione \uD83C\uDF89",Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        });
        builder.setCancelable(false);
        builder.show();
    }
    public static void askBatteryOpt(Activity activity){
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        String packageName = activity.getPackageName();
        if(!pm.isIgnoringBatteryOptimizations(packageName)){
            new AlertDialog.Builder(activity)
                    .setTitle("Disattiva ottimizzazione batteria 🔋")
                    .setMessage("L'applicazione se non disattivi l'ottimizzazione batteria non può girare in background 💤 ")
                    .setPositiveButton("Disattiva", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        activity.startActivity(intent);

                    })
                    .show();
        }
    }
    public static void errorDialog(Context context,String error){
        new AlertDialog.Builder(context)
                .setTitle("Errore ")
                .setMessage(error)
                .setPositiveButton("ok", null)
                .show();
    }


    public static void askFineLocation(PermissionHelper fineLocation, MainActivity activity ,LocationTrackService location) {

        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            activity.initmap();
            location.startLocationUpdates();
        } else {
            // Permesso non concesso: mostra dialog che spiega perché serve e fa partire la richiesta
            new AlertDialog.Builder(activity)
                    .setTitle("Permesso posizione \uD83D\uDCCD")
                    .setMessage("L'app ha bisogno del permesso per accedere alla tua posizione per funzionare correttamente. \uD83D\uDC63")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            fineLocation.checkAndRequestPermission(new PermissionHelper.PermissionCallback() {
                                @Override
                                public void onPermissionGranted() {
                                    activity.initmap();

                                    location.startLocationUpdates();
                                }


                                @Override
                                public void onPermissionDenied() {
                                    Toast.makeText(activity, "Permesso posizione negato", Toast.LENGTH_SHORT).show();
                                    activity.finishAffinity();
                                    System.exit(0);
                                }
                            });
                        }
                    }

                    )
                    .setCancelable(false)
                    .show();
        }
    }
    public static void askNotification(PermissionHelper notificationPermission, MainActivity activity) {
        // Verifica se il permesso è già stato concesso
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

            return;
        }

        // Se il permesso non è concesso, mostra un dialog
        new AlertDialog.Builder(activity)
                .setTitle("Permesso notifiche")
                .setMessage("L'app ha bisogno del permesso per inviarti notifiche, anche quando è in background.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Crea il PermissionHelper e richiedi il permesso


                    notificationPermission.checkAndRequestPermission(new PermissionHelper.PermissionCallback() {
                        @Override
                        public void onPermissionGranted() {
                            Toast.makeText(activity, " \uD83D\uDC63 Notifiche permesse", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onPermissionDenied() {
                            Toast.makeText(activity, " ❌ L'app non potrà notificarti se è chiusa o in background", Toast.LENGTH_LONG).show();
                        }
                    });
                })

                .setCancelable(false)
                .show();
    }



}
