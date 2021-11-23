# [Learn to build RESTful Microservices with Spring Boot and Spring Cloud](https://cognizant.udemy.com/course/spring-boot-microservices-and-spring-cloud/learn/lecture/13233710#overview)


# [Version 5 - Logging and Tracing](https://spring.io/blog/2016/02/15/distributed-tracing-with-spring-cloud-sleuth-and-spring-cloud-zipkin)

Spring Cloud Sleuth is used to generate and attach the trace id, span id to the logs so that these can then be used by tools like Zipkin and ELK for storage and analysis. Zipkin is a distributed tracing system. It helps gather timing data needed to troubleshoot latency problems in service architectures.


## Steps

Add dependencies to all services

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-sleuth-zipkin</artifactId>
    </dependency>

Add properties in config-server git application.properties

    spring.zipkin.base-url=http://localhost:9411
    spring.zipkin.sender.type=web
    spring.zipkin.sampler.probability=1



# [Version 4 - Spring Cloud Bus ](https://spring.io/projects/spring-cloud-bus)

Spring Cloud Bus links nodes of a distributed system with a lightweight message broker. This can then be used to broadcast state changes (e.g. configuration changes) or other management instructions. AMQP broker implementations are included with the project. Alternatively, any Spring Cloud Stream binder found on the classpath will work out of the box as a transport.

[![Image](./resources/spring-cloud-bus.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://spring.io/projects/spring-cloud-bus)


## Steps

`Add dependencies to these services - config,gateway,user,account`

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bus-amqp</artifactId>
    </dependency>

`Add dependencies to config-server` - this is to broadcast changes

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

`Expose url in config-server application.properties.` 

[![Image](./resources/spring-cloud-bus-refresh.JPG "Deploying Spring Boot Apps to AWS using Elastic Beanstalk")](https://docs.spring.io/spring-cloud-bus/docs/current/reference/html/#bus-endpoints)

After we update the configuration properties in our remote repository, we need to tell Spring Cloud Bus that it's time to broadcast configuration changes to all micro services that have subscribed to this update. And the way we do it is by sending the POST request to an actuator endpoint called Bus-Refresh. And this is why we've added the actuator dependency to our config-server. So this post request will make config-server to load up new configuration properties from a remote git repository and make Spring Cloud Bus to broadcast these updates to all subscriber Fikret services.

    management.endpoints.web.exposure.include=busrefresh
 
 
[`Run RabbitMQ using Docker`](https://hub.docker.com/_/rabbitmq)

RabbitMQ is used as a message broker to implement advanced messaging queuing protocol (AMQP). A complete AMQP has three main components a broker, a consumer, and a producer. For simplicity, we will use docker to run RabbitMQ

For more info, click [here](https://www.baeldung.com/spring-cloud-bus) 

    docker run -d --hostname my-rabbit --name some-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management


`Add RabbitMq properties in config-server application.properties (source code)`

    spring.rabbitmq.host=localhost
    spring.rabbitmq.port=5672
    spring.rabbitmq.username=guest
    spring.rabbitmq.password=guest

This properties will be applied to other services as they are connected to config-server

`Add bootstrap.properties in api-gateway,user,account services`

    spring.cloud.config.uri=http://localhost:8012
    spring.cloud.config.name=<service-name>

`Add @RefreshScope`

Add this in your Controller where the changes are expected to happen. Else changes wont get reflected

`Test using user-service`

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