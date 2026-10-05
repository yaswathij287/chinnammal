package com.chinnammal;

import com.smarthostel.database.DatabaseInitializer;
import com.smarthostel.database.DatabaseSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"com.chinnammal", "com.smarthostel"})
public class ChinnammalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChinnammalApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            System.out.println("==================================================");
            System.out.println("  CHINNAMMAL — SMART MANAGEMENT PLATFORM STARTING");
            System.out.println("==================================================");
            try {
                DatabaseInitializer.initializeDatabase();
                DatabaseSeeder.seedIfNeeded();
                System.out.println("[Chinnammal] Database initialized and verified successfully.");
            } catch (Exception e) {
                System.err.println("[Chinnammal] Database startup initialization warning: " + e.getMessage());
            }
        };
    }
}
