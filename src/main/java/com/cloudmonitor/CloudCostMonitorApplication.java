package com.cloudmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Cloud Cost Monitor application.
 *
 * This application monitors AWS cloud costs using the Cost Explorer API,
 * stores historical data in H2 database, and provides REST APIs for
 * cost analysis and alerting.
 */
@SpringBootApplication
@EnableScheduling
public class CloudCostMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudCostMonitorApplication.class, args);
    }
}
