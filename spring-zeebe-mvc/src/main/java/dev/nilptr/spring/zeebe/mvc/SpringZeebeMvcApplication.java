package dev.nilptr.spring.zeebe.mvc;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath*:/bpmn/**/*.bpmn")
public class SpringZeebeMvcApplication {


    public static void main(String[] args) {
        SpringApplication.run(SpringZeebeMvcApplication.class, args);
    }

}
