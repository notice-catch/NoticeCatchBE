package com.noticecatch.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NoticeCatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeCatchApplication.class, args);
    }

}
