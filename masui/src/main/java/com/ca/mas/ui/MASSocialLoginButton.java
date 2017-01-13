/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MASSocialLoginButton extends CardView {
    private int elevationNormal, elevationPressed;
    private ImageView imageView;
    private TextView textView;
    private LinearLayout linearLayout;

    public MASSocialLoginButton(Context context) {
        this(context, null);
    }

    public MASSocialLoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MASSocialLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton, defStyleAttr, 0);

            // LinearLayout attrs
            final int paddingLeft = (int) a.getDimension(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_padding_left, 0);
            final int paddingRight = (int) a.getDimension(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_padding_right, 0);
            linearLayout.setPadding(paddingLeft, 0, paddingRight, 0);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            // Text attrs
            final String text = a.getString(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_text);
            final int textColor = a.getColor(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_text_color, textView.getSolidColor());
            final int textSize = a.getDimensionPixelSize(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_text_size, 12);
            textView.setText(text);
            textView.setTextColor(textColor);
            if (textSize > 0) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }

            // Image attrs
            final float imgWidth = a.getDimension(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_image_width, 140);
            final float imgHeight = a.getDimension(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_image_height, 140);
            final Drawable disabled = a.getDrawable(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_image_src_disabled);
            final Drawable enabled = a.getDrawable(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_image_src_enabled);
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[] {android.R.attr.state_enabled}, enabled);
            states.addState(new int[] {-android.R.attr.state_enabled}, disabled);
            imageView.setImageDrawable(states);
            final float imageMarginRight = a.getDimension(com.ca.mas.ui.R.styleable.com_ca_mas_ui_MASSocialLoginButton_image_margin_right, 0f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) imgWidth, (int) imgHeight);
            layoutParams.setMargins(0, 0, (int) imageMarginRight, 0);
            imageView.setLayoutParams(layoutParams);

            a.recycle();
        }
    }

    private void init(Context context) {
        elevationNormal = dpToPx(5);
        elevationPressed = dpToPx(12);
        setClickable(true);
        setCardElevation(elevationNormal);
        setRadius(8);
        setUseCompatPadding(true);

        // Ripple effect
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        setForeground(ContextCompat.getDrawable(context, outValue.resourceId));

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(linearLayoutParams);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(linearLayout);

        // Set up image view
        imageView = new ImageView(context);
        linearLayout.addView(imageView);

        // Set up text view
        textView = new TextView(context);
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        linearLayout.addView(textView);
        linearLayout.setGravity(Gravity.CENTER);
        textView.setLayoutParams(linearLayoutParams);
        textView.setGravity(Gravity.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setCardElevation(elevationPressed);
                break;
            case MotionEvent.ACTION_UP:
                setCardElevation(elevationNormal);
                break;
        }

        return super.onTouchEvent(event);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
