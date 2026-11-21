package com.incidentcommand.backend.observability;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

/**
 * See infra/aws/cloudwatch/production-simulator-logging.json. Unlike S3Config
 * (which relies on the SDK's default credential chain / AWS_ACCESS_KEY_ID),
 * this builds its own explicit credentials provider from distinct env vars --
 * the whole point of this feature is that log-reading uses a genuinely
 * separate, read-only IAM identity (incidentcommand-logs-reader-dev) from the
 * S3 writer identity, and the default chain only ever holds one credential
 * pair at a time.
 */
@Configuration
public class CloudWatchLogsConfig {

    @Bean
    CloudWatchLogsClient cloudWatchLogsClient(
            @Value("${incidentcommand.cloudwatch.region:ap-south-1}") String region,
            @Value("${incidentcommand.cloudwatch.access-key-id:}") String accessKeyId,
            @Value("${incidentcommand.cloudwatch.secret-access-key:}") String secretAccessKey) {
        var builder = CloudWatchLogsClient.builder().region(Region.of(region));
        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        }
        // Blank: falls back to the SDK default chain. Harmless at bean-construction
        // time either way -- CloudWatchLogsService gates every real call behind
        // incidentcommand.cloudwatch.enabled, same pattern as S3StorageService.
        return builder.build();
    }
}
