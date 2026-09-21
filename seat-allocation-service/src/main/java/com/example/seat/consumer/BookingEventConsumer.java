package com.example.seat.consumer;

import com.example.seat.model.CinemaBookingRequest;
import com.example.seat.producer.SeatConfirmedProducer;
import com.example.seat.service.SeatReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final SeatReservationService seatService;
    private final SeatConfirmedProducer seatConfirmedProducer;
    private final ObjectMapper objectMapper;

    public BookingEventConsumer(SeatReservationService seatService,
                                SeatConfirmedProducer seatConfirmedProducer,
                                ObjectMapper objectMapper) {
        this.seatService = seatService;
        this.seatConfirmedProducer = seatConfirmedProducer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "booking-events", groupId = "seat-group")
    public void handleBooking(ConsumerRecord<String, String> record) {
        // Bước 3: trích xuất correlationId từ HEADER - KHÔNG lấy từ payload
        String correlationId = extractCorrelationId(record);

        log.info("[SeatAllocationService] Received SeatRequest for {}. CorrelationID: {}",
                record.key(), correlationId);

        try {
            CinemaBookingRequest request = objectMapper.readValue(record.value(), CinemaBookingRequest.class);

            // Thực hiện giữ ghế
            String seatResult = seatService.reserveSeats(request);
            log.info("[SeatAllocationService] Seat reserved: {}. CorrelationID: {}",
                    seatResult, correlationId);

            // Gửi sự kiện tiếp theo kèm correlationId trong header
            seatConfirmedProducer.publishSeatConfirmed(request, correlationId);

        } catch (Exception e) {
            log.error("[SeatAllocationService] Error processing booking: {}. CorrelationID: {}",
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
