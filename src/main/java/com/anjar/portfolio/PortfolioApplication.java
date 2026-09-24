package com.anjar.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // ← TAMBAH INI
public class PortfolioApplication {
	public static void main(String[] args) {
		SpringApplication.run(PortfolioApplication.class, args);
	}
}