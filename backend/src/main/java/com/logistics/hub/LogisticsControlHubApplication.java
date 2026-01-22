package com.logistics.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LogisticsControlHubApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(LogisticsControlHubApplication.class, args);
		
		System.out.println("\033[0;33m=========================================\033[0m");
        System.out.println("\033[1;34m   AI SUPPLY CHAIN CONTROL TOWER   \033[0m");
        System.out.println("\033[0;33m=========================================\033[0m");
    }

}
