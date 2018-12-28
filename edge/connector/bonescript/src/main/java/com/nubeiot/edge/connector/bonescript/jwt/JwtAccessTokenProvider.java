package com.nubeiot.edge.connector.bonescript.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class JwtAccessTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessTokenProvider.class);

    static String create() {
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            return JWT.create().withIssuer("auth0").sign(algorithm);
        } catch (JWTCreationException exception) {
            logger.info("Error occurred {}", exception.getCause().getMessage());
            throw exception;
        }
    }
}
