package shah.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
@RequestMapping("/user")
public class UserController {

	@Value("${app.description}")
	private String description;
	
	@Autowired
	private Environment env;
	
	
	@EventListener({RefreshScopeRefreshedEvent.class})
	public void onEvent() {
		System.out.println("NEW VALUE:"+description);
	}
	
	@GetMapping("/status/check")
	public String status() {
		return "Working from user-service on port: "+env.getProperty("local.server.port")+" and instance id: "+env.getProperty("eureka.instance.instance-id");
	}
	@GetMapping("/status/check-property")
	public String checkProperty() {
		return description;
	}
}
