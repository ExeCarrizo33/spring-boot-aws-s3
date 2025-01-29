package com.s3.api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.access.key}")
    private String awsAccessKey;

    @Value("${aws.secret.key}")
    private String awsSecretKey;

    @Value("${aws.region}")
    private String region;


    /**
     * Provee un cliente S3 síncrono.
     *
     * @return una instancia de S3Client configurada con las credenciales y la región especificadas.
     */
    @Bean
    public S3Client getS3client() {
        AwsCredentials basicCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com"))
                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .build();
    }

    /**
     * Provee un cliente S3 asíncrono.
     *
     * @return una instancia de S3AsyncClient configurada con las credenciales y la región especificadas.
     */
    @Bean
    public S3AsyncClient getS3AsyncClient() {
        AwsCredentials basicCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        return S3AsyncClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com"))
                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .forcePathStyle(true)
                .build();
    }

    /**
     * Provee un objeto para firmar URLs de S3.
     *
     * @return una instancia de S3Presigner configurada con las credenciales y la región especificadas.
     */
    @Bean
    public S3Presigner getS3Presigner() {
        AwsCredentials basicCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .build();
    }
}
