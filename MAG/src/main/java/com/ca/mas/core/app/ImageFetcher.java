/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

public class ImageFetcher extends AsyncTask<Object, Object, Object> {

    //Use 1/24th of the available memory for image cache.
    private static LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 24));
    ImageView imageView;
    private HttpURLConnection connection;
    private InputStream is;
    private Bitmap bitmap;

    public ImageFetcher(ImageView mImageView) {
        imageView = mImageView;
    }

    @Override
    protected Object doInBackground(Object... params) {

        Bitmap cached = cache.get((String) params[0]);

        if (cached != null)  {
            return cached;
        }
        try {
            URL url = new URL((String) params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setUseCaches(true);
            connection.connect();
            is = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error in downloading the image", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                if (DEBUG) Log.w(TAG, "Failed to clear up connection.", e);
            }
        }
        if (bitmap != null) {
            cache.put((String) params[0], bitmap);
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        if (null != result) {
            imageView.setImageBitmap((Bitmap) result);
        }
    }
}
