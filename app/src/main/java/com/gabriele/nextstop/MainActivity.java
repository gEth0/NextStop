package com.gabriele.nextstop;



import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;


import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_BACK_CODE = 101;
    WebView mappa;
    OttieniFermate ottieniFermate;

    WebSettings ws;
    TextView timerStatus;
    PermissionHelper fineLocation,backgroundLocation,notificationPermission;
    Button search;
    EditText query;
    ArrayAdapter<Fermata> adapter;
    ArrayList<Fermata> listaFermate = new ArrayList<>();
    ListView listView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        IntentFilter filter = new IntentFilter("com.gabriele.nextstop.ALERTA_FERMATA");
        registerReceiver(alertReceiver, filter,RECEIVER_EXPORTED);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // INIZIO PERMESSI
        fineLocation = new PermissionHelper(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE);
        LocationTrackService location = new LocationTrackService(this, new LocationTrackService.LocationUpdateListener() {
            @Override
            public void onLocationUpdate(double latitude, double longitude) {
                runOnUiThread(() -> {
                    FunzioniHelper.aggiornaPosizioneSullaMappa( latitude, longitude, mappa);
                });
            }
        });
        CustomAlertDialogs.askFineLocation(fineLocation,this,location); // CHIEDE PERMESSO DELLA POSIZIONE IN FOREGROUND

        CustomAlertDialogs.askBatteryOpt(this); // CHIEDE IL PERMESSO DELL'OTTIMIZZAZIONE DELLA BATTERIA

        //FINE PERMESSI , l'app se ha i permessi può procedere

        initWidget();
        // AGGIORNA LA TEXTVIEW RELATIVA AI TIMER ATTIVI ASCOLTANDO PER CAMBIAMENTI
        AppState.getInstance().getTimerAttivo().observe(this, attivo -> {
            FunzioniHelper.aggiornaTVTImer(timerStatus);
        });



        //INIT LISTVIEW E ADAPTER
        adapter = new ArrayAdapter<Fermata>(this, android.R.layout.simple_list_item_1,listaFermate);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Fermata selected = adapter.getItem(i);

                runOnUiThread(()->{
                   CustomAlertDialogs.showTimerConfirm(MainActivity.this, selected.getStopName(), new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           backgroundLocation = new PermissionHelper(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION,LOCATION_BACK_CODE);

                           backgroundLocation.checkAndRequestPermission(new PermissionHelper.PermissionCallback() {
                               @Override
                               public void onPermissionGranted() {
                                   notificationPermission = new PermissionHelper(MainActivity.this,Manifest.permission.POST_NOTIFICATIONS,1002);

                                    CustomAlertDialogs.askNotification(notificationPermission,MainActivity.this);

                                   if(AppState.getInstance().getTimerAttivo().getValue()){
                                       Toast.makeText(MainActivity.this, "\uD83D\uDD34 Timer già attivo per una fermata.", Toast.LENGTH_LONG).show();
                                   }else {
                                       AppState.getInstance().setFermataSelezionata(selected);
                                       AppState.getInstance().setTimerAttivo(true);

                                       Intent serviceIntent = new Intent(MainActivity.this, ServizioTracciamento.class);
                                       serviceIntent.putExtra("DEST_LAT", selected.getLatitude());
                                       serviceIntent.putExtra("DEST_LON", selected.getLongitude());
                                       serviceIntent.putExtra("DEST_NAME", selected.getStopName());
                                       startForegroundService(serviceIntent);

                                       Toast.makeText(MainActivity.this, "⏰Servizio attivato", Toast.LENGTH_SHORT).show();
                                       FunzioniHelper.aggiornaTVTImer(timerStatus);

                                   }
                               }

                               @Override
                               public void onPermissionDenied() {
                                   // Permesso background negato, puoi gestirlo
                                   Toast.makeText(MainActivity.this,
                                           "Permesso localizzazione background negato",
                                           Toast.LENGTH_LONG).show();
                                   finishAffinity();
                               }
                           });
                       }
                   });
                });

            }
        });
        //INIT API OTTIENI FERMATE
        ottieniFermate = new OttieniFermate(new OttieniFermate.Listener() {
            @Override
            public void onFermateOttenute(ArrayList<Fermata> fermate) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss(); // Nascondi il caricamento
                }
                listaFermate.clear();
                listaFermate.addAll(fermate);
                FunzioniHelper.passaFermateAJs(listaFermate, mappa);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onErrore(Exception e) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                CustomAlertDialogs.errorDialog(MainActivity.this, " 🌍 Errore di connessione, controlla se sei collegato a internet");
            }
        });



    }

    public void initWidget(){
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(" \uD83D\uDD04 Caricamento fermate...");
        progressDialog.setCancelable(false); // L’utente non può chiuderlo manualmente
        timerStatus = findViewById(R.id.timerStatusView);
        timerStatus.setOnClickListener(v->{
            FunzioniHelper.eliminaTimer(this);

        });
        listView = findViewById(R.id.lv_fermate);
        query = findViewById(R.id.query);
        search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String testo = query.getText().toString();
                if(!testo.isEmpty()){
                    progressDialog.show();
                    ottieniFermate.esegui(query.getText().toString());
                }
                query.setText("");
                //nasconde la tasiera per una migliore esperienza
                View currentView = getCurrentFocus();
                if (currentView != null) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(currentView.getWindowToken(), 0);
                }
            }
        });
    }
    public void initmap(){
        mappa = findViewById(R.id.mappa);
         ws = mappa.getSettings();
        ws.setJavaScriptEnabled(true);
        mappa.loadUrl("file:///android_asset/map.html");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        fineLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);
        backgroundLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);
        notificationPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.gabriele.nextstop.ALERTA_FERMATA");
        registerReceiver(alertReceiver, filter,RECEIVER_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(alertReceiver);
    }

    private final BroadcastReceiver alertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FunzioniHelper.mostraAvvisoEAvviaVibrazione(MainActivity.this);
        }
    };



}