package com.qdx.bishe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.qdx.bishe.mapper")
@ConfigurationPropertiesScan
public class BisheBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BisheBackendApplication.class, args);
    }
}

