

package com.gabriele.nextstop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent popupIntent = new Intent(context, AlertActivity.class);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(popupIntent);
    }
}
