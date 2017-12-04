/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.ca.mas.foundation.MAS;

public class MASFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast toast = Toast.makeText(this, getString(R.string.request_cancel), Toast.LENGTH_LONG);
        toast.show();
        MAS.cancelAllRequests();
        finish();
    }
}
