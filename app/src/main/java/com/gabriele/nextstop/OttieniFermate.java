package com.gabriele.nextstop;


import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OttieniFermate {

    public interface Listener {
        void onFermateOttenute(ArrayList<Fermata> fermate);
        void onErrore(Exception e);
    }

    private static final String API_URL = "https://transit.land/api/v2/rest/stops";

    private Listener listener;
    private OkHttpClient client;
    private Handler mainHandler;

    public OttieniFermate(Listener listener) {
        this.listener = listener;
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void esegui(String query) {
        Uri builtUri = Uri.parse(API_URL).buildUpon()
                .appendQueryParameter("search", query).appendQueryParameter("apikey",BuildConfig.API_KEY).appendQueryParameter("limit","10")
                .build();
        String url = builtUri.toString();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> listener.onErrore(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> listener.onErrore(new IOException("HTTP code: " + response.code())));
                    return;
                }

                String jsonResponse = response.body().string();

                try {
                    ArrayList<Fermata> fermate = Fermata.fromJson(jsonResponse);
                    mainHandler.post(() -> listener.onFermateOttenute(fermate));
                } catch (Exception e) {
                    mainHandler.post(() -> listener.onErrore(e));
                }
            }
        });
    }
}
