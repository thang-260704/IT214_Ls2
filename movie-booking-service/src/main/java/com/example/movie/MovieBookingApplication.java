package com.example.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

@SpringBootApplication
public class MovieBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieBookingApplication.class, args);
    }

    /** Tự động tạo topic booking-events nếu chưa tồn tại. */
    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name("booking-events").partitions(1).replicas(1).build();
    }
}
