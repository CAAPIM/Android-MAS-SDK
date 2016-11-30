/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.ui;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.auth.MASAuthenticationProvider;
import com.ca.mas.foundation.auth.MASSocialLogin;

public class MASSocialLoginFragment extends DialogFragment {
    // The fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String PROVIDER = "PROVIDER";

    private long requestId;
    private MASAuthenticationProvider provider;

    private WebView mWebView;

    public MASSocialLoginFragment() {
        // Required empty public constructor
    }

    public static MASSocialLoginFragment newInstance(long requestId, MASAuthenticationProvider provider) {
        MASSocialLoginFragment fragment = new MASSocialLoginFragment();
        Bundle args = new Bundle();
        args.putLong(REQUEST_ID, requestId);
        args.putParcelable(PROVIDER, provider);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            requestId = getArguments().getLong(REQUEST_ID);
            provider = getArguments().getParcelable(PROVIDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mas_social_login, container, false);
        mWebView = (WebView) view.findViewById(R.id.webView);

        new MASSocialLogin(getActivity(), mWebView, requestId, provider) {
            @Override
            protected void onError(final String msg, Exception e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Social login failed: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
                dismiss();
            }

            @Override
            protected void onAuthCodeReceived(String code) {
                //Fetch the user profile
                MASUser.login(null);
                dismiss();
            }
        };

        // Show progress Bar
        final ProgressBar progressBar;
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }

                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });

        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        MAS.cancelRequest(requestId);
    }
}
