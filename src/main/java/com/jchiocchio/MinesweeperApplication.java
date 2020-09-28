package com.jchiocchio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import lombok.Generated;

@ServletComponentScan
@SpringBootApplication
public class MinesweeperApplication {

    @Generated // Ignoring from Code Coverage because we don't bootstrap the app this way when running integration tests
    public static void main(String[] args) {
        SpringApplication.run(MinesweeperApplication.class, args);
    }
}
