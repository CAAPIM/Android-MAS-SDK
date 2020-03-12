package com.ca.mas.core.service;

import android.content.ServiceConnection;


/**
 * Represents state of the {@link MssoService}, if it is bound or not.
 * Also maintains a {@link MssoService} instance and a {@link ServiceConnection} instance.
 */
public class MssoServiceState {

    private  static MssoServiceState _instance;
    private  MssoService mssoService;
    private  boolean isBound;
    private  ServiceConnection serviceConnection;

    private MssoServiceState(){

    }

    public static MssoServiceState getInstance(){
        if(_instance == null)
            _instance = new MssoServiceState();

        return _instance;
    }

    public MssoService getMssoService() {
        return mssoService;
    }

    public void setMssoService(MssoService mssoService) {
        this.mssoService = mssoService;
    }

    public boolean isBound() {
        return isBound;
    }

    public void setBound(boolean bound) {
        isBound = bound;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public void setServiceConnection(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }
}
