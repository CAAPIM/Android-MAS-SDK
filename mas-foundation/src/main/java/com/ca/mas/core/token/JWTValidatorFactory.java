package com.ca.mas.core.token;

public class JWTValidatorFactory {



    private JWTValidatorFactory(){}

    public enum Algorithm {
        HS256(1), RSA(2), RS256(3);
        private int value;

        private Algorithm(int value) {
            this.value = value;
        }
    }

    public static JWTValidator getValidator(String algorithm){

        JWTValidator jwtValidator = null;
        if(algorithm.equals(Algorithm.HS256.toString())){
             jwtValidator =  new JWTHmacValidator();
        }
        if (algorithm.equals(Algorithm.RS256.toString())){
            jwtValidator = new JWTRS256Validator();
        }

        return jwtValidator;
    }


}
