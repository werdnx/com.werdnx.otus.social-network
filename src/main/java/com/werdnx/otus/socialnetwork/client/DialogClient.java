package com.werdnx.otus.socialnetwork.client;

import com.werdnx.otus.socialnetwork.dto.MessageResponse;
import com.werdnx.otus.socialnetwork.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DialogClient {

    private final RestTemplate restTemplate;

    @Value("${dialog.base-url:http://localhost:8081}")
    private String baseUrl;

    private static final String REQ_ID = "x-request-id";

    public void sendV2(Long userId, SendMessageRequest dto, String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        setCommonHeaders(headers, authorization);

        HttpEntity<SendMessageRequest> entity = new HttpEntity<>(dto, headers);

        restTemplate.exchange(
                baseUrl + "/api/v2/dialogs/{userId}/send",
                HttpMethod.POST,
                entity,
                Void.class,
                userId
        );
    }

    public List<MessageResponse> listV2(Long userId, Long peerId, int limit, String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        setCommonHeaders(headers, authorization);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/v2/dialogs/{userId}/list")
                .queryParam("peerId", peerId)
                .queryParam("limit", limit)
                .buildAndExpand(userId)
                .toUri();

        ResponseEntity<MessageResponse[]> resp = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                MessageResponse[].class
        );

        MessageResponse[] body = resp.getBody();
        return body == null ? List.of() : Arrays.asList(body);
    }

    private void setCommonHeaders(HttpHeaders headers, String authorization) {
        String rid = MDC.get(REQ_ID);
        if (rid != null && !rid.isBlank()) headers.set(REQ_ID, rid);
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }
}
