package nl.thomas.stravaclient.controllers;

import jakarta.servlet.http.Cookie;
import nl.thomas.stravaclient.client.StravaClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ExtendWith(OutputCaptureExtension.class)
class StravaControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    StravaClient stravaClient;

    @Test
    void loggedIn_athleteRequest_clientCalled(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/athlete")
                .with(oidcLogin())
                .cookie(new Cookie("JSESSIONID", "65DDB887A7458EBF6F366E410408D170")));
        assertThat(output).contains("GET request received at /athlete for user user with cookie JSESSIONID value 65DDB887A7458EBF6F366E410408D170");
        verify(stravaClient).getDetailedAthlete(any());
    }

    @Test
    void notLoggedIn_athleteRequest_redirected() throws Exception {
        mockMvc.perform(get("/athlete")).andExpect(status().is3xxRedirection());
    }

    @Test
    void loggedIn_activitiesRequest_clientCalled(CapturedOutput output) throws Exception {
        String url = "/athlete/activities?after=2025-05-01T10:02&before=2025-05-08T10:02&timeZone=Europe/Amsterdam";
        mockMvc.perform(get(url).with(oidcLogin()));
        assertThat(output).contains("GET request received at /athlete/activities for user user with runs between 2025-05-01T10:02+02:00[Europe/Amsterdam] and 2025-05-08T10:02+02:00[Europe/Amsterdam]");
        ZonedDateTime afterExpected = ZonedDateTime.of(LocalDateTime.parse("2025-05-01T10:02"), ZoneId.of("Europe/Amsterdam"));
        ZonedDateTime beforeExpected = ZonedDateTime.of(LocalDateTime.parse("2025-05-08T10:02"), ZoneId.of("Europe/Amsterdam"));
        verify(stravaClient).getDetailedActivities(any(), eq(afterExpected), eq(beforeExpected));
    }

    @Test
    void notLoggedIn_activitiesRequest_redirected() throws Exception {
        String url = "/athlete/activities?after=2025-05-01T10:02&before=2025-05-08T10:02&timeZone=Europe/Amsterdam";
        mockMvc.perform(get(url)).andExpect(status().is3xxRedirection());
    }

    @Test
    void loggedIn_activitiesRequestWithClientException_errorMessage(CapturedOutput output) throws Exception {
        String url = "/athlete/activities?after=2025-05-01T10:02&before=2025-05-08T10:02&timeZone=Europe/Amsterdam";
        IllegalArgumentException exception = new IllegalArgumentException("Foutje");
        when(stravaClient.getDetailedActivities(any(), any(), any())).thenThrow(exception);

        mockMvc.perform(get(url).with(oidcLogin()))
                .andExpect(content().string("Foutje"))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
        assertThat(output).contains("IllegalArgumentException with message \"Foutje\". Returned BAD_REQUEST.");
    }
}