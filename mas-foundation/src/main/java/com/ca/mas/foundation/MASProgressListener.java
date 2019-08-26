package com.ca.mas.foundation;

import com.ca.mas.core.error.MAGError;


/**
 * <p><b>MASProgressListener</b> is implemented by classes that want to know the progress on file upload/download.
 */
public interface MASProgressListener {

    /**
     * Invoked while the upload/download is in progress.
     */
    void onProgress(String progressPercent);

    /**
     * Invoked when the file upload/download is complete.
     */
    void onComplete();

    /**
     * Invoked when the file upload/download error occurs.
     */
    void onError(MAGError error);

}
