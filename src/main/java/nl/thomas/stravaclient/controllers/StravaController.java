package nl.thomas.stravaclient.controllers;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nl.thomas.strava.model.DetailedActivity;
import nl.thomas.strava.model.DetailedAthlete;
import nl.thomas.stravaclient.client.StravaClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class StravaController {

    private final StravaClient stravaClient;

    public StravaController(StravaClient stravaClient) {
        this.stravaClient = stravaClient;
    }

    @GetMapping("/athlete")
    public Mono<DetailedAthlete> getCurrentAthlete(@AuthenticationPrincipal @NonNull OAuth2User oAuth2User, @CookieValue("JSESSIONID") String jsessionId) {
        log.info("GET request received at /athlete for user {} with cookie JSESSIONID value {}", oAuth2User.getName(), jsessionId);
        return stravaClient.getDetailedAthlete(oAuth2User);
    }

    @GetMapping("/athlete/activities")
    public Mono<List<DetailedActivity>> getDetailedActivities(
            @AuthenticationPrincipal @NonNull OAuth2User oAuth2User,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
            @RequestParam ZoneId timeZone
    ) {
        ZonedDateTime zonedAfter = ZonedDateTime.of(after, timeZone);
        ZonedDateTime zonedBefore = ZonedDateTime.of(before, timeZone);
        log.info(
                "GET request received at /athlete/activities for user {} with runs between {} and {}",
                oAuth2User.getName(),
                zonedAfter,
                zonedBefore);
        return stravaClient.getDetailedActivities(oAuth2User, zonedAfter, zonedBefore);
    }

    @PutMapping("/activity/name")
    public Mono<DetailedActivity> updateActivity(
            @AuthenticationPrincipal @NonNull OAuth2User oAuth2User,
            @RequestBody Map<String, String> body) {
        Long activityId = getActivityId(body);
        String newName = getNewName(body);
        log.info("PUT request from {} at /activity/name to replace name for activity {} with \"{}\"",
                oAuth2User.getName(),
                activityId,
                newName);
        return stravaClient.replaceNameForActivity(oAuth2User, activityId, newName);
    }

    private Long getActivityId(Map<String, String> body) {
        try {
            return Long.parseLong(body.get("activityId"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ActivityId must be a number");
        }
    }

    private String getNewName(Map<String, String> body) {
        String newName = body.get("newName");
        if (newName == null) {
            throw new IllegalArgumentException("New name cannot be null");
        }
        return newName;
    }

    @ExceptionHandler
    public ResponseEntity<String> handleWebclientException(WebClientResponseException e) {
        log.warn("{} with message \"{}\". Returned {}.",
                e.getClass().getSimpleName(),
                e.getMessage(),
                e.getStatusCode());
        return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(Exception e) {
        log.warn("{} with message \"{}\". Returned {}.",
                e.getClass().getSimpleName(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.name());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
