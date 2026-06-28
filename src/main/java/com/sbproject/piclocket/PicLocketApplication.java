package com.sbproject.piclocket;

import com.sbproject.piclocket.config.AwsProperties;
import com.sbproject.piclocket.config.CorsProperties;
import com.sbproject.piclocket.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        AwsProperties.class, JwtProperties.class, CorsProperties.class
})
public class PicLocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(PicLocketApplication.class, args);
    }

}
