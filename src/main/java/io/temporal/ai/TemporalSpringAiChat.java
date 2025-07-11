package io.temporal.ai;

import io.temporal.client.WorkflowClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TemporalSpringAiChat {
    public static void main(String[] args) {
        SpringApplication.run(TemporalSpringAiChat.class, args);
    }
}
