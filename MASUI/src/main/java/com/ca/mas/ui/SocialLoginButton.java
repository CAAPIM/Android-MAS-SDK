package com.ca.mas.ui;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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

public class SocialLoginButton extends CardView {

    private int elevationNormal, elevationPressed;
    private ImageView imageView;
    private TextView textView;
    private LinearLayout linearLayout;

    public SocialLoginButton(Context context) {
        this(context, null);
    }

    public SocialLoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SocialLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SocialLoginButton, defStyleAttr, 0);

            // LinearLayout attrs
            final int paddingLeft = (int) a.getDimension(R.styleable.SocialLoginButton_padding_left, 0);
            final int paddingRight = (int) a.getDimension(R.styleable.SocialLoginButton_padding_right, 0);
            linearLayout.setPadding(paddingLeft, 0, paddingRight, 0);

            // Text attrs
            final String text = a.getString(R.styleable.SocialLoginButton_text);
            final int textColor = a.getColor(R.styleable.SocialLoginButton_text_color, textView.getSolidColor());
            final int textSize = a.getDimensionPixelSize(R.styleable.SocialLoginButton_text_size, 12);
            textView.setText(text);
            textView.setTextColor(textColor);
            if (textSize > 0) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }

            // Image attrs
            final float imgWidth = a.getDimension(R.styleable.SocialLoginButton_image_width, 100);
            final float imgHeight = a.getDimension(R.styleable.SocialLoginButton_image_height, 100);
            final Drawable d = a.getDrawable(R.styleable.SocialLoginButton_image_src);
            final float imageMarginRight = a.getDimension(R.styleable.SocialLoginButton_image_margin_right, 0f);
            imageView.setImageDrawable(d);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) imgWidth, (int) imgHeight);
            layoutParams.setMargins(0, 0, (int) imageMarginRight, 0);
            imageView.setLayoutParams(layoutParams);

            a.recycle();
        }
    }

    private void init(Context context) {
        elevationNormal = dpToPx(2);
        elevationPressed = dpToPx(8);
        setClickable(true);
        setCardElevation(elevationNormal);
        setRadius(25);
        setUseCompatPadding(true);

        // Ripple effect
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        setForeground(ContextCompat.getDrawable(context, outValue.resourceId));

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(linearLayoutParams);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        addView(linearLayout);

        // Set up image view
        imageView = new ImageView(context);
        linearLayout.addView(imageView);

        // Set up text view
        textView = new TextView(context);
        linearLayout.addView(textView);
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
