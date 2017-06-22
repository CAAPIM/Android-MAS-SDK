package com.ca.mas.masmessagingsample.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ca.mas.masmessagingsample.R;

import pl.droidsonroids.gif.GifImageView;

public class MessagingLaunch extends AppCompatActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_launch);
        mContext = this;
        GifImageView givImageView = (GifImageView) findViewById(R.id.blinking_animation);
        givImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, GroupListActivity.class);
                startActivity(intent);
            }
        });
    }
}
