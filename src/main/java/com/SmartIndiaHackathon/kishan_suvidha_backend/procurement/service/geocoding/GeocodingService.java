package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.geocoding;


public interface GeocodingService {

    Coordinates geocode(String address);
}