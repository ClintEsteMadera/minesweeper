package com.jchiocchio;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ComponentScan(basePackages = "com.jchiocchio.entityfactory")
@Slf4j
public class TestConfig {

}
