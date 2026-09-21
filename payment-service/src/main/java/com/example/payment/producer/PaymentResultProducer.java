package com.example.payment.producer;

import com.example.payment.model.PaymentEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** Gửi sự kiện kết quả thanh toán, vẫn giữ nguyên correlationId trong header. */
@Component
public class PaymentResultProducer {

    public static final String TOPIC = "payment-events";
    public static final String CORRELATION_HEADER = "correlationId";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentResultProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(PaymentEvent event, String correlationId) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(event);
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, event.getCinemaBookingId(), payload);
        record.headers().add(CORRELATION_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record);
    }
}
