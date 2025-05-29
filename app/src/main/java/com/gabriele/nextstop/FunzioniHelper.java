package com.gabriele.nextstop;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;


public class FunzioniHelper {
    static Gson gson = new Gson();
    private static Vibrator vibrator;

    public static void passaFermateAJs(ArrayList<Fermata> lista, WebView mappa){
        String fermate = gson.toJson(lista);
        mappa.evaluateJavascript("posizionaFermate("+fermate+")",null);
    }


    // Soglia di prossimità in metri
    private static final float DISTANZA_SOGLIA_METRI = 400f;

    public static boolean isVicinoAllaDestinazione(double userLat, double userLon, double destLat, double destLon) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(userLat, userLon, destLat, destLon, results);
        return results[0] < DISTANZA_SOGLIA_METRI;
    }

    @SuppressLint("MissingPermission")
    public static void mostraAvvisoEAvviaVibrazione(Activity activity) {
        // Accendi lo schermo per qualche secondo
        PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "NextStop::ScreenWakeLock");
        wakeLock.acquire(3000);

        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createWaveform(
                        new long[]{0, 500, 500}, 0); // 500ms ON, 500ms OFF, loop infinito
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(new long[]{0, 500, 500}, 0);
            }
        }

        CustomAlertDialogs.showStopDialog(activity, vibrator);
    }


    public static void aggiornaTVTImer(TextView timer){
        if(AppState.getInstance().getTimerAttivo().getValue()){
            timer.setText(" \uD83D\uDFE2 Timer attivi:\n \uD83D\uDE8F Fermata - "+AppState.getInstance().getFermataSelezionata().getValue().getStopName()+"\nClicca se vuoi fermare il timer");
        }else{
            timer.setText("❌ Nessun timer attivo");
        }
    }
    public static void eliminaTimer(Context context){
        if(AppState.getInstance().getTimerAttivo().getValue() ){
            Intent intent = new Intent(context, ServizioTracciamento.class);
            context.stopService(intent);
            AppState.getInstance().setTimerAttivo(false);
            AppState.getInstance().setFermataSelezionata(null);
            Toast.makeText(context," ❌ Servizio disattivato",Toast.LENGTH_LONG).show();
        }
        return;
    }
    public static void aggiornaPosizioneSullaMappa(double latitude,double longitude,WebView mappa){
        String lat = String.valueOf(latitude).replace(',', '.');
        String lon = String.valueOf(longitude).replace(',', '.');
        mappa.evaluateJavascript("updatePosition(" + lat + "," + lon + ")", null);
    }

}
