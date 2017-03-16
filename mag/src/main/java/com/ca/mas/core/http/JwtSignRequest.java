package com.ca.mas.core.http;

import android.net.Uri;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.Server;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JwtSignRequest extends MAGRequestProxy {

    public JwtSignRequest(MAGRequest request) {
        this.request = request;
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
                String magId = StorageProvider.getInstance().getTokenManager().getMagIdentifier();
                claimBuilder.issuer("device://" + magId + "/$");

                // TODO: sub: username

                // aud
                Server server = ConfigurationManager.getInstance().getConnectedGateway();
                String gateway = server.getHost();
                claimBuilder.audience(gateway);

                // jti
                UUID uuid = UUID.randomUUID();
                claimBuilder.jwtID(uuid.toString());

                // iat
                long currentTime = System.currentTimeMillis() * 1000;
                Date currentDate = DateUtils.fromSecondsSinceEpoch(currentTime);
                claimBuilder.issueTime(currentDate);

                // exp
                TimeUnit timeUnit = super.getTimeUnit();
                if (timeUnit == null) {
                    timeUnit = TimeUnit.SECONDS;
               }
                long timeOut = TimeUnit.SECONDS.convert(getTimeout(), timeUnit);
                Date expiryDate = DateUtils.fromSecondsSinceEpoch(timeOut + currentTime);
                claimBuilder.expirationTime(expiryDate);


                claimBuilder.claim("content", new String(data));

                JWSHeader rs256Header = new JWSHeader(JWSAlgorithm.RS256);
                SignedJWT claimsToken = new SignedJWT(rs256Header, claimBuilder.build());
                claimsToken.sign(signer);

                //JWSObject jwsObject = new JWSObject(rs256Header, new Payload(claimsToken));

                String compactJws = claimsToken.serialize();
                return MAGRequestBody.stringBody(compactJws);
            } catch (IOException | JOSEException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected PrivateKey getPrivateKey() {
        return StorageProvider.getInstance().getTokenManager().getClientPrivateKey();
    }


}
