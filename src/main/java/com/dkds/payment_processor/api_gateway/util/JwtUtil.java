package com.dkds.payment_processor.api_gateway.util;

import com.dkds.payment_processor.api_gateway.exceptions.AuthException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

@Component
public class JwtUtil {

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public JwtUtil(@Value("${jwt.jwk-url}") String jwkUrl, @Value("${jwt.issuer}") String issuer) throws URISyntaxException, MalformedURLException {
        jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("jwt")));
        JWKSource<SecurityContext> keySource = JWKSourceBuilder
                .create(new URI(jwkUrl).toURL())
                .retrying(true)
                .build();
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                expectedJWSAlg,
                keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(issuer).build(),
                new HashSet<>(Arrays.asList(
                        JWTClaimNames.SUBJECT,
                        JWTClaimNames.ISSUED_AT,
                        JWTClaimNames.EXPIRATION_TIME,
                        JWTClaimNames.JWT_ID))));
    }

    public JWTClaimsSet validateToken(String accessToken) {
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, null);
            System.out.println(claimsSet.toJSONObject());
            return claimsSet;
        } catch (ParseException | BadJOSEException e) {
            System.err.println(e.getMessage());
            throw new AuthException();
        } catch (JOSEException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
