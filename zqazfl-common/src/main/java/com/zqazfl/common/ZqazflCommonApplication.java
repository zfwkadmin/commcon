package com.zqazfl.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class ZqazflCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZqazflCommonApplication.class, args);
    }

}
