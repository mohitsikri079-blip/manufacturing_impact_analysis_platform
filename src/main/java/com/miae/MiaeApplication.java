package com.miae;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Manufacturing Impact Analysis Engine (MIAE).
 * This class serves as the entry point for the Spring Boot application, initializing the application context and starting the embedded server. 
 * <p>
 * The MIAE application is designed to analyze the impact of changes in manufacturing processes by leveraging a graph database (ex : Neo4j) to model the relationships between products, revisions, bills of materials, suppliers, inventory, and orders.
 * <p>
 * The application provides RESTful APIs for projecting data into the graph and performing impact analysis based on the relationships defined in the graph database. 
 */
@SpringBootApplication
public class MiaeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiaeApplication.class, args);
    }
}
