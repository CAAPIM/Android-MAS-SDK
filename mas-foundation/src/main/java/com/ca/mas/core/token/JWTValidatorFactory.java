package com.ca.mas.core.token;

import com.ca.mas.core.error.MAGErrorCode;

public class JWTValidatorFactory {


    private JWTValidatorFactory() {
    }

    public static JWTValidator getValidator(String algorithm) throws JWTValidationException {

        JWTValidator jwtValidator = null;
        if (algorithm.equals(Algorithm.HS256.toString())) {
            jwtValidator = new JWTHmacValidator();
        } else if (algorithm.equals(Algorithm.RS256.toString())) {
            jwtValidator = new JWTRS256Validator();
        } else {
            throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN);
        }

        return jwtValidator;
    }

    public enum Algorithm {
        HS256(1), RSA(2), RS256(3);
        private int value;

        Algorithm(int value) {
            this.value = value;
        }
    }


}
