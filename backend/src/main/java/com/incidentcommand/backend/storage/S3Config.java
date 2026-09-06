package com.incidentcommand.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * See docs/19-aws-strategy.md (S3). Credentials are NOT wired here explicitly
 * -- the SDK's default credential chain picks up AWS_ACCESS_KEY_ID /
 * AWS_SECRET_ACCESS_KEY from the environment (via backend/.env + spring-dotenv
 * locally) automatically, and will pick up an IAM role with zero code changes
 * once this runs on real AWS infrastructure -- see docs/15-security-and-rbac.md.
 */
@Configuration
public class S3Config {

    @Bean
    S3Client s3Client(@Value("${incidentcommand.s3.region:ap-south-1}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
