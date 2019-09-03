package com.ca.mas.core.util;


import android.net.Uri;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASException;

import java.io.ByteArrayOutputStream;
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
    public static byte[] getBytesFromPath(String path) throws MASException, IOException {
        byte[] bytes = {};
        InputStream is = null;
        try {
            File file = new File(path);
            bytes = new byte[(int) file.length()];
            is = new FileInputStream(file);
            is.read(bytes);
        } catch (IOException e) {
            throw new MASException(e);
        } finally {
            if (is != null)
                is.close();
        }
        return bytes;
    }

    /**
     * Returns the file content as byte array.
     *
     * @param uri the file uri.
     * @return file content as byte array
     */
    public static byte[] getBytesFromUri(Uri uri) throws MASException, IOException {

        InputStream inputStream = null;
        byte[] bytes;
        try {
            inputStream = MAS.getContext().getContentResolver().openInputStream(uri);
            bytes = getBytes(inputStream);
        } catch (IOException e) {
            throw new MASException(e);
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
        return bytes;
    }


    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static String getFileTyepFromUri(Uri uri) {
        return MAS.getContext().getContentResolver().getType(uri);
    }
}
