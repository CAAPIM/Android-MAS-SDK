package com.ca.mas.core.http;

import android.net.Uri;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.request.internal.MAGRequestProxy;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;

public class JwtSignRequest extends MAGRequestProxy {

    public JwtSignRequest(MAGRequest request) {
        this.request = request;
    }

    @Override
    public URL getURL() {
        URL url = request.getURL();
        URL r = null;
        if (url != null) {
            try {
                if (url.getQuery() != null) {

                    Uri uri = Uri.parse(url.toString());
                    JSONObject parameter = new JSONObject();
                    for (String n : uri.getQueryParameterNames()) {
                        JSONArray v = new JSONArray(uri.getQueryParameters(n));
                        parameter.put(n, v);
                    }

                    JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256),
                            new Payload(parameter.toString()));
                    jwsObject.sign(new RSASSASigner(getPrivateKey()));
                    String compactJws = jwsObject.serialize();
                    r = new URL(uri.getScheme() +"://" + uri.getAuthority()+ uri.getPath() + "?payload=" + compactJws);
                } else {
                    return url;
                }
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            } catch (JOSEException | JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return r;
    }

    @Override
    public MAGRequestBody getBody() {
        MAGRequestBody body = super.getBody();
        if (body != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                body.write(baos);
                byte[] data = baos.toByteArray();
                JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256),
                        new Payload(data));
                jwsObject.sign(new RSASSASigner(getPrivateKey()));
                String compactJws = jwsObject.serialize();
                return MAGRequestBody.stringBody(compactJws);

            } catch (IOException | JOSEException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected PrivateKey getPrivateKey() {
        StorageProvider storageProvider = new StorageProvider(ConfigurationManager.getInstance().getContext());
        TokenManager tokenManager = storageProvider.createTokenManager();
        return tokenManager.getClientPrivateKey();
    }
}
