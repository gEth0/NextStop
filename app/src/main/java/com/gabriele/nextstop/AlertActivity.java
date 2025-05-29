package com.gabriele.nextstop;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class AlertActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onCreate(savedInstanceState);
        FunzioniHelper.mostraAvvisoEAvviaVibrazione(this);

    }
}
