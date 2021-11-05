# [Learn to build RESTful Microservices with Spring Boot and Spring Cloud](https://cognizant.udemy.com/course/spring-boot-microservices-and-spring-cloud/learn/lecture/13233710#overview)

# Version 1

## Service Discovery

Eureka Server is an application that holds the information about all client-service applications. Every Micro service will register into the Eureka server and Eureka server knows all the client applications running on each port and IP address. Eureka Server is also known as Discovery Server.

Let's create a new service called `service-discovery` for this

`Diagram`

[![Image](./resources/service-discovery.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/service-discovery.JPG)

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
    spring.application.name=discovery-service
    eureka.client.registerWithEureka=false
    eureka.client.fetchRegistry=false
    eureka.client.serviceUrl.defaultZone = http://localhost:8010/eureka

`@EnableEurekaServer`

    Add this in your main method

`Start your Spring boot!`

    go to http://localhost:8010/

## User Service

`Diagram and features`

[![Image](./resources/user-service-1.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/user-service-1.JPG)  
[![Image](./resources/user-service-2.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/user-service-1.JPG)

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
    spring.application.name=users-service
    eureka.client.serviceUrl.defaultZone = http://localhost:8010/eureka
    spring.devtools.restart.enabled = true

`Set up controller`

	@RestController
@   RequestMapping("/user")
p   ublic class UserController {

	@Autowired
	private Environment env;

	@GetMapping("/status/check")
	public String status() {
		return "Working from user-service on port: "+env.getProperty("local.server.port")+" and instance id: "+env.getProperty("eureka.instance.instance-id");
	    }
    }



`Start (both discovery-service and user-service) your Spring boot!`

- Go to http://localhost:8010/
- Your user-service will get registered in Eureka
- Click on user-service
- Go to http://localhost:{assigned-port}/user/status/check
- You will get the String value assigned


### Account Service

`Diagram and features`

[![Image](./resources/account-service-1.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/account-service-1.JPG)  
[![Image](./resources/account-service-2.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/account-service-1.JPG)

### Dependencies

For this service to get registered in Discovery service, this service acts as Eureka client.

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
    spring.application.name=account-service
    eureka.client.serviceUrl.defaultZone = http://localhost:8010/eureka
    spring.devtools.restart.enabled = true

`Set up controller`

    @RestController
    @RequestMapping("/account")
    public class AccountController {

    @Autowired
    private Environment env;

    @GetMapping("/status/check")
    public String status() {
        return "Working from account-service on port: "+env.getProperty("local.server.port")+" and instance id: "+env.getProperty("eureka.instance.instance-id");
        }
    }


`Start (both discovery-service and user-service) your Spring boot!`

- Go to http://localhost:8010/
- Your user-service will get registered in Eureka
- Click on user-service
- Go to http://localhost:{assigned-port}/account/status/check
- You will get the String value assigned


### Api Gateway Service

`Diagram and features`

[![Image](./resources/api-gateway-service-1.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](./resources/api-gateway-service-1.JPG)  

### Dependencies

Api Gateway Service must register in Discovery service, in order to connect to  other services.

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>


`Application properties`
        
    spring.application.name=api-gateway-service
    server.port=8082
    eureka.client.serviceUrl.defaultZone = http://localhost:8010/eureka
    spring.cloud.gateway.discovery.locator.enabled=true

    # ignore case-sensitive
    spring.cloud.gateway.discovery.locator.lower-case-service-id=true


`Start (all services) your Spring boot!`

Testing your api-gateway as automatic routing

    http://localhost:8082/user-service/user/status/check
    http://localhost:8082/account-service/account/status/check

### Running multiple instances

Discovery service can only register unique instance id based on the same instances. We can do that if we assign a unique id in our application.properties. Add/change this in user & account service:

    server.port=${PORT:0}
    eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}

### Run using Spring Boot with random instance id and port

Run user service few time and check Eureka dashboard. Alternatively you can use CLI to run too and use your own custom port and instance id. Make sure you are in that service project folder.

### Run using Maven with random/custom instance id and port

Random port with custom instance id.  

    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.application.instance_id=shah44"

Random port and random instance id.

    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.application.instance_id=shah45 --server.port=1234"

Once you have multiple instances running, go to the url /status/check and keep refreshing to see different port used