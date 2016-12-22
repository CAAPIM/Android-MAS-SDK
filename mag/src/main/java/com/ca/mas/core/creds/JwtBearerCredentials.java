package com.ca.mas.core.creds;

import android.os.Parcel;
import android.util.Pair;

import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.MssoContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtBearerCredentials implements Credentials {

    private volatile char[] idToken;

    public JwtBearerCredentials(char[] idToken) {
        this.idToken = idToken;
    }

    @Override
    public void clear() {
        char[] p = idToken;
        this.idToken = null;
        if (p != null)
            Arrays.fill(p, 'X');
    }

    @Override
    public boolean isValid() {
        return idToken != null;
    }

    @Override
    public Map<String, List<String>> getHeaders(MssoContext context) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("id-token", Collections.singletonList(new String(idToken)));
        headers.put("id-token-type", Collections.singletonList(getGrantType()));
        return headers;
    }

    @Override
    public List<Pair<String, String>> getParams(MssoContext context) {
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>(ServerClient.ASSERTION, new String(idToken)));
        return params;
    }

    @Override
    public String getGrantType() {
        return "urn:ietf:params:oauth:grant-type:jwt-bearer";
    }

    @Override
    public String getUsername() {
        return "idToken";
    }

    @Override
    public boolean isReuseable() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharArray(this.idToken);
    }

    private JwtBearerCredentials(Parcel in) {
        this.idToken = in.createCharArray();
    }

    public static final Creator<JwtBearerCredentials> CREATOR = new Creator<JwtBearerCredentials>() {
        @Override
        public JwtBearerCredentials createFromParcel(Parcel source) {
            return new JwtBearerCredentials(source);
        }

        @Override
        public JwtBearerCredentials[] newArray(int size) {
            return new JwtBearerCredentials[size];
        }
    };
}
