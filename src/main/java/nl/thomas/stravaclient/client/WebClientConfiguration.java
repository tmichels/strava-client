package nl.thomas.stravaclient.client;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    WebClient webClient(@Value("${strava.baseurl}") @NonNull String stravaBaseUrl) {
        return WebClient.builder()
                .baseUrl(stravaBaseUrl)
                .build();
    }
}
