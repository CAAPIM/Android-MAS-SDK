/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui.otp;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.ui.R;
import com.ca.mas.ui.listener.MASErrorMessageListener;
import com.ca.mas.ui.listener.MASFragmentAttachDismissListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A sample dialog to select OTP delivery channels, send an OTP over these channels,
 * and verify its contents over the gateway.
 */
public class MASOtpDialogFragment extends DialogFragment {
    private static final String HANDLER = MASOtpDialogFragment.class.getSimpleName() + "handler";
    private static final String LOGO_ID = MASOtpDialogFragment.class.getSimpleName() +  "logoId";
    private static final String LOGO_IMAGE = MASOtpDialogFragment.class.getSimpleName() +  "logoImage";

    private boolean mIsRequestProcessing = false;
    private String mErrorMessage = "Request cancelled.";

    private MASOtpAuthenticationHandler mHandler;

    private AlertDialog mDialog;
    private LinearLayout mChannelsContainer;
    private TextInputLayout mOtpTextInputLayout;
    private TextInputEditText mOtpTextInputEditText;
    private CheckBox[] mCheckBoxes;
    private ContentLoadingProgressBar mProgressBar;

    private List<String> mChannels;

    /**
     * Creates a dialog with the default CA logo.
     * @param handler The authentication handler
     * @return A dialog
     */
    public static MASOtpDialogFragment newInstance(MASOtpAuthenticationHandler handler) {
        Bundle args = new Bundle();
        args.putParcelable(HANDLER, handler);
        return createFragment(args);
    }

    /**
     * Creates a dialog with a custom logo from a Bitmap.
     * @param handler The authentication handler
     * @param logo A bitmap logo
     * @return A dialog
     */
    public static MASOtpDialogFragment newInstance(MASOtpAuthenticationHandler handler, Bitmap logo) {
        Bundle args = new Bundle();
        args.putParcelable(HANDLER, handler);
        args.putParcelable(LOGO_IMAGE, logo);
        return createFragment(args);
    }

    /**
     * Creates a dialog with a custom logo from a drawable resource ID.
     * @param handler The authentication handler
     * @param logoRes A drawable resource for the logo
     * @return A dialog
     */
    public static MASOtpDialogFragment newInstance(MASOtpAuthenticationHandler handler, @DrawableRes int logoRes) {
        Bundle args = new Bundle();
        args.putParcelable(HANDLER, handler);
        args.putInt(LOGO_ID, logoRes);
        return createFragment(args);
    }

