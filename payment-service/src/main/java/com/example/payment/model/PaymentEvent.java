package com.example.payment.model;

/** Sự kiện kết quả thanh toán (PAYMENT_SUCCESS / PAYMENT_FAILED). */
public class PaymentEvent {

    private String cinemaBookingId;
    private String customerEmail;
    private long amount;
    private String status;

    public PaymentEvent() { }

    public PaymentEvent(String cinemaBookingId, String customerEmail, long amount, String status) {
        this.cinemaBookingId = cinemaBookingId;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.status = status;
    }

    public String getCinemaBookingId() { return cinemaBookingId; }
    public void setCinemaBookingId(String cinemaBookingId) { this.cinemaBookingId = cinemaBookingId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
