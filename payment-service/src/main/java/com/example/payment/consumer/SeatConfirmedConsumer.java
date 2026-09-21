package com.example.payment.consumer;

import com.example.payment.model.PaymentEvent;
import com.example.payment.model.SeatAllocationEvent;
import com.example.payment.producer.PaymentResultProducer;
import com.example.payment.service.PaymentProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SeatConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeatConfirmedConsumer.class);

    private final PaymentProcessingService paymentService;
    private final PaymentResultProducer resultProducer;
    private final ObjectMapper objectMapper;

    public SeatConfirmedConsumer(PaymentProcessingService paymentService,
                                 PaymentResultProducer resultProducer,
                                 ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.resultProducer = resultProducer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "seat-confirmed-events", groupId = "payment-group")
    public void handleSeatConfirmed(ConsumerRecord<String, String> record) {
        // Trích xuất correlationId từ HEADER
        String correlationId = extractCorrelationId(record);

        log.info("[PaymentService] Processing Payment for {}. CorrelationID: {}",
                record.key(), correlationId);

        try {
            SeatAllocationEvent event = objectMapper.readValue(record.value(), SeatAllocationEvent.class);
            boolean success = paymentService.processPayment(event);

            if (success) {
                log.info("[PaymentService] Payment success: {} VND. CorrelationID: {}",
                        event.getTotalPrice(), correlationId);
            } else {
                log.warn("[PaymentService] Payment failed for {}. CorrelationID: {}",
                        event.getCinemaBookingId(), correlationId);
            }

            // Gửi sự kiện kết quả (giữ nguyên correlationId ở header)
            resultProducer.publish(new PaymentEvent(
                    event.getCinemaBookingId(), event.getCustomerEmail(), event.getTotalPrice(),
                    success ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED"), correlationId);

        } catch (Exception e) {
            log.error("[PaymentService] Error processing payment: {}. CorrelationID: {}",
                    e.getMessage(), correlationId);
        }
    }

    private String extractCorrelationId(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("correlationId");
        if (header == null || header.value() == null) {
            return "N/A";
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
