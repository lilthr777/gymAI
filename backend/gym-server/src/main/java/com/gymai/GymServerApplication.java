package com.gymai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gymai.mapper")
public class GymServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymServerApplication.class, args);
    }
}
