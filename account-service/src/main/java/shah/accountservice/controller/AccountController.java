package shah.accountservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RefreshScope
@RequestMapping("/account")
public class AccountController {


	@Value("${app.description}")
	private String description;
	
	@Autowired
	private Environment env;
	
	@GetMapping("/status/check")
	public String status() {
		return "Working from account-service on port: "+env.getProperty("local.server.port")+" and instance id: "+env.getProperty("eureka.instance.instance-id");
	}
	@GetMapping("/status/check-property")
	public String checkProperty() {
		return description;
	}
}
