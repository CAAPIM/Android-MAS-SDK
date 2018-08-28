package com.ca.mas.foundation;

import com.ca.mas.core.error.MAGException;

public class MASDeviceAttributeOverflowException extends MAGException {

    public MASDeviceAttributeOverflowException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
