package com.fivlo.fivlo_backend;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableBatchProcessing
@EnableScheduling
@SpringBootApplication
public class FivloBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FivloBackendApplication.class, args);
	}

}
