package com.ca.mas.core.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.ca.mas.foundation.MAS;

import static com.ca.mas.foundation.MAS.TAG;

/**
 * A ServiceConnection class used for binding to MssoService. This connection instance will be used to bind and unbind from MssoService.
 */

public class MssoServiceConnection implements ServiceConnection {
    private Intent intent;

    public MssoServiceConnection(Intent intent){
     this.intent = intent;
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if(MAS.DEBUG){
            Log.d(TAG, "onServiceConnected called");
        }
        MssoService.MASBinder binder = (MssoService.MASBinder) service;
        MAS.setService(binder.getService());
        MAS.setIsBound(true);
        MAS.getService().handleWork(intent);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if(MAS.DEBUG){
            Log.d(TAG, "onServiceDisconnected called");
        }
        MAS.setIsBound(false);

    }
}
