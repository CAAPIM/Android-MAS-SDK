/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Render the QRCode for cross device session sharing
 */
public class QRCodeRenderer extends PollingRenderer {

    /**
     * Failed to generate QRCode error code
     */
    public static final int QRCODE_ERROR = 100;

    /**
     * Default height of QRCode image
     */
    private static final int HEIGHT = 250;

    /**
     * Default width of QRCode image
     */
    private static final int WIDTH = 250;

    /**
     * Writes the given Matrix on a new Bitmap object.
     *
     * @param matrix the matrix to write.
     * @return the new {@link Bitmap}-object.
     */
    private Bitmap toBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;

    }

    @Override
    public View render() {
        final QRCodeWriter writer = new QRCodeWriter();
        final ImageView imageView = new QRCode(context);

        try {
            BitMatrix matrix = writer.encode(provider.getUrl(), BarcodeFormat.QR_CODE, getQRCodeWidth(), getQRCodeHeight());
            Bitmap bmp = toBitmap(matrix);
            imageView.setImageBitmap(bmp);
            imageView.setContentDescription(provider.getId());
            return imageView;
        } catch (WriterException e) {
            onError(QRCODE_ERROR, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void onError(int code, final String m, Exception e) {
    }

    /**
     * Retrieve the QRCode image width
     * @return Width of the QRCode
     */
    protected int getQRCodeWidth() {
        return WIDTH;
    }

    /**
     * Retrieve the QRCode image height
     * @return Height of the QRCode
     */
    protected int getQRCodeHeight() {
        return HEIGHT;
    }

    @Override
    protected boolean startPollingOnStartup() {
        return true;
    }
}
