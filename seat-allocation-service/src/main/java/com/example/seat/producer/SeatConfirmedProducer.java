package com.example.seat.producer;

import com.example.seat.model.CinemaBookingRequest;
import com.example.seat.model.SeatAllocationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** Gửi sự kiện SeatConfirmed và TRUYỀN TIẾP correlationId qua header. */
@Component
public class SeatConfirmedProducer {

    private static final Logger log = LoggerFactory.getLogger(SeatConfirmedProducer.class);

    public static final String TOPIC = "seat-confirmed-events";
    public static final String CORRELATION_HEADER = "correlationId";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SeatConfirmedProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSeatConfirmed(CinemaBookingRequest request, String correlationId) throws JsonProcessingException {
        SeatAllocationEvent event = SeatAllocationEvent.from(request, "SEAT_CONFIRMED");
        String payload = objectMapper.writeValueAsString(event);

        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, request.getCinemaBookingId(), payload);
        // Chuyển tiếp correlationId nguyên vẹn sang header của sự kiện kế tiếp
        record.headers().add(CORRELATION_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record);
        log.info("[SeatAllocationService] SeatConfirmed published for {}. CorrelationID: {}",
                request.getCinemaBookingId(), correlationId);
    }
}
