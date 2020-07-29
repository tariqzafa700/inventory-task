package com.gildedroses.inventory.operations.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
	
	@Bean
	public Clock getClock() {
		return Clock.systemUTC();
	}

}
