/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.ui;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.auth.MASApplication;
import com.ca.mas.foundation.MASCallback;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * <p>A simple {@link Fragment} subclass.
 * Use the {@link MASEnterpriseBrowserFragment#newInstance} factory method to
 * create an instance of this fragment.</p>
 * A sample Enterprise Browser dialog as a Fragment.
 */
public class MASEnterpriseBrowserFragment extends DialogFragment {
    public static final String TAG = MASEnterpriseBrowserFragment.class.getCanonicalName();
    public static final String AUTH_URL = "com.ca.mas.AUTH_URL";

    public MASEnterpriseBrowserFragment() {
        // Required empty public constructor
    }

    public static MASEnterpriseBrowserFragment newInstance() {
        MASEnterpriseBrowserFragment fragment = new MASEnterpriseBrowserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mas_enterprise_browser, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        final GridView gridView = (GridView) view.findViewById(R.id.gridview);

        // Provide Application Launcher, launch Native App and Browser App
        MASApplication.setApplicationLauncher(getApplicationLauncher());

        // Retrieve the Enterprise Applications
        MASApplication.retrieveEnterpriseApps(new MASCallback<List<MASApplication>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(List<MASApplication> result) {
                final ImageAdapter imageAdapter = new ImageAdapter(getActivity());
                imageAdapter.setApps(result);
                gridView.setAdapter(imageAdapter);
                gridView.refreshDrawableState();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
        return view;
    }

    /**
     * Adapter for Enterprise Application icons.
     */
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private List<MASApplication> apps;

        public void setApps(List<MASApplication> apps) {
            this.apps = apps;
        }

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            if (apps != null) {
                return apps.size();
            } else {
                return 0;
            }
        }

        public Object getItem(int position) {
            return apps.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
                apps.get(position).renderEnterpriseIcon(imageView);
            } else {
                imageView = (ImageView) convertView;
            }
            return imageView;
        }
    }

    protected MASApplication.MASApplicationLauncher getApplicationLauncher() {
        return new MASApplication.MASApplicationLauncher() {
            @Override
            public void onWebAppLaunch(MASApplication application) {
                if (application.getAuthUrl() != null) {
                    try {
                        if (!(new URL(application.getAuthUrl())).getHost().equals(MASConfiguration.getCurrentConfiguration().getGatewayHostName())) {
                            Log.e(TAG, "This auth url is valid only for the host that has issued the access_token");
                            return;
                        }
                    } catch (MalformedURLException e) {
                        Log.e(TAG, "Invalid auth url", e);
                        return;
                    }
                    Intent intent = new Intent(getActivity(), MASEnterpriseWebApplicationActivity.class);
                    intent.putExtra(AUTH_URL, application.getAuthUrl());
                    getActivity().startActivity(intent);
                }
            }
        };
    }
}
