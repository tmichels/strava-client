package nl.thomas.stravaclient.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nl.thomas.strava.model.DetailedActivity;
import nl.thomas.strava.model.DetailedAthlete;
import nl.thomas.strava.model.UpdatableActivity;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;

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

    public Mono<List<DetailedActivity>> getDetailedActivities(
            @NonNull OAuth2User oAuth2User,
            @NonNull ZonedDateTime after,
            @NonNull ZonedDateTime before) {
        throwIfInvalidParameters(after, before);
        String token = tokenService.getToken(oAuth2User);
        Mono<List<DetailedActivity>> mono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("athlete/activities")
                        .queryParam("after", after.toEpochSecond())
                        .queryParam("before", before.toEpochSecond())
                        .queryParam("per_page", 10)
                        .build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToFlux(DetailedActivity.class)
                .collectList();
        mono.subscribe(detailedActivities -> log.info(
                "The following activities were received from Strava: {}",
                detailedActivities.stream().map(DetailedActivity::getId).toList()));
        return mono;
    }

    private void throwIfInvalidParameters(ZonedDateTime after, ZonedDateTime before) {
        if (before.isBefore(after)) {
            throw new IllegalArgumentException("Incorrect parameters: after %s should be earlier than before %s.".formatted(after, before));
        }
    }

    public Mono<DetailedActivity> replaceNameForActivity(
            @NonNull OAuth2User oAuth2User,
            @NonNull Long activityId,
            @NonNull String newName) {
        String token = tokenService.getToken(oAuth2User);
        UpdatableActivity updatableActivity = createUpdatableActivity(activityId, newName, token);
        return webClient.put()
                .uri("activities/{id}", activityId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatableActivity)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToMono(DetailedActivity.class);
    }

    private UpdatableActivity createUpdatableActivity(Long activityId, String newName, String token) {
        DetailedActivity activity = webClient.get()
                .uri("/activities/{activityId}", activityId)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToMono(DetailedActivity.class)
                .block();
        log.info("Replacing name \"{}\" with \"{}\" for activity {}", activity.getName(), newName, activity.getId());
        return new UpdatableActivity()
                .name(newName)
                .commute(activity.getCommute())
                .description(activity.getDescription())
                .type(activity.getType())
                .gearId(activity.getGearId())
                .hideFromHome(activity.getHideFromHome())
                .trainer(activity.getTrainer())
                .sportType(activity.getSportType());
    }
}
