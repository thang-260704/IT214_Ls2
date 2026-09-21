package com.example.seat.service;

import com.example.seat.model.CinemaBookingRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/** Nghiệp vụ giữ ghế (mô phỏng). */
@Service
public class SeatReservationService {

    /** @return chuỗi ghế đã giữ, ví dụ "A12, A13" */
    public String reserveSeats(CinemaBookingRequest request) {
        List<String> seats = request.getSeatNumbers();
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("No seats requested");
        }
        return String.join(", ", seats);
    }
}
