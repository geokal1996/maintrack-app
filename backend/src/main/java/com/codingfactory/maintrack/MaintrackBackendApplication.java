package com.codingfactory.maintrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Auto to einai to "power button" tis efarmogis.
// Otan to trexoume, to Spring Boot sikonei enan server pou akouei gia requests.
@SpringBootApplication
public class MaintrackBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaintrackBackendApplication.class, args);
    }

}
