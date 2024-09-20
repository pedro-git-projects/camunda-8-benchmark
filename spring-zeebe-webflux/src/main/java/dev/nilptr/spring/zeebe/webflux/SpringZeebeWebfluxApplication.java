package dev.nilptr.spring.zeebe.webflux;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath*:/bpmn/**/*.bpmn")
public class SpringZeebeWebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringZeebeWebfluxApplication.class, args);
    }

}
