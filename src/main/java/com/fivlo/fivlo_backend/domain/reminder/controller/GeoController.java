package com.fivlo.fivlo_backend.domain.reminder.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.reminder.dto.AddressSearchResponse;
import com.fivlo.fivlo_backend.domain.reminder.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    /**
     * 주소 검색
     * HTTP : GET
     * EndPoint : /api/v1/geo/search-address
     */
    @GetMapping(Routes.GEO_SEARCH_ADDRESS)
    public ResponseEntity<AddressSearchResponse> searchAddress(@RequestParam("query") String query) {
        return ResponseEntity.ok(geoService.searchAddress(query));
    }
}
