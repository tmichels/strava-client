package nl.thomas.stravaclient;

import nl.thomas.strava.model.DetailedAthlete;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StravaClient {

    private final WebClient webClient;

    public StravaClient(WebClient webClient) {
        this.webClient = webClient;
    }

    Mono<DetailedAthlete> getDetailedAthlete(String token) {
        return webClient.get()
                .uri("athlete")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToMono(DetailedAthlete.class);
    }
}
