package nl.thomas.stravaclient.client;

import nl.thomas.strava.model.DetailedAthlete;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StravaClient {

    private final WebClient webClient;
    private final TokenService tokenService;

    public StravaClient(WebClient webClient, TokenService tokenService) {
        this.webClient = webClient;
        this.tokenService = tokenService;
    }

    public Mono<DetailedAthlete> getDetailedAthlete(OAuth2User oAuth2User) {
        String token = tokenService.getToken(oAuth2User);
        return webClient.get()
                .uri("athlete")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToMono(DetailedAthlete.class);
    }
}
