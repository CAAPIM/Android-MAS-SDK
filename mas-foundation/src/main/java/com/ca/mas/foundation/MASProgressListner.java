package com.ca.mas.foundation;

    /**
     * Interface that allows for implementing classes to listen for file upload/download progress updates..
     * Listener is registered with {@link MASRequest.MASRequestBuilder#connectionListener(MASConnectionListener)} object.
     */
    public interface MASProgressListner {

        void onComplete();
        void onError(Error error);
        void onProgress(int i);
    }

