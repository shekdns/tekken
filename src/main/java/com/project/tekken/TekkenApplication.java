package com.project.tekken;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TekkenApplication {
    //test
    public static void main(String[] args) {
        SpringApplication.run(TekkenApplication.class, args);
    }
}
