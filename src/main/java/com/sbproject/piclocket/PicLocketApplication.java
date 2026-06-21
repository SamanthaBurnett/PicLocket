package com.sbproject.piclocket;

import com.sbproject.piclocket.config.AwsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AwsProperties.class)
public class PicLocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(PicLocketApplication.class, args);
    }

}
