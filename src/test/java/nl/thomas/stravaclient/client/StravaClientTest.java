package nl.thomas.stravaclient.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StravaClientTest {

    @InjectMocks
    private StravaClient stravaClient;
    @Mock
    TokenService tokenService;
    @Mock
    WebClient webClient;

    public static final ZonedDateTime EARLIER = ZonedDateTime.of(LocalDateTime.of(2025, 5, 17, 12, 3, 3), ZoneId.of("Europe/Amsterdam"));
    public static final ZonedDateTime LATER = ZonedDateTime.of(LocalDateTime.of(2025, 5, 17, 12, 5, 3), ZoneId.of("Europe/Amsterdam"));

    @Test
    void beforeIsAfter_getActivities_exception() {
        assertThatThrownBy(() -> stravaClient.getDetailedActivities(mock(OAuth2User.class), LATER, EARLIER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Incorrect parameters: after 2025-05-17T12:05:03+02:00[Europe/Amsterdam] should be earlier than before 2025-05-17T12:03:03+02:00[Europe/Amsterdam].");
    }
}