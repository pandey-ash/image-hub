package com.cloud.assignment;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScans({@ComponentScan("com.cloud.controller"), @ComponentScan("com.cloud.entity"), 
	@ComponentScan("com.cloud.entity"), @ComponentScan("com.cloud.repository"), @ComponentScan("com.cloud.service"), 
	@ComponentScan("com.cloud.exception"), @ComponentScan("com.cloud.config")
})
@EntityScan("com.cloud.entity")
@EnableJpaRepositories("com.cloud.repository")
public class AssignmentApplication {

	public static void main(String[] args) throws ClassNotFoundException {
		
		SpringApplication.run(AssignmentApplication.class, args);
	}

}
