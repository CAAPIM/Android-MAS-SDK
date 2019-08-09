package com.ca.mas.core.util;


import com.ca.mas.foundation.MASException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility methods for file operations.
 */
public class FileUtils {

    /**
     * Returns the file content as byte array.
     *
     * @param path the absolute path of file
     * @return file content as byte array
     */
    public static byte[] getBytesFromPath(String path) throws MASException {
        byte[] bytes = {};
        try {
            File file = new File(path);
            bytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(bytes);
            is.close();
        } catch (IOException e) {
            throw new MASException(e);
        }
        return bytes;
    }
}