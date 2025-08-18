package com.fivlo.fivlo_backend.domain.reminder.service;

import com.fivlo.fivlo_backend.domain.reminder.dto.AddressDto;
import com.fivlo.fivlo_backend.domain.reminder.dto.AddressSearchResponse;
import com.fivlo.fivlo_backend.domain.reminder.dto.KakaoAddressResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class GeoService {

    private final WebClient webClient;
    private final String kakaoApiKey;

    public GeoService(WebClient.Builder webClientBuilder,
                      @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String kakaoApiKey) {
        this.webClient = webClientBuilder.baseUrl("https://dapi.kakao.com").build();
        this.kakaoApiKey = kakaoApiKey;
    }

    public AddressSearchResponse searchAddress(String query) {
        KakaoAddressResponse kakaoResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .bodyToMono(KakaoAddressResponse.class)
                .block();

        if(kakaoResponse == null || kakaoResponse.documents() == null) {
            return new AddressSearchResponse(Collections.emptyList());
        }

        // 응답으로 받는 여러 결과 중 가장 첫번째 응답만 선택
        var firstDocument = kakaoResponse.documents().get(0);

        // 첫번째 결과만 dto로 변환
        AddressDto dto = new AddressDto(
                firstDocument.addressName(),
                firstDocument.roadAddressName(),
                new BigDecimal(firstDocument.x()),
                new BigDecimal(firstDocument.y())
        );

        return new AddressSearchResponse(List.of(dto));
    }
}
