package nl.thomas.stravaclient.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class WebClientConfiguration {

    @Bean
    WebClient webClient(@Value("${strava.baseurl}") @NonNull String stravaBaseUrl) {
        return WebClient.builder()
                .baseUrl(stravaBaseUrl)
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.info("{} request to {}", clientRequest.method(), clientRequest.url());
                    return Mono.just(clientRequest);
                }
        );
    }
}
