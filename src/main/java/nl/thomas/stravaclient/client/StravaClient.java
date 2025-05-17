package nl.thomas.stravaclient.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nl.thomas.strava.model.DetailedActivity;
import nl.thomas.strava.model.DetailedAthlete;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

@Component
@Slf4j
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

    public Flux<DetailedActivity> getDetailedActivities(
            @NonNull OAuth2User oAuth2User,
            @NonNull ZonedDateTime after,
            @NonNull ZonedDateTime before) {
        throwIfInvalidParameters(after, before);
        String token = tokenService.getToken(oAuth2User);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("athlete/activities")
                        .queryParam("after", after.toEpochSecond())
                        .queryParam("before", before.toEpochSecond())
                        .queryParam("per_page", 10)
                        .build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToFlux(DetailedActivity.class);
    }

    private void throwIfInvalidParameters(ZonedDateTime after, ZonedDateTime before) {
        if (before.isBefore(after)) {
            throw new IllegalArgumentException("Incorrect parameters: after %s should be earlier than before %s.".formatted(after, before));
        }
    }

    public String logout(@NonNull OAuth2User oAuth2User) {
        return tokenService.logout(oAuth2User);
    }

}
