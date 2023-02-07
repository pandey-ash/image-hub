package com.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class AmazonConfig {
	
	@Bean
    public AmazonS3 s3() {
//		AWSCredentials awsCredentials =
//                new BasicAWSCredentials("AKIAZFAFK6BVWC7GQRCJ", "buMQJ27vI4L/8lzLv9WLqnUKh2Rn3iHWmT6Ui2gI");
//        return AmazonS3ClientBuilder
//                .standard()
//                .withRegion("us-east-1")
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .build();
        
        return AmazonS3ClientBuilder
                .standard()
                .withRegion("us-east-1")
                .withCredentials(new EC2ContainerCredentialsProviderWrapper())
                .build();

//        return AmazonS3ClientBuilder.standard()
//                .withCredentials(new ProfileCredentialsProvider())
//                .withRegion(Regions.US_EAST_1)
//                .build();
    }
	
//	@Bean
//    @Profile("ec2")
//    public AmazonS3 amazonS3ForEC2() {
//        return AmazonS3ClientBuilder.standard()
//                .withCredentials(new InstanceProfileCredentialsProvider(false))
//                .withRegion(region)
//                .build();
//    }
}
