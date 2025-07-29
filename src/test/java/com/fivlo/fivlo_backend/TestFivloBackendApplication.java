package com.fivlo.fivlo_backend;

import org.springframework.boot.SpringApplication;

public class TestFivloBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(FivloBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
