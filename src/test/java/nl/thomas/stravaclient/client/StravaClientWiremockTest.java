package nl.thomas.stravaclient.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import nl.thomas.strava.model.ActivityType;
import nl.thomas.strava.model.DetailedActivity;
import nl.thomas.strava.model.DetailedAthlete;
import nl.thomas.strava.model.SportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.wiremock.spring.EnableWireMock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static nl.thomas.stravaclient.client.StravaClientTest.EARLIER;
import static nl.thomas.stravaclient.client.StravaClientTest.LATER;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@EnableWireMock
@SpringBootTest
@TestPropertySource(properties = {"strava.base-url=http://localhost:${wiremock.server.port}"})
@ExtendWith(OutputCaptureExtension.class)
class StravaClientWiremockTest {

    @Autowired
    private StravaClient stravaClient;
    @MockitoBean
    private TokenService tokenService;

    @Value("classpath:athlete-sample.json")
    Resource athleteResponsFile;
    @Value("classpath:activities-sample.json")
    Resource activitiesResponsFile;
    @Value("classpath:activity-sample.json")
    Resource activityResponsFile;

    @Test
    void validResponse_correctlyMapped() throws IOException {
        String athleteSampleResponse = athleteResponsFile.getContentAsString(Charset.defaultCharset());
        WireMock.stubFor(get("/athlete").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(athleteSampleResponse)));

        Mono<DetailedAthlete> mono = stravaClient.getDetailedAthlete(mock(OAuth2User.class));
        DetailedAthlete actual = mono.block();

