package com.polykhel.ssa.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.polykhel.ssa.utils.constants.ProfileConstants.PROFILE_OAUTH2;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.ACCESS_DENIED;

@Component
@Profile(PROFILE_OAUTH2)
@Slf4j
public class AuthorizationHeaderUtil {

    private static final OAuth2Error tokenIsExpired =
        new OAuth2Error(ACCESS_DENIED, "The token is expired", null);
    private final OAuth2AuthorizedClientService clientService;
    private final RestTemplateBuilder restTemplateBuilder;

    public AuthorizationHeaderUtil(
        OAuth2AuthorizedClientService clientService, RestTemplateBuilder restTemplateBuilder) {
        this.clientService = clientService;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public Optional<String> getAuthorizationHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String name = oauthToken.getName();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(registrationId, name);

        if (client == null) {
            throw new OAuth2AuthorizationException(tokenIsExpired);
        }
        OAuth2AccessToken accessToken = client.getAccessToken();

        if (accessToken != null) {
            String tokenType = accessToken.getTokenType().getValue();
            String accessTokenValue = accessToken.getTokenValue();
            if (isExpired(accessToken)) {
                log.info("AccessToken expired, refreshing automatically");
                accessTokenValue = refreshToken(client, oauthToken);
                if (accessTokenValue == null) {
                    SecurityContextHolder.getContext().setAuthentication(null);
                    throw new OAuth2AuthorizationException(tokenIsExpired);
                }
            }
            String authorizationHeaderValue = String.format("%s %s", tokenType, accessTokenValue);
            return Optional.of(authorizationHeaderValue);
        }
        return Optional.empty();
    }

    private String refreshToken(OAuth2AuthorizedClient client, OAuth2AuthenticationToken oauthToken) {
        OAuth2AccessTokenResponse atr = refreshTokenClient(client);
        if (atr == null || atr.getAccessToken() == null) {
            log.info("Failed to refresh token for user");
            return null;
        }

        OAuth2RefreshToken refreshToken =
            ObjectUtils.defaultIfNull(atr.getRefreshToken(), client.getRefreshToken());
        OAuth2AuthorizedClient updatedClient =
            new OAuth2AuthorizedClient(
                client.getClientRegistration(),
                client.getPrincipalName(),
                atr.getAccessToken(),
                refreshToken);

        clientService.saveAuthorizedClient(updatedClient, oauthToken);
        return atr.getAccessToken().getTokenValue();
    }

    private OAuth2AccessTokenResponse refreshTokenClient(OAuth2AuthorizedClient currentClient) {
        try {
            MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
            formParameters.add(
                OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.REFRESH_TOKEN.getValue());
            formParameters.add(
                OAuth2ParameterNames.REFRESH_TOKEN,
                Objects.requireNonNull(currentClient.getRefreshToken()).getTokenValue());
            formParameters.add(
                OAuth2ParameterNames.CLIENT_ID, currentClient.getClientRegistration().getClientId());
            RequestEntity<MultiValueMap<String, String>> requestEntity =
                RequestEntity.post(
                    URI.create(
                        currentClient.getClientRegistration().getProviderDetails().getTokenUri()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formParameters);
            RestTemplate r =
                restTemplate(
                    currentClient.getClientRegistration().getClientId(),
                    currentClient.getClientRegistration().getClientSecret());
            ResponseEntity<OAuthIdpTokenResponseDTO> responseEntity =
                r.exchange(requestEntity, OAuthIdpTokenResponseDTO.class);
            return toAuth2AccessTokenResponse(Objects.requireNonNull(responseEntity.getBody()));
        } catch (OAuth2AuthorizationException e) {
            log.error("Unable to refresh token", e);
            throw new OAuth2AuthenticationException(e.getError(), e);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private OAuth2AccessTokenResponse toAuth2AccessTokenResponse(
        OAuthIdpTokenResponseDTO oAuthIdpResponse) {
        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("id_token", oAuthIdpResponse.getIdToken());
        additionalParameters.put("not-before-policy", oAuthIdpResponse.getNotBefore());
        additionalParameters.put("refresh_expires_in", oAuthIdpResponse.getRefreshToken());
        additionalParameters.put("session_state", oAuthIdpResponse.getSessionState());
        return OAuth2AccessTokenResponse.withToken(oAuthIdpResponse.getAccessToken())
            .expiresIn(oAuthIdpResponse.getExpiresIn())
            .refreshToken(oAuthIdpResponse.getRefreshToken())
            .scopes(
                Pattern.compile("\\s")
                    .splitAsStream(oAuthIdpResponse.getScope())
                    .collect(Collectors.toSet()))
            .tokenType(OAuth2AccessToken.TokenType.BEARER)
            .additionalParameters(additionalParameters)
            .build();
    }

    private RestTemplate restTemplate(String clientId, String clientSecret) {
        return restTemplateBuilder
            .additionalMessageConverters(
                new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter())
            .errorHandler(new OAuth2ErrorResponseErrorHandler())
            .basicAuthentication(clientId, clientSecret)
            .build();
    }

    private boolean isExpired(OAuth2AccessToken accessToken) {
        Instant now = Instant.now();
        Instant expiresAt = Objects.requireNonNull(accessToken.getExpiresAt());
        return now.isAfter(expiresAt.minus(Duration.ofMinutes(1L)));
    }
}
