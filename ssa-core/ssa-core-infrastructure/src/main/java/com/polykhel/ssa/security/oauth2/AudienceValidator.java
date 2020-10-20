package com.polykhel.ssa.security.oauth2;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Slf4j
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final OAuth2Error error =
        new OAuth2Error("invalid_token", "The required audience is missing", null);
    private final List<String> allowedAudience;

    public AudienceValidator(List<String> allowedAudience) {
        Assert.notEmpty(allowedAudience, "Allowed audience should not be null or empty.");
        this.allowedAudience = allowedAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audience = jwt.getAudience();
        if (audience.stream().anyMatch(allowedAudience::contains)) {
            return OAuth2TokenValidatorResult.success();
        } else {
            log.warn("Invalid audience: {}", audience);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}
