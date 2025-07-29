package com.helios.cctv;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.helios.cctv.mapper")
public class CctvApplication {

    public static void main(String[] args) {
        SpringApplication.run(CctvApplication.class, args);
    }

}
