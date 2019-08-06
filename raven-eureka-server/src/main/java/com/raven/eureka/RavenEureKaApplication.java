package com.raven.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author XiuYang
 * @date 2019/08/06
 */

@SpringBootApplication
@EnableEurekaServer
public class RavenEureKaApplication {
    public static void main(String[] args) {
        SpringApplication.run(RavenEureKaApplication.class, args);
    }
}