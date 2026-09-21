package com.example.movie.controller;

import com.example.movie.model.CinemaBookingRequest;
import com.example.movie.service.BookingPublisherService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class MovieBookingController {

    private final BookingPublisherService publisherService;

    public MovieBookingController(BookingPublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody CinemaBookingRequest request) {
        String correlationId = publisherService.createBooking(request);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ACCEPTED");
        body.put("cinemaBookingId", request.getCinemaBookingId());
        body.put("correlationId", correlationId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Correlation-Id", correlationId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).headers(headers).body(body);
    }
}
