package shah.accountservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import shah.accountservice.model.Account;
import shah.accountservice.service.AccountService;

@RestController
@RefreshScope
@RequestMapping("/account")
public class AccountController {

	@Value("${app.description}")
	private String description;

	@Autowired
	private AccountService accountService;

	@Autowired
	private Environment env;

	@GetMapping("/status/check")
	public String status() {
		return "Working from account-service on port: " + env.getProperty("local.server.port") + " and instance id: "
				+ env.getProperty("eureka.instance.instance-id");
	}

	@GetMapping("/status/check-property")
	public String checkProperty() {
		return description;
	}

	@GetMapping("/get-account/{userId}")
	public ResponseEntity<?> getUser(@PathVariable Long userId) {
		return new ResponseEntity<List<Account>>(accountService.getUser(userId), HttpStatus.OK);
	}
}
