package shah.userservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import shah.userservice.dto.AccountResponseModel;
import shah.userservice.model.User;
import shah.userservice.service.UserService;

@RestController
@RefreshScope
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	@Value("${app.description}")
	private String description;

	@Autowired
	private Environment env;

	@Autowired
	private UserService userService;

	@EventListener({ RefreshScopeRefreshedEvent.class })
	public void onEvent() {
		System.out.println("NEW VALUE:" + description);
	}

	@GetMapping("/status/check")
	public String status() {
		return "Working from user-service on port: " + env.getProperty("local.server.port") + " and instance id: "
				+ env.getProperty("eureka.instance.instance-id");
	}

	@GetMapping("/status/check-property")
	public String checkProperty() {
		return description;
	}

	@GetMapping("/get-user/{id}")
	public ResponseEntity<?> getUser(@PathVariable Long id) {
		return new ResponseEntity<User>(userService.getUser(id), HttpStatus.FOUND);
	}

	@GetMapping("/get-user-account/{userId}")
	public ResponseEntity<?> getUserAccount(@PathVariable Long userId) {

		return new ResponseEntity<List<AccountResponseModel>>(userService.getUserAccounts(userId), HttpStatus.OK);
	}
}