        assertThat(actual.getId()).isEqualTo(5646321L);
        assertThat(actual.getResourceState()).isEqualTo(2);
        assertThat(actual.getFirstname()).isEqualTo("Sifan");
        assertThat(actual.getLastname()).isEqualTo("Hassan");
        assertThat(actual.getProfileMedium()).isEqualTo("https://medium.jpg");
        assertThat(actual.getProfile()).isEqualTo("https://large.jpg");
        assertThat(actual.getCity()).isEqualTo("Utrecht");
        assertThat(actual.getState()).isEmpty();
        assertThat(actual.getCountry()).isEqualTo("The Netherlands");
        assertThat(actual.getSex()).isEqualTo(DetailedAthlete.SexEnum.F);
        assertThat(actual.getPremium()).isFalse();
        assertThat(actual.getSummit()).isFalse();
        assertThat(actual.getCreatedAt()).isEqualTo(ZonedDateTime.parse("2011-10-15T12:37:24Z").toOffsetDateTime());
        assertThat(actual.getUpdatedAt()).isEqualTo(ZonedDateTime.parse("2021-05-10T07:34:14Z").toOffsetDateTime());
        assertThat(actual.getFollowerCount()).isNull();
        assertThat(actual.getFriendCount()).isNull();
        assertThat(actual.getMeasurementPreference()).isNull();
        assertThat(actual.getFtp()).isNull();
        assertThat(actual.getWeight()).isEqualTo(75.0f);
        assertThat(actual.getClubs()).isNull();
        assertThat(actual.getBikes()).isNull();
        assertThat(actual.getShoes()).isNull();
    }

    @Test
    void invalidResponse_exception() {
        WireMock.stubFor(get("/athlete").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("bla")));

        Mono<DetailedAthlete> mono = stravaClient.getDetailedAthlete(mock(OAuth2User.class));

        StepVerifier.create(mono)
                .expectErrorMatches(e ->
                        e.getMessage().equals("JSON decoding error: Unrecognized token 'bla': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')") &&
                                e instanceof DecodingException)
                .verify();
    }

    @Test
    void partialBody_partialObject() {
        WireMock.stubFor(get("/athlete").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                  "id": 5646321,
                                  "resource_state": 2,
                                  "firstname": "Sifan",
                                  "measurement_preference": null,
                                  "ftp": null,
                                  "weight": 75,
                                  "clubs": null,
                                  "bikes": null,
                                  "shoes": null
                                }""")));

        Mono<DetailedAthlete> mono = stravaClient.getDetailedAthlete(mock(OAuth2User.class));
        DetailedAthlete actual = mono.block();

        assertThat(actual.getId()).isEqualTo(5646321L);
        assertThat(actual.getLastname()).isNull();
    }

    @Test
    void getActivities(CapturedOutput output) throws IOException {
        String activitiesSampleResponse = activitiesResponsFile.getContentAsString(StandardCharsets.UTF_8);
        WireMock.stubFor(get("/athlete/activities?after=1747476183&before=1747476303&per_page=10").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(activitiesSampleResponse)));

        Mono<List<DetailedActivity>> detailedActivities = stravaClient.getDetailedActivities(mock(OAuth2User.class), EARLIER, LATER);
        List<DetailedActivity> actual = detailedActivities.block();

        assertThat(output).containsPattern("200 OK response from Strava to GET request http://localhost:[0-9]+/athlete/activities\\?after=1747476183&before=1747476303&per_page=10");
        assertThatList(actual).hasSize(1);
        DetailedActivity detailedActivity = actual.get(0);
        assertThat(detailedActivity.getId()).isEqualTo(1155632529L);
        assertThat(detailedActivity.getExternalId()).isEqualTo("running-20240423195101.tcx");
        assertThat(detailedActivity.getAthlete().getId()).isEqualTo(2523456);
        assertThat(detailedActivity.getName()).isEqualTo("Zomeravondcup");
        assertThat(detailedActivity.getDistance()).isEqualTo(10028.8f);
        assertThat(detailedActivity.getMovingTime()).isEqualTo(2768);
        assertThat(detailedActivity.getElapsedTime()).isEqualTo(2768);
        assertThat(detailedActivity.getTotalElevationGain()).isEqualTo(2.4f);
        assertThat(detailedActivity.getElevHigh()).isEqualTo(4.7f);
        assertThat(detailedActivity.getElevLow()).isEqualTo(2.2f);
        assertThat(detailedActivity.getType()).isEqualTo(ActivityType.RUN);
        assertThat(detailedActivity.getSportType()).isEqualTo(SportType.RUN);
        assertThat(detailedActivity.getStartDate()).isEqualTo("2024-04-23T17:51:02Z");
        assertThat(detailedActivity.getStartDateLocal()).isEqualTo("2024-04-23T19:51:02Z");
        assertThat(detailedActivity.getTimezone()).isEqualTo("(GMT+01:00) Europe/Amsterdam");
        assertThat(detailedActivity.getStartLatlng()).isEqualTo(List.of(52.07869f, 5.1566014f));
        assertThat(detailedActivity.getEndLatlng()).isEqualTo(List.of(52.07906f, 5.1565537f));
        assertThat(detailedActivity.getAchievementCount()).isEqualTo(16);
        assertThat(detailedActivity.getKudosCount()).isEqualTo(6);
        assertThat(detailedActivity.getCommentCount()).isZero();
        assertThat(detailedActivity.getAthleteCount()).isEqualTo(25);
        assertThat(detailedActivity.getPhotoCount()).isZero();
        assertThat(detailedActivity.getTotalPhotoCount()).isZero();
        assertThat(detailedActivity.getMap().getId()).isEqualTo("a1155632529");
        assertThat(detailedActivity.getMap().getPolyline()).isNull();
        assertThat(detailedActivity.getMap().getSummaryPolyline()).startsWith("wrz|Hwcn^MNUJ]?qB_A_@UWWS_@G]E_@Dk@^w@NIRERBlCdAf@d@JZJn@Az@CHSj@SLUH[AaB}@i@UWOQSISE");
        assertThat(detailedActivity.getTrainer()).isFalse();
        assertThat(detailedActivity.getCommute()).isFalse();
        assertThat(detailedActivity.getManual()).isFalse();
        assertThat(detailedActivity.getFlagged()).isFalse();
        assertThat(detailedActivity.getWorkoutType()).isEqualTo(1);
        assertThat(detailedActivity.getUploadIdStr()).isEqualTo("12018543448");
        assertThat(detailedActivity.getAverageSpeed()).isEqualTo(3.623f);
        assertThat(detailedActivity.getMaxSpeed()).isEqualTo(7.128f);
        assertThat(detailedActivity.getHasKudoed()).isFalse();
        assertThat(detailedActivity.getHideFromHome()).isNull();
        assertThat(detailedActivity.getGearId()).isNull();
        assertThat(detailedActivity.getKilojoules()).isNull();
        assertThat(detailedActivity.getAverageWatts()).isNull();
        assertThat(detailedActivity.getDeviceWatts()).isNull();
        assertThat(detailedActivity.getMaxWatts()).isNull();
        assertThat(detailedActivity.getWeightedAverageWatts()).isNull();
        assertThat(detailedActivity.getDescription()).isNull();
        assertThat(detailedActivity.getPhotos()).isNull();
        assertThat(detailedActivity.getGear()).isNull();
        assertThat(detailedActivity.getCalories()).isNull();
        assertThat(detailedActivity.getSegmentEfforts()).isNull();
        assertThat(detailedActivity.getDeviceName()).isNull();
        assertThat(detailedActivity.getEmbedToken()).isNull();
        assertThat(detailedActivity.getSplitsMetric()).isNull();
        assertThat(detailedActivity.getSplitsStandard()).isNull();
        assertThat(detailedActivity.getLaps()).isNull();
        assertThat(detailedActivity.getBestEfforts()).isNull();
        assertThat(detailedActivity.getPrivate()).isFalse();
    }

    @Test
    void validNameChangeRequest_nameChanged(CapturedOutput output) throws IOException {
        String activitySampleResponse = activityResponsFile.getContentAsString(StandardCharsets.UTF_8);
        WireMock.stubFor(get("/activities/123132").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(activitySampleResponse)));
        WireMock.stubFor(put("/activities/123132").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(activitySampleResponse)));

        stravaClient.replaceNameForActivity(mock(OAuth2User.class), 123132L, "new");

        assertThat(output).contains("Replacing name \"Zomeravondcup\" with \"new\" for activity 1155632529");
    }

    @Test
    void nonExistingActivityId_notFound(CapturedOutput output) {
        WireMock.stubFor(get("/activities/123132").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(404)));
        assertThatThrownBy(() -> stravaClient.replaceNameForActivity(mock(OAuth2User.class), 123132L, "new"))
                .isInstanceOf(WebClientResponseException.NotFound.class)
                .hasMessageMatching("404 Not Found from GET http://localhost:[0-9]+/activities/123132");

        assertThat(output).containsPattern("404 NOT_FOUND response from Strava to GET request http://localhost:[0-9]+/activities/123132");
    }
}