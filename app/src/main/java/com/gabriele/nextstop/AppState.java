package com.gabriele.nextstop;

import android.content.Context;
import android.os.Vibrator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AppState {

    private static AppState instance;

    private final MutableLiveData<Boolean> timerAttivo = new MutableLiveData<>(false);
    private final MutableLiveData<Fermata> fermataSelezionata = new MutableLiveData<>(null);
    private final MutableLiveData<Vibrator> vibrator = new MutableLiveData<>(null);
   private AppState() {}
    public static synchronized AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }
    public MutableLiveData<Vibrator> getVibrator(){
       return vibrator;
    }
    public void setVibrator(Vibrator vibrator){
       this.vibrator.postValue(vibrator);
    }
    public LiveData<Boolean> getTimerAttivo() {
        return timerAttivo;
    }

    public void setTimerAttivo(boolean attivo) {
        timerAttivo.postValue(attivo);
    }

    public LiveData<Fermata> getFermataSelezionata() {
        return fermataSelezionata;
    }

    public void setFermataSelezionata(Fermata fermata) {
        fermataSelezionata.postValue(fermata);
    }
}