package com.capgemini.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

@SpringBootApplication
//@EnableBatchProcessing
public class UserServiceApplication {
    public static void main(String[] args) {
        System.out.println("**** STARTING USER SERVICE ****");
        SpringApplication.run(UserServiceApplication.class, args);
    }
}