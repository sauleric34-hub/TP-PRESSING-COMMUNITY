package com.pressing.gestion_pressing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionPressingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionPressingApplication.class, args);
	}

}
