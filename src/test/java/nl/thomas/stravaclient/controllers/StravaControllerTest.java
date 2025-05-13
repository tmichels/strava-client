package nl.thomas.stravaclient.controllers;

import nl.thomas.stravaclient.client.StravaClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class StravaControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    StravaClient stravaClient;

    @Test
    void loggedIn_clientCalled() throws Exception {
        mockMvc.perform(get("/athlete").with(oidcLogin()));
        verify(stravaClient).getDetailedAthlete(any());
    }

    @Test
    void notLoggedIn_redirected() throws Exception {
        mockMvc.perform(get("/athlete")).andExpect(status().is3xxRedirection());
    }
}