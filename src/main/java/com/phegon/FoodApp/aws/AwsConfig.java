package com.phegon.FoodApp.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {
    @Value("${aws.s3.region}")
    private String awsRegion;



    @Value("${aws.s3.accessKeyId}")
    private String awsAccessKey;

    @Value("${aws.s3.secretKey}")
    private String awsSecretKey;


    @Bean
    public StaticCredentialsProvider staticCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
        );
    }


    // what does bean do= // It is used to indicate that a method produces a bean to be managed by the Spring container.
    @Bean
    public S3Client s3Client(StaticCredentialsProvider credentialsProvider){
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }


}
