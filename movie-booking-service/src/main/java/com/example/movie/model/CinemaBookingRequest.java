package com.example.movie.model;

import java.util.List;

/**
 * Payload yêu cầu đặt vé. LƯU Ý: không có trường correlationId,
 * vì correlationId được truyền qua HEADER của Kafka.
 */
public class CinemaBookingRequest {

    private String cinemaBookingId;
    private String movieCode;
    private String showTime;
    private List<String> seatNumbers;
    private String customerEmail;
    private long totalPrice;

    public CinemaBookingRequest() { }

    public String getCinemaBookingId() { return cinemaBookingId; }
    public void setCinemaBookingId(String cinemaBookingId) { this.cinemaBookingId = cinemaBookingId; }

    public String getMovieCode() { return movieCode; }
    public void setMovieCode(String movieCode) { this.movieCode = movieCode; }

    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }

    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
}
