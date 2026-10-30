package com.ppphui.cemsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CemSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(CemSystemApplication.class, args);
	}

}
