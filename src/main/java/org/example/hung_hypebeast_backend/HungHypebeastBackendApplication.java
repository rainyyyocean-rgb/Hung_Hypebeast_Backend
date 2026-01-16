package org.example.hung_hypebeast_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HungHypebeastBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HungHypebeastBackendApplication.class, args);
    }

}
