package com.spring.batch.sample.tasklet;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class Main {
	public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
