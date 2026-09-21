package com.example.seat;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
public class SeatAllocationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeatAllocationApplication.class, args);
    }

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name("booking-events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic seatConfirmedEventsTopic() {
        return TopicBuilder.name("seat-confirmed-events").partitions(1).replicas(1).build();
    }
}