    private static MASOtpDialogFragment createFragment(Bundle args) {
        MASOtpDialogFragment fragment = new MASOtpDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MASOtpDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        if (getArguments() != null) {
            mHandler = getArguments().getParcelable(HANDLER);
            if (mHandler != null && mHandler.isInvalidOtp()) {
                mHandler = getArguments().getParcelable(HANDLER);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_otp, null);

        mChannelsContainer = (LinearLayout) view.findViewById(R.id.channelsContainer);
        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.progressBar);
        ImageView logoView = (ImageView) view.findViewById(R.id.logoImageView);
        mOtpTextInputLayout = (TextInputLayout) view.findViewById(R.id.otpTextInputLayout);
        mOtpTextInputEditText = (TextInputEditText) view.findViewById(R.id.otpEditText);

        // Custom logo setup
        boolean invalidOtp = mHandler.isInvalidOtp();
        Bundle args = getArguments();
        if (args != null) {
            Parcelable mLogoBitmap = args.getParcelable(LOGO_IMAGE);
            if (mLogoBitmap != null && mLogoBitmap instanceof Bitmap) {
                logoView.setImageDrawable(new BitmapDrawable(getResources(), (Bitmap) mLogoBitmap));
            } else {
                int logoRes = args.getInt(LOGO_ID);
                if (logoRes != 0) {
                    logoView.setImageResource(logoRes);
                }
            }
        }

        mChannels = mHandler.getChannels();
        if (invalidOtp) {
            mChannelsContainer.setVisibility(View.GONE);
            mOtpTextInputLayout.setVisibility(View.VISIBLE);
            mOtpTextInputLayout.setError("Authentication failed due to an invalid OTP.");
        } else {
            mCheckBoxes = new CheckBox[mChannels.size()];
            if (mChannels == null || mChannels.isEmpty()) {
                mChannels = new ArrayList<>();
                mChannels.add("Email");
            } else {
                for (int i = 0; i < mChannels.size(); i++) {
                    CheckBox cb = new CheckBox(getActivity());
                    cb.setText(mChannels.get(i));
                    cb.setTextSize(16);
                    mCheckBoxes[i] = cb;
                    mChannelsContainer.addView(cb, i);
                }
            }
        }

        builder.setView(view)
                .setTitle(R.string.select_delivery_channel)
                // Will set in onStart() instead
                .setPositiveButton(invalidOtp ? R.string.verify_otp : R.string.request_otp, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelDialog(mHandler);
                    }
                });

        mDialog = builder.create();
        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            // We override this in onStart() due to AlertDialog dismissing on positive button clicks
            Button positiveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
            if (!mHandler.isInvalidOtp()) {
                positiveButton.setOnClickListener(sendOtpListener(mCheckBoxes, mChannels));
            } else {
                positiveButton.setOnClickListener(verifyOtpListener());
                positiveButton.setEnabled(false);
                mOtpTextInputEditText.addTextChangedListener(otpTextWatcher(positiveButton));
            }
        }
    }

    public View.OnClickListener sendOtpListener(final CheckBox[] checkboxes, final List<String> channels) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String channel = "";
                mIsRequestProcessing = true;
                if (channels != null) {
                    if (channels.size() == 1) {
                        channel = channels.get(0);
                    } else {
                        for (CheckBox checkbox : checkboxes) {
                            if (checkbox.isChecked()) {
                                String text = checkbox.getText().toString();
                                channel = channel.isEmpty() ? text : channel + "," + text;
                            }
                        }
                    }
                }

                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.show();
                mChannelsContainer.setVisibility(View.GONE);

                mHandler.deliver(channel, sendOtpCallback());
            }
        };
    }

    public MASCallback<Void> sendOtpCallback() {
        return new MASCallback<Void>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(Void result) {
                mChannelsContainer.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mOtpTextInputLayout.setVisibility(View.VISIBLE);

                final Button positiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setText(R.string.verify_otp);
                positiveButton.setEnabled(false);
                positiveButton.setOnClickListener(verifyOtpListener());

                mOtpTextInputEditText.addTextChangedListener(otpTextWatcher(positiveButton));
            }

            @Override
            public void onError(Throwable e) {
                mIsRequestProcessing = false;
                Activity activity = getActivity();
                if (activity instanceof MASErrorMessageListener) {
                    String errorMessage = "OTP delivery failure";
                    if (e instanceof MAGServerException) {
                        int errorCode = ((MAGServerException) e).getErrorCode();
                        String errorCodeString = Integer.toString(errorCode);
                        if (errorCodeString.endsWith("140")) {
                            errorMessage = getResources().getString(R.string.errorCode140);
                        } else if (errorCodeString.endsWith("142")) {
                            errorMessage = getResources().getString(R.string.errorCode142);
                        } else if (errorCodeString.endsWith("143")) {
                            errorMessage = getResources().getString(R.string.errorCode143);
                        } else if (errorCodeString.endsWith("144")) {
                            errorMessage = getResources().getString(R.string.errorCode144);
                        } else if (errorCodeString.endsWith("145")) {
                            errorMessage = getResources().getString(R.string.errorCode145);
                        }
                    }
                    ((MASErrorMessageListener) activity).getErrorMessage(errorMessage);
                }
                mHandler.cancel();
                dismiss();
            }
        };
    }

    private TextWatcher otpTextWatcher(final Button positiveButton) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                positiveButton.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    public View.OnClickListener verifyOtpListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsRequestProcessing = true;
                String otp = mOtpTextInputEditText.getText().toString();
                mProgressBar.setVisibility(View.VISIBLE);

                mHandler.proceed(getActivity().getBaseContext(), otp);
                mErrorMessage = "Processing Request";
                dismiss();
            }
        };
    }

    public void cancelDialog(MASOtpAuthenticationHandler handler) {
        handler.cancel();
        mIsRequestProcessing = false;
        dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof MASFragmentAttachDismissListener) {
            ((MASFragmentAttachDismissListener) activity).handleDialogOpen(activity);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity instanceof MASFragmentAttachDismissListener) {
            ((MASFragmentAttachDismissListener) activity).handleDialogClose(activity, mIsRequestProcessing);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof MASFragmentAttachDismissListener)
            ((MASFragmentAttachDismissListener) activity).handleDialogClose(activity, mIsRequestProcessing);
        if (activity instanceof MASErrorMessageListener)
            ((MASErrorMessageListener) activity).getErrorMessage(mErrorMessage);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mHandler.cancel();
    }
}
