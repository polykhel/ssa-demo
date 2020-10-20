package com.polykhel.ssa.security.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class OAuthIdpTokenResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("token_type")
    private String tokenType;

    private String scope;

    @JsonProperty("ext_expires_in")
    private Long expiresIn;

    @JsonProperty("ext_expires_in")
    private Long extExpiresIn;

    @JsonProperty("expires_on")
    private Long expiresOn;

    @JsonProperty("not-before-policy")
    private Long notBefore;

    private UUID resource;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("session_state")
    private String sessionState;

    @JsonProperty("refresh_expires_in")
    private String refreshExpiresIn;
}
