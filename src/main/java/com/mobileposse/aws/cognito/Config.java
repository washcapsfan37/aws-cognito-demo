package com.mobileposse.aws.cognito;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClientBuilder;
import com.amazonaws.services.cognitosync.AmazonCognitoSync;
import com.amazonaws.services.cognitosync.AmazonCognitoSyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class Config {
    private static AWSCredentialsProvider getCredentials(String profile) {
        return StringUtils.isEmpty(profile) || "default".equalsIgnoreCase(profile)
                ? new DefaultAWSCredentialsProviderChain()
                : new ProfileCredentialsProvider(profile);
    }

    @Value("${com.mobileposse.aws.profile}")
    private String awsProfile = null;

    @Bean
    public AmazonCognitoIdentity amazonCognitoIdentity() {
        return AmazonCognitoIdentityClientBuilder
                .standard()
                .withCredentials(getCredentials(awsProfile))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    @Bean
    public AmazonCognitoSync amazonCognitoSync() {
        return AmazonCognitoSyncClientBuilder
                .standard()
                .withCredentials(getCredentials(awsProfile))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

}
