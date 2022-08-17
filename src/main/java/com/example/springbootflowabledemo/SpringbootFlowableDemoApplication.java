package com.example.springbootflowabledemo;

import com.example.springbootflowabledemo.service.MyService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan(basePackages = "com.example.springbootflowabledemo.mapper")
public class SpringbootFlowableDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootFlowableDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(final MyService myService) {

        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                myService.createDemoUsers();
            }
        };
    }

}
