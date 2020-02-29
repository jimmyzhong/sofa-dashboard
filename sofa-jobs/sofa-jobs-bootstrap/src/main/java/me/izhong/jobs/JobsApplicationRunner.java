package me.izhong.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(value = {"me.izhong"})
@SpringBootApplication
public class JobsApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(JobsApplicationRunner.class, args);
    }
}
