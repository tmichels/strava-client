package nl.thomas.stravaclient.controllers;

import lombok.extern.slf4j.Slf4j;
import nl.thomas.strava.model.DetailedAthlete;
import nl.thomas.stravaclient.client.StravaClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class StravaController {

    private final StravaClient stravaClient;

    public StravaController(StravaClient stravaClient) {
        this.stravaClient = stravaClient;
    }

    @GetMapping("athlete")
    public Mono<DetailedAthlete> moetInloggen(@AuthenticationPrincipal OAuth2User oAuth2User) {
        log.info("GET request received at /athlete for user {}", oAuth2User.getName());
        return stravaClient.getDetailedAthlete(oAuth2User);
    }

}
