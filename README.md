# [Learn to build RESTful Microservices with Spring Boot and Spring Cloud](https://cognizant.udemy.com/course/spring-boot-microservices-and-spring-cloud/learn/lecture/13233710#overview)

# [Version 8 - Logging and Tracing](https://spring.io/blog/2016/02/15/distributed-tracing-with-spring-cloud-sleuth-and-spring-cloud-zipkin)

Logging using Sleuth

Spring Cloud Sleuth is used to generate and attach the trace id, span id to the logs so that these can then be used by tools like Zipkin and ELK for storage and analysis. Zipkin is a distributed tracing system. It helps gather timing data needed to troubleshoot latency problems in service architectures.

[![Image](./resources/sleuth-zipkin.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://cognizant.udemy.com/course/spring-boot-microservices-and-spring-cloud/learn/lecture/14898450#questions)

### Use case 1: logging using Sleuth

We will implement this in user-service only for now.

### Steps

Add dependencies to user services

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>

Add properties in config-server git application.properties

    spring.zipkin.base-url=http://localhost:9411
    spring.zipkin.sender.type=web
    spring.sleuth.sampler.probability=1

### Test

Just do any GET request and check the logs. This looks like a normal log, except for the part in the beginning between the brackets. This is the core information that Spring Sleuth has added. This data follows the format of:

`[application name, traceId, spanId, export]`

    Application name – This is the name we set in the properties file and can be used to aggregate logs from multiple instances of the same application.  
    TraceId – This is an id that is assigned to a single request, job, or action. Something like each unique user initiated web request will have its own traceId.  
    SpanId – Tracks a unit of work. Think of a request that consists of multiple steps. Each step could have its own spanId and be tracked individually. By default, any application flow will start with same TraceId and SpanId.  
    Export – This property is a boolean that indicates whether or not this log was exported to an aggregator like Zipkin. Zipkin is beyond the scope of this article but plays an important role in analyzing logs created by Sleuth.  


### Use case 2: Tracing using Zipkin

Add dependencies to user services

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-zipkin</artifactId>
        <version>2.2.8.RELEASE</version>
    </dependency>

Starting Zipkin (using Docker)

    docker run -d -p 9411:9411 openzipkin/zipkin

`Test`

On browser, access your zipkin console by run
    
    http://localhost:9411/zipkin/ 

On Postman, run

    http://localhost:8011/user-service/user/get-user-account/1 

Then refresh your zipkin console

#
# [Version 7  - Hystrix Circuit Breaker ](https://cognizant.udemy.com/course/spring-boot-microservices-and-spring-cloud/learn/lecture/14796232#overview)

Circuit Breaker pattern prevents failure cascading and gives a default behavior when services fail. Netflix Hystrix allows us to introduce fault tolerance and latency tolerance by isolating failure and by preventing them from cascading into the other part of the system building a more robust distributed application

## Use case


[![Image](./resources/hystrix-circuit-breaker.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://spring.io/guides/gs/cloud-circuit-breaker/)

We will implement Circuit Breaker on user-service such that if any requests made from user-service failed, it will have a fallback method to execute.

`Steps`

Add dependency in user-service

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        <version>2.2.10.RELEASE</version>
    </dependency>

Add in main class

    @CircuitBreaker

Add your fallback method

    @Component
    class AccountFallBackFactory implements FallbackFactory<AccountFeignClient> {

    @Override
    public AccountFeignClient create(Throwable cause) {
        return new AccountFeignClientFallback(cause);
        }
    }

    class AccountFeignClientFallback implements AccountFeignClient {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Throwable cause;

    public AccountFeignClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public List<AccountResponseModel> getAccounts(Long userId) {
        if (cause instanceof FeignException) {
        logger.error("Feign Exception occured: " + cause.getLocalizedMessage());
        } else if (cause instanceof HystrixTimeoutException) {
        logger.error("Timeout occured connecting to service: " + cause.getLocalizedMessage());
        } else {
        logger.error("Other exception occured: " + cause.getLocalizedMessage());
        }
        return new ArrayList<AccountResponseModel>();
        }
    }


Add in application.properties. We set the feignClient timeout to trigger first before hystrix so we can get meaningful error message from feignClient

    feign.circuitbreaker.enabled=true
    feign.client.config.default.connect-timeout=5000
    feign.client.config.default.read-timeout=5000
    hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=6000


`Test`

1st test - stop your account-service and run 

    GET http://localhost:8011/user-service/user/get-user-account/1

2nd test - start your account-service but with wrong url in your feign client: @FeignClient(name = "account-serviceeee") & run 

    GET http://localhost:8011/user-service/user/get-user-account/1

3rd test - start your account-service with correct url in your feign client but set the return status of account-controller to `FOUND` & run 

    GET http://localhost:8011/user-service/user/get-user-account/1

And lastly change the return status of account-controller to `OK` and run the same url to get correct response.

# [Version 6  - Microservices communication - Part 2 ](https://spring.io/projects/spring-cloud-bus)

## Last lesson was setup for the database and data. Now lets communicate!

### Method 1 -  [RestTemplate](https://www.youtube.com/watch?v=F3uJyeAyv5g)
[Why use ParameterizedTypeReference?](https://stackoverflow.com/questions/58254381/resttemplate-get-list-of-objects-why-use-parameterizedtypereference-instead-of)
[What is ParameterizedTypeReference?](https://shuaibabdulla40.medium.com/need-and-usage-of-rest-template-parameterizedtypereference-13a3a3977f6f)


### Steps

Create Beans in main method

	@Bean
	@LoadBalanced
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

Create Response Model

    @Data
    public class AccountResponseModel {
    private String accountType;
    private Double balance;
    private String accountNumber;
    private Long userId;
    }

Application.properties

    account.url=http://account-service/

Service layer

    public List<AccountResponseModel> getUserAccounts(Long userId) {

		String url = accountUrl+ "account/get-account/" + userId;

		ResponseEntity<List<AccountResponseModel>> response = restTemplate.exchange(url, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<AccountResponseModel>>() {
				});
		List<AccountResponseModel> userAccounts = response.getBody();
		return userAccounts;
	}

Controller

    @GetMapping("/get-user-account/{userId}")
	public ResponseEntity<?> getUserAccount(@PathVariable Long userId) {

		return new ResponseEntity<List<AccountResponseModel>>(userService.getUserAccounts(userId), HttpStatus.OK);
	}

Test in postman

    GET http://localhost:8011/user-service/user/get-user-account/5
    Header -> Accept -> application/json

### Method 2 - [Using Feign Client](https://cloud.spring.io/spring-cloud-openfeign/reference/html/)
### [Solutions to Feign Client issue](https://stackoverflow.com/questions/70038094/error-302-using-feignclient-in-spring-boot-microservices)

Add feign dependencies

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

Add in main method

    @EnableFeignClients

Create interface for feighClient. This works similar to restTemplate

    @FeignClient(name="account-service")
    public interface AccountFeignClient {
  
    @GetMapping("/account/get-account/{userId}")
	public List<AccountResponseModel> getAccounts(@PathVariable Long userId);
    }

Add/edit UserService

    @Autowired
	AccountFeignClient accountFeignClient;

    List<AccountResponseModel> userAccounts = accountFeignClient.getAccounts(userId);

Test - same as RestTemplate approach, using Postman

    GET http://localhost:8011/user-service/user/get-user-account/5
    Header -> Accept -> application/json

`Logging`

Add in application.properties

    #Use relative path
    logging.level.shah.userservice.dto.AccountFeignClient=DEBUG

Add @Beans in main class

    @Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

Run again in postman

    GET http://localhost:8011/user-service/user/get-user-account/5
    Header -> Accept -> application/json

Check your application console for the logs

### [Handle Feign exception using decoder](https://www.appsdeveloperblog.com/feign-error-handling-with-errordecoder/)

Feign ErrorDecoder is for exception handling. [Here](https://github.com/OpenFeign/feign/wiki/Custom-error-handling) is the url for your reference.

### Steps

FeignErrorDecoder.java - Handle Feign Exceptions

    public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
        case 400:
            break;

        case 302:
            return new ResponseStatusException(HttpStatus.valueOf(response.status()),
                "Feign Client only accept status 2xx, else with return FeignException");

        case 404:
            return new ResourceNotFoundException("Account not found");

        default:
            return new Exception(response.reason());
        }
        return null;
        }
    }

Create @Beans in main class

    @Bean
    public FeignErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }

`Test`

Now lets trigger exception. As we now know, Feign Client only accepts status code of 2xx as normal, the rest will trigger exception. Lets change the controller in AccountService to return status FOUND ie 302. 

in AccountController.java,

from 

	return new ResponseEntity<List<Account>>(accountService.getUser(userId), HttpStatus.OK);

change status code to

    return new ResponseEntity<List<Account>>(accountService.getUser(userId), HttpStatus.FOUND);

This will trigger exception and will display the message as we configured earlier.





*********************************************************************************


# [Version 5  - Microservices communication ](https://spring.io/projects/spring-cloud-bus)

We will learn how micro-services can communicate with each other. There are different ways how they communicate and there are different communication patterns. One of the ways is over a HTTP. This communication is Synchronous.

When a HTTP request is sent, the sender service has to wait until it receives a response. It is also a one to one way of communication. One micro-service can send only one request to target a service.

Another way is Asynchronous. It's over a protocol like AMQP, which stands for advance message queuing protocol. It uses queue to exchange messages. Message is placed into a queue and the sender micro-service does not need to wait until this message is processed. A micro-service which is called consumer consumes this message from a queue once the message is processed.

This way there can be even multiple consumers of single message. And if a task to process the message is very time and resource consuming, like, for example, image or video downloads and processing, then a sender micro-service does not need to wait until this very heavy task is performed.

## Use case - Bank account

We currently have 2 microservices - user and account. user-service will have user information and account service will have account information of that user like savings types and available balance. This is a one-to-many relationship: one user can have many account.

[![Image](./resources/user-account-communication.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://spring.io/projects/spring-cloud-bus)

## Steps

Lets add dependencies to user & account services:

        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <artifactId>spring-boot-starter-data-rest</artifactId>
        <artifactId>mysql-connector-java</artifactId>
        <artifactId>validation-api</artifactId>
        <artifactId>lombok</artifactId>

Create this:

    entity/model
    rpository
    service
    controller
    schema.sql
    data.sql

application.properties
        
    #database
    spring.datasource.url=jdbc:mysql://localhost:3306/mydb?useLegacyDatetimeCode=false&serverTimezone=UTC
    spring.datasource.username=admin
    spring.datasource.password=root
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
    spring.jpa.properties.hibernate.format.sql=true
    spring.jpa.hibernate.ddl-auto=update
    spring.sql.init.mode=always
    spring.jpa.show-sql=true
    spring.jpa.generate-ddl=true

Add schema in account-service

    create table  IF NOT EXISTS account (
	id INT NOT NULL AUTO_INCREMENT,
	account_number VARCHAR(50),
	account_type TEXT,
	balance DECIMAL(7,2),
	user_id INT,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    UNIQUE(user_id)
    );

Add schema in user-service

    create table IF NOT EXISTS user(
    id int NOT NULL AUTO_INCREMENT,
    name varchar(50),
    email varchar(50),
    password varchar(50),
    PRIMARY KEY (id)
    );


Run database using Docker

    docker run --detach --env MYSQL_ROOT_PASSWORD=root --env MYSQL_DATABASE=mydb --env MYSQL_PASSWORD=root --env MYSQL_USER=admin --name localhost --publish 3306:3306 mysql:8.0


Test db - Make sure user & account table is created & data inserted.

Run mysql in cli using docker

    docker exec -it localhost bash

Connect to mysql  

    mysql -u admin -proot

Test  

    use mydb;  
    show tables;
    desc user;  
    select * from user;  
    select * from account;  

Stop & remove all running proceses 

    docker rm $(docker ps -a -q) -f

Once all tested, run HHTP requests using postman

    GET http://localhost:8011/user-service/user/get-user/1
    GET http://localhost:8011/account-service/account/get-account/1

Now we are good to go! Lets proceed to call from user service to account service.

************************************************************************************************



# [Version 4 - Spring Cloud Bus ](https://spring.io/projects/spring-cloud-bus)

Spring Cloud Bus links nodes of a distributed system with a lightweight message broker. This can then be used to broadcast state changes (e.g. configuration changes) or other management instructions. AMQP broker implementations are included with the project. Alternatively, any Spring Cloud Stream binder found on the classpath will work out of the box as a transport.

[![Image](./resources/spring-cloud-bus.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://spring.io/projects/spring-cloud-bus)


## Steps

Add dependencies to these services - config,gateway,user,account

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bus-amqp</artifactId>
    </dependency>

Add dependencies to config-server - this is to broadcast changes

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

Expose url in config-server application.properties.

[![Image](./resources/spring-cloud-bus-refresh.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://docs.spring.io/spring-cloud-bus/docs/current/reference/html/#bus-endpoints)

After we update the configuration properties in our remote repository, we need to tell Spring Cloud Bus that it's time to broadcast configuration changes to all micro services that have subscribed to this update. And the way we do it is by sending the POST request to an actuator endpoint called Bus-Refresh. And this is why we've added the actuator dependency to our config-server. So this post request will make config-server to load up new configuration properties from a remote git repository and make Spring Cloud Bus to broadcast these updates to all subscriber Fikret services.

    management.endpoints.web.exposure.include=busrefresh


[`Run RabbitMQ using Docker`](https://hub.docker.com/_/rabbitmq)

RabbitMQ is used as a message broker to implement advanced messaging queuing protocol (AMQP). A complete AMQP has three main components a broker, a consumer, and a producer. For simplicity, we will use docker to run RabbitMQ

For more info, click [here](https://www.baeldung.com/spring-cloud-bus) 

    docker run -d --hostname my-rabbit --name some-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management


Add RabbitMq properties in config-server application.properties (source code)

    spring.rabbitmq.host=localhost
    spring.rabbitmq.port=5672
    spring.rabbitmq.username=guest
    spring.rabbitmq.password=guest

This properties will be applied to other services as they are connected to config-server

Add bootstrap.properties in api-gateway,user,account services

    spring.cloud.config.uri=http://localhost:8012
    spring.cloud.config.name=<service-name>

Add @RefreshScope

Add this in your Controller where the changes are expected to happen. Else changes wont get reflected

Test using user-service

Change your app.description values in user-service.properties git. then run:

    GET http://localhost:8012/user-service/default

to see if the changes is reflected in git. then run:

    GET http://localhost:8011/user-service/user/status/check-property

to see the value u changed. It wont get reflected so u will see the old value. To broadcast the changes, simple run:

    POST http://localhost:8012/actuator/busrefresh 

then run:

    GET http://localhost:8011/user-service/user/status/check-property

and you will see the changes reflected. You can add eventListener in your controller to listen for changes and print the new values and check the logs.

    @EventListener({RefreshScopeRefreshedEvent.class})
	public void onEvent() {
		System.out.println("NEW VALUE:"+description);
	}

**************************************************

# [Version 3 - Encrypt configuration files ](https://cognizant.udemy.com/course/spring-boot-microservices-and-spring-cloud/learn/lecture/14465230#questions)

By default, the spring cloud configuration server stores all property values as plain text. Storing sensitive data in the form of plain text may not be a good idea.

[![Image](./resources/encrypt-property-values.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://cloud.spring.io/spring-cloud-config/reference/html/)

Spring cloud configuration server supports both `symmetric` and `asymmetric` ways of encryption of configuration property values.


## Steps

For some weird thing, if you can't start Eureka Discovery server, [go to solutions.](https://stackoverflow.com/questions/65164809/error-creating-bean-with-name-org-springframework-cloud-netflix-eureka-server-e) Or, add this dependency:

    <dependency>
        <groupId>com.sun.jersey.contribs</groupId>
        <artifactId>jersey-apache-client4</artifactId>
        <version>1.19.4</version>
    </dependency>

Download JCE jars (for java 8 & below)

Add random value for your key in config-server:

     encrypt.key=dstfh69d6086darf9hg8606adsfgb0d6f0h8df6

Then run in postman to encryption random value: 

    POST http://localhost:8012/encrypt

    Body > raw > JSON > random-value

You will get encrypted value. Now copy, go to 

    POST http://localhost:8012/decrypt

    Body > raw > JSON > {ENCRYPTED-VALUE}

You will get back the decrypted value. Now add this in your config-server application properties in GIT:
 
    app.name=random-value

Restart config-server and run in postman to see the value:

    GET http://localhost:8012/user-service/default

You will see the new value. Then, with the encrypted value earlier, replace the value with the encryppted one, with prefix:

    {cipher}ace63b0baf2d031cb4d461078ba04ed9ea5ee23956f500dc734c9dd07b66531f

Restart config-server and run again in postman to see the value:

    GET http://localhost:8012/user-service/default

We get back same encrypted value. This happens because config-server decrypt encrypted properties before it returns them. So the fact that they see unencrypted value returned instead of the encrypted value which is stored in property file tells that config-server was able to decrypt it.

Once all is working, do the same for your spring.cloud.config.server.git.password


#
#





#
#







# [Version 2 - Externalised Configuration](https://springframework.guru/spring-external-configuration-data/)

Spring Boot likes you to externalize your configuration so you can work with the same application code in different environments. You can use properties files, YAML files, environment variables and command-line arguments to externalize configuration. Property values can be injected directly into your beans using the @Value annotation, accessed via Spring’s Environment abstraction or bound to structured objects.

`Diagram`

[![Image](./resources/config-server.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://docs.spring.io/spring-boot/docs/1.0.1.RELEASE/reference/html/boot-features-external-config.html)


## Steps

### Create a GIT repo  

Your config server will fetch properties from this repo. Create a new private repo, and copy the url

### Create config-server project   

Add dependencies: 

    <dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-config-server</artifactId>
	</dependency>

Add this in your main method:

    @EnableConfigServer

application.properties  - ensure token is encrypted before pushing to git!
        
    spring.application.name=config-server
    server.port=8012

    spring.cloud.config.git.uri=https://github.com/norulshahlam/spring-boot-microservices3-properties
    spring.cloud.config.server.git.username=norulshahlam
    spring.cloud.config.server.git.password=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
    spring.cloud.config.server.git.clone-on-start=true
    spring.cloud.config.server.git.default-label=main

   # FOR TESTING
   app.description=you have loaded properties from application.properties in local ${spring.application.name}.


### Other services - (api-gateway, user, account services)

Add in application.properties:

    spring.config.import=optional:configserver:http://localhost:8012

    # FOR TESTING
    app.description=you have loaded property files from ${spring.application.name}

Add in pom.xml

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bootstrap</artifactId>
    </dependency>

### Testing

Let do the test from user-service. Create a controller in UserController.java:

    @Value("${app.description}")
    private String description;

    @GetMapping("/status/check-property")
	public String checkProperty() {
		return description;
	}

Run this url using api-gateway. 8011 is your api-gateway port number

    http://localhost:8011/user-service/user/status/check-property


You should be getting values fetched from config-server. Try this from your other services too.  


## Externalised multiple property files 

We can set different property file which has diffferent level of priority. Below is the illustration, with P1 as the highest:


[![Image](./resources/config-server2.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://docs.spring.io/spring-boot/docs/1.0.1.RELEASE/reference/html/boot-features-external-config.html)

## Steps

Create user-service property file in your private GIT repo and add this:

     app.description=you have loaded property files from user-service in private git repo

Then run the same url to test if this value is picked up. Make sure to restart all services:

    http://localhost:8011/user-service/user/status/check-property

You can also check the property files using client:

    http://localhost:8012/config-server/default
    http://localhost:8012/user-service/default      
 

## [Profiles for multiple environments](https://www.baeldung.com/spring-profiles)  

Spring Profiles provide a way to segregate parts of your application configuration and make it only available in certain environments. It automatically loads the properties in a application file for all profiles and the ones in profile-specific property files only for the specified profile. The properties in the profile-specific configuration override the ones in the master configuration.

[![Image](https://miro.medium.com/max/1400/1*pql-CjRPKvmIp3Hew1Wxag.png "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://ashishgopalhattimare.medium.com/sprimera-a-spring-profile-merger-f9f780004872)

The properties are imported in order from top to bottom. If the properties are also available on lower configurations, the property would get overridden by the lowest configuration i.e. if the same property exists in the my-app-{profile}.yml (1) and application-{profile}.yml (2), then the property in (1) would be taken as final property.

From the above explanation, we can also conclude that the properties present in the lower configuration have higher priority than upper configurations. If a property is imported from my-app-{profile}.yml, it would not be overridden by the upper configurations.
 
 
[![Image](./resources/config-server3.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://docs.spring.io/spring-boot/docs/1.2.0.M1/reference/html/boot-features-profiles.html)

### Steps for creating a profile

In user-service, copy paste application.property and rename it to application-production.properties

Add this in your application.properties to use this profile:

    spring.profiles.active=production


In your application-production.properties, change this:

    # FOR TESTING
    app.description=you have loaded property files from ${spring.application.name} production profile source code

Run this url. You should get the messsage you changed

    http://localhost:8011/user-service/user/status/check-property

Repeat the same for config-server. Copy paste user-service.properties and rename it to user-service-production.properties

In your user-service-production.properties, change this:

    # FOR TESTING
    app.description=you have loaded property files from user-service (production profile) in config server

Run this url. You should get the messsage you changed. 

    http://localhost:8011/user-service/user/status/check-property


Noticed we don't need to activate the profile again in the config server, only once in the source code and the rest will follow accordingly, in order of priority explained in the image earlier. 



############################################################################

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
    @RequestMapping("/user")
    public class UserController {

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