package com.trazabilidad.ayni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AyniApplication {

	public static void main(String[] args) {
		SpringApplication.run(AyniApplication.class, args);
	}

}
