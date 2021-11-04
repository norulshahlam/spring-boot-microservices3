# Learn to build RESTful Microservices with Spring Boot and Spring Cloud

# [Udemy tutorials](https://cognizant.udemy.com/course/spring-boot-microservices-and-spring-cloud/learn/lecture/13233710#overview)

## Version 1

### Service Discovery

Eureka Server is an application that holds the information about all client-service applications. Every Micro service will register into the Eureka server and Eureka server knows all the client applications running on each port and IP address. Eureka Server is also known as Discovery Server.

Let's create a new service called `service-discovery` for this

`Diagram`

[![Image](./resources/service-discovery.jpg "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/service-discovery.jpg)

`Dependencies`

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>

`application.properties`

    server.port=8010
    spring.application.name=discoveryservice
    eureka.client.registerWithEureka=false
    eureka.client.fetchRegistry=false
    eureka.client.serviceUrl.defaultZone = http://localhost:8010/eureka

`@EnableEurekaServer`

    Add this in your main method

`Start your Spring boot!`

    go to http://localhost:8010/

### User Service

`Diagram and features`

[![Image](./resources/user-service-1.jpg "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/user-service-1.jpg)  
[![Image](./resources/user-service-2.jpg "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/user-service-1.jpg)

### Dependencies

For this service to get registered in Discovery service, this service acts as Eureka client


    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

`@EnableEurekaClient`

    Add this in your main method

`Application properties`

    # 0 means port will be dynamically assigned
    server.port=0
    spring.application.name=users-ws
    eureka.client.serviceUrl.defaultZone = http://localhost:8010/eureka
    spring.devtools.restart.enabled = true

`Set up controller`

	@GetMapping("/status/check")
	public String status() {
		return "Working";
	}

`Start (both discovery-service and user-service) your Spring boot!`

- Go to http://localhost:8010/
- Your user-service will get registered in Eureka
- Click on user-service
- Go to http://localhost:{assigned-port}/status/check
- You will get the String value assigned


### Account Service

`Diagram and features`

[![Image](./resources/account-service-1.jpg "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/account-service-1.jpg)  
[![Image](./resources/account-service-2.jpg "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/account-service-1.jpg)

### Dependencies

For this service to get registered in Discovery service, this service acts as Eureka client

