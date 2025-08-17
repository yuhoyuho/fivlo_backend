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
import java.util.stream.Collectors;

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
                        .path("/v2/local/search/address.json")
                        .queryParam("query", query)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .bodyToMono(KakaoAddressResponse.class)
                .block();

        if(kakaoResponse == null || kakaoResponse.documents() == null) {
            return new AddressSearchResponse(Collections.emptyList());
        }

        // 카카오 dto 리스트를 애플리케이션 api dto 리스트로 변환
        List<AddressDto> dto = kakaoResponse.documents().stream()
                .map(document -> new AddressDto(
                        document.addressName(),
                        document.roadAddressName(),
                        new BigDecimal(document.x()),
                        new BigDecimal(document.y())
                ))
                .collect(Collectors.toList());

        return new AddressSearchResponse(dto);
    }
}
