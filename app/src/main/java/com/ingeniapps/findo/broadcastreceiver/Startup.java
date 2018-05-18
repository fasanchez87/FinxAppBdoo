package com.ingeniapps.findo.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ingeniapps.findo.service.LocationClientService;

public class Startup extends BroadcastReceiver
{
    public Startup()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // start your service here
        context.startService(new Intent(context, LocationClientService.class));
    }
}
