package com.example.payment.service;

import com.example.payment.model.SeatAllocationEvent;
import org.springframework.stereotype.Service;

/** Nghiệp vụ thanh toán (mô phỏng). */
@Service
public class PaymentProcessingService {

    /** @return true nếu thanh toán thành công */
    public boolean processPayment(SeatAllocationEvent event) {
        return event.getTotalPrice() > 0;
    }
}
