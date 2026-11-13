package com.foodly.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(
        exclude = UserDetailsServiceAutoConfiguration.class
)
public class IamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                IamServiceApplication.class,
                args
        );
    }
}
