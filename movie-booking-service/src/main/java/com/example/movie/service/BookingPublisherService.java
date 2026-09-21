package com.example.movie.service;

import com.example.movie.model.CinemaBookingRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Publisher khởi tạo Saga: sinh correlationId và gửi sự kiện BookingCreated.
 */
@Service
public class BookingPublisherService {

    private static final Logger log = LoggerFactory.getLogger(BookingPublisherService.class);

    public static final String TOPIC = "booking-events";
    public static final String CORRELATION_HEADER = "correlationId";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BookingPublisherService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * @return correlationId vừa được sinh ra (để trả về cho client tra cứu log).
     */
    public String createBooking(CinemaBookingRequest request) {
        // Bước 1: sinh UUID ngẫu nhiên ngay khi nhận request
        String correlationId = UUID.randomUUID().toString();
        log.info("[MovieBookingService] Created booking {}. CorrelationID: {}",
                request.getCinemaBookingId(), correlationId);

        try {
            String payload = objectMapper.writeValueAsString(request);

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    TOPIC, request.getCinemaBookingId(), payload);

            // Bước 2: gắn correlationId vào HEADER - KHÔNG đặt trong payload
            record.headers().add(CORRELATION_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[MovieBookingService] Failed to publish BookingCreated. CorrelationID: {}",
                            correlationId, ex);
                } else {
                    log.info("[MovieBookingService] BookingCreated published to {} (partition {}, offset {}). CorrelationID: {}",
                            TOPIC, result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(), correlationId);
                }
            });
        } catch (JsonProcessingException e) {
            log.error("[MovieBookingService] Error serializing booking request. CorrelationID: {}", correlationId, e);
            throw new IllegalStateException("Cannot serialize booking request", e);
        }
        return correlationId;
    }
}
