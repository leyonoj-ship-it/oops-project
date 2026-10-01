package com.smartdine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartDineApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartDineApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  SMARTDINE PLATFORM SERVER STARTED SUCCESSFULLY ");
        System.out.println("  URL: http://localhost:8080                     ");
        System.out.println("  H2 Console: http://localhost:8080/h2-console   ");
        System.out.println("=================================================");
    }
}
