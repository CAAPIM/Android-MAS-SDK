package com.ca.mas.foundation;


public class MASDeviceAttributeOverflowException extends Exception {

    private final Throwable object;

    public MASDeviceAttributeOverflowException(Throwable e) {
        this.object = e;
    }
}
