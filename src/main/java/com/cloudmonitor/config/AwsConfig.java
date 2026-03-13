package com.cloudmonitor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;

/**
 * AWS SDK Configuration.
 *
 * Creates beans for AWS services using the default credential provider chain.
 * Credentials are resolved in order: environment variables, system properties,
 * credential profiles file (~/.aws/credentials), EC2 instance profile, etc.
 *
 * NEVER hardcode AWS credentials in this file or any source code.
 */
@Configuration
public class AwsConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsConfig.class);

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    /**
     * Creates the AWS Cost Explorer client.
     * Uses the default credential provider chain for authentication.
     */
    @Bean
    public CostExplorerClient costExplorerClient() {
        log.info("Initializing CostExplorerClient with region: {}", awsRegion);

        return CostExplorerClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Configures ObjectMapper for JSON serialization with Java 8 date/time support.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
