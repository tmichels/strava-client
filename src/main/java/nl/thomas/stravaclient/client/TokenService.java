package nl.thomas.stravaclient.client;

import lombok.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class TokenService {

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    public TokenService(OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
    }

    String getToken(@NonNull OAuth2User oAuth2User) {
        return oAuth2AuthorizedClientService
                .loadAuthorizedClient("strava", oAuth2User.getName())
                .getAccessToken()
                .getTokenValue();
    }

    public String logout(@NonNull OAuth2User oAuth2User) {
        oAuth2AuthorizedClientService.removeAuthorizedClient("strava", oAuth2User.getName());
        return "Uitgelogd";
    }
}
