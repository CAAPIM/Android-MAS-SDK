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
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JwtSignRequest extends MAGRequestProxy {

    private TokenManager mTokenManager;

    public JwtSignRequest(MAGRequest request) {
        this.request = request;
        StorageProvider storageProvider = new StorageProvider(ConfigurationManager.getInstance().getContext());
        mTokenManager = storageProvider.createTokenManager();
    }

    @Override
    public URL getURL() {
        URL r = null;
        URL url = request.getURL();
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
                    r = new URL(uri.getScheme() + "://" + uri.getAuthority() + uri.getPath() + "?payload=" + compactJws);
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
                JWSSigner signer = new RSASSASigner(getPrivateKey());
                JWTClaimsSet.Builder claimBuilder = new JWTClaimsSet.Builder();

                // JWT claims
                // iss
//                claimBuilder.issuer("device://" + {mag-identifier}/{client_id});
//                claimBuilder.issuer("device://");

                // aud
//                for the receiving MAG, e.g. https://mag.ca.com

                ConfigurationManager.getInstance().getConnectedGateway();
                // jti
                // use the UUID

                // exp
                TimeUnit timeUnit = super.getTimeUnit();
                if (timeUnit != null) {
                    long timeOut = TimeUnit.SECONDS.convert(getTimeout(), timeUnit);
                    Date expiryDate = DateUtils.fromSecondsSinceEpoch(timeOut);

                    claimBuilder.expirationTime(expiryDate);
                }

                JWSHeader rs256Header = new JWSHeader(JWSAlgorithm.RS256);
                SignedJWT claimsToken = new SignedJWT(rs256Header, claimBuilder.build());
                claimsToken.sign(signer);

                JWSObject jwsObject = new JWSObject(rs256Header, new Payload(data));
                jwsObject.sign(signer);

                String compactJws = jwsObject.serialize();
                return MAGRequestBody.stringBody(compactJws);
            } catch (IOException | JOSEException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected PrivateKey getPrivateKey() {
        return mTokenManager.getClientPrivateKey();
    }
}
