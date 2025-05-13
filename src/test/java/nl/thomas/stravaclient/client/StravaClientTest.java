package nl.thomas.stravaclient.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import nl.thomas.strava.model.DetailedAthlete;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.codec.DecodingException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.EnableWireMock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@EnableWireMock
@SpringBootTest
@TestPropertySource(properties = {"strava.base-url=http://localhost:${wiremock.server.port}"})
class StravaClientTest {

    @Autowired
    private StravaClient stravaClient;
    @MockitoBean
    private TokenService tokenService;

    @Test
    void validResponse_correctlyMapped() {
        WireMock.stubFor(get("/athlete").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                  "id": 5646321,
                                  "resource_state": 2,
                                  "firstname": "Sifan",
                                  "lastname": "Hassan",
                                  "profile_medium": "https://medium.jpg",
                                  "profile": "https://large.jpg",
                                  "city": "Utrecht",
                                  "state": "",
                                  "country": "The Netherlands",
                                  "sex": "F",
                                  "premium": false,
                                  "summit": false,
                                  "created_at": "2011-10-15T12:37:24Z",
                                  "updated_at": "2021-05-10T07:34:14Z",
                                  "follower_count": null,
                                  "friend_count": null,
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
        assertThat(actual.getResourceState()).isEqualTo(2);
        assertThat(actual.getFirstname()).isEqualTo("Sifan");
        assertThat(actual.getLastname()).isEqualTo("Hassan");
        assertThat(actual.getProfileMedium()).isEqualTo("https://medium.jpg");
        assertThat(actual.getProfile()).isEqualTo("https://large.jpg");
        assertThat(actual.getCity()).isEqualTo("Utrecht");
        assertThat(actual.getState()).isEqualTo("");
        assertThat(actual.getCountry()).isEqualTo("The Netherlands");
        assertThat(actual.getSex()).isEqualTo(DetailedAthlete.SexEnum.F);
        assertThat(actual.getPremium()).isEqualTo(false);
        assertThat(actual.getSummit()).isEqualTo(false);
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

}