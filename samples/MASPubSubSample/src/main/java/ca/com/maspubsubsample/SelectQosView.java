/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package ca.com.maspubsubsample;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectQosView extends LinearLayout {

    AppCompatSpinner spinner;

    public SelectQosView(Context context) {
        super(context);
    }

    public SelectQosView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectQosView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOrientation(HORIZONTAL);

        TextView textView = new TextView(getContext());
        textView.setText(getContext().getResources().getString(R.string.qos_spinner_title));
        textView.setTextSize(20);
        addView(textView);


        spinner = new AppCompatSpinner(getContext());
        spinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.qos_spinner_item,
                getResources().getStringArray(R.array.qos_options)));
        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinner.setLayoutParams(layoutParams);
        addView(spinner);
    }

    public Integer getSelectedQos() {
        return Integer.parseInt((String) spinner.getSelectedItem());
    }
}
