package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.geocoding;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NominatimGeocodingService implements GeocodingService {

    private final RestClient restClient;

    @Override
    public Coordinates geocode(String address) {

        List<NominatimResponse> responses = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("nominatim.openstreetmap.org")
                        .path("/search")
                        .queryParam("q", address)
                        .queryParam("format", "jsonv2")
                        .queryParam("countrycodes", "in")
                        .queryParam("limit", 1)
                        .build())
                .header(
                        "User-Agent",
                        "KisanSuvidha/1.0 (development)"
                )
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});

        if (responses == null || responses.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unable to find coordinates for address: " + address
            );
        }

        NominatimResponse response = responses.getFirst();

        return new Coordinates(
                Double.valueOf(response.lat()),
                Double.valueOf(response.lon())
        );
    }

    private record NominatimResponse(
            String lat,
            String lon
    ) {
    }
}