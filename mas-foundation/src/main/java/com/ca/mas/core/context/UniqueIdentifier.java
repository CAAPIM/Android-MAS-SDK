package com.ca.mas.core.context;

import com.ca.mas.core.io.IoUtils;
import com.ca.mas.core.security.KeyStoreException;
import com.ca.mas.core.util.KeyUtilsAsymmetric;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public abstract class UniqueIdentifier {

    protected String identifier = "";

    /**
     * Generates a set of asymmetric keys in the Android keystore and builds the device identifier off of the public key.
     * Apps built with the same sharedUserId value in AndroidManifest.xml will reuse the same identifier.
     */
    public UniqueIdentifier() throws KeyStoreException, NoSuchAlgorithmException {
        String identifierKey = getIdentifierKey();
        PublicKey publicKey = KeyUtilsAsymmetric.getRsaPublicKey(identifierKey);
        if (publicKey == null) {
            KeyUtilsAsymmetric.generateRsaPrivateKey(  identifierKey,
                    String.format("CN=%s, OU=%s", identifierKey, "com.ca"),
                    false, false, Integer.MAX_VALUE, false);
            publicKey = KeyUtilsAsymmetric.getRsaPublicKey(identifierKey);
        }

        //Convert the public key to a hash string
        byte[] encoded = publicKey.getEncoded();

        //Encode to SHA-256 and then convert to a hex string
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(encoded);
        byte[] mdBytes = md.digest();
        identifier = IoUtils.hexDump(mdBytes);
    }

    @Override
    public String toString() {
        return identifier;
    }

    protected abstract String getIdentifierKey();

}
