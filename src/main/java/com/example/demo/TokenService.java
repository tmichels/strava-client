package com.example.demo;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TokenService {

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    public TokenService(OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
    }

    String getToken(OAuth2User oAuth2User) {
        return oAuth2AuthorizedClientService
                .loadAuthorizedClient("strava", oAuth2User.getName())
                .getAccessToken()
                .getTokenValue();
    }
}
