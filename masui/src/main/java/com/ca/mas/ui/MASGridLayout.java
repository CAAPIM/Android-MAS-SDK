/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class MASGridLayout extends android.support.v7.widget.GridLayout {
    View[] mChild = null;

    public MASGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MASGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MASGridLayout(Context context) {
        this(context, null);
    }

    private void arrangeElements() {
        mChild = new View[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            mChild[i] = getChildAt(i);
        }

        removeAllViews();
        for (int j = 0; j < mChild.length; j++) {
            if (mChild[j].getVisibility() != GONE) {
                addView(mChild[j]);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        arrangeElements();
        super.onLayout(changed, left, top, right, bottom);
    }
}
