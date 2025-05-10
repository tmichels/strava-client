package nl.thomas.stravaclient;

import lombok.extern.slf4j.Slf4j;
import nl.thomas.strava.model.DetailedAthlete;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class StravaController {

    private final TokenService tokenService;
    private final StravaClient stravaClient;

    public StravaController(TokenService tokenService, StravaClient stravaClient) {
        this.tokenService = tokenService;
        this.stravaClient = stravaClient;
    }

    @GetMapping("athlete")
    public Mono<DetailedAthlete> moetInloggen(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String token = tokenService.getToken(oAuth2User);
        return stravaClient.getDetailedAthlete(token);
    }

}
