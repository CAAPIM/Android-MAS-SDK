package com.ca.mas.masauthenticationsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "JASON";
    private Button loginButton;
    private Button logoutButton;
    private Button invokeButton;
    private Button clearButton;
    private TextView resultsView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.loginButton);
        logoutButton = findViewById(R.id.logoutButton);
        invokeButton = findViewById(R.id.invokeAPI);
        clearButton = findViewById(R.id.clearButton);
        resultsView = findViewById(R.id.resultsTextView);
        progressBar = findViewById(R.id.progressBar2);

        resultsView.setText("");
        resultsView.setMovementMethod(new ScrollingMovementMethod());
        progressBar.setVisibility(View.INVISIBLE);

        loginButton.setOnClickListener(new View.OnClickListener(
        ) {
            @Override
            public void onClick(View v) {
                if (MASUser.getCurrentUser() == null) {
                    MASUser.login("spock", "StRonG5^)".toCharArray(), getUserCallback());
                } else {
                    try {
                        resultsView.setText(R.string.log_in_msg);
                        resultsView.append(MASUser.getCurrentUser().getAsJSONObject().toString());
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MASUser.getCurrentUser() != null) {
                    MASUser.getCurrentUser().logout(logoutCallback());
                }
            }
        });

        invokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                try {
                    MAS.invoke(getProductRequest(), apiCallback());
                } catch (Throwable e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultsView.setText("");
            }
        });
    }

    private MASCallback<MASUser> getUserCallback() {
        return new MASCallback<MASUser>() {

            @Override
            public void onSuccess(MASUser user) {
                try {
                    resultsView.setText(R.string.log_in_msg);
                    resultsView.append(MASUser.getCurrentUser().getAsJSONObject().toString(4));
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {
            }
        };
    }

    private MASCallback<Void> logoutCallback() {
        return new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                resultsView.setText(R.string.log_out_msg);
            }

            @Override
            public void onError(Throwable e) {

            }
        };
    }

    private MASCallback<MASResponse<JSONObject>> apiCallback() {
        return new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    resultsView.setText(result.getBody().getContent().toString(4));
                } catch (JSONException e) {
                }
                progressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onError(Throwable e) {
                resultsView.setText(e.toString());
                progressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

    private MASRequest getProductRequest() throws URISyntaxException {
        return new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts"))
                .notifyOnCancel()
                .build();
    }
}
