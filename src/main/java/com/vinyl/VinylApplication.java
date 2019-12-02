package com.vinyl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class VinylApplication {

    public static void main(String[] args) {
        SpringApplication.run(VinylApplication.class, args);
    }
}
