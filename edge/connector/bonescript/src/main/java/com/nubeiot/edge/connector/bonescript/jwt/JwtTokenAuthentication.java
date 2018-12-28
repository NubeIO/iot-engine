package com.nubeiot.edge.connector.bonescript.jwt;

import java.util.function.Function;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class JwtTokenAuthentication<T, R> implements Function<JwtUserPrincipal, JwtUserPrincipal> {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenAuthentication.class);

    @Override
    public JwtUserPrincipal apply(JwtUserPrincipal jwtUserPrincipal) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                                      .withIssuer("auth0")
                                      .build();
            DecodedJWT jwt = verifier.verify(jwtUserPrincipal.getAccessToken());
            jwtUserPrincipal.setAuthorized(true);
            logger.info("Valid token.");
        } catch (JWTVerificationException exception){
            jwtUserPrincipal.setAuthorized(false);
            logger.error("Not a valid token.");
        }

        return jwtUserPrincipal;
    }
}
