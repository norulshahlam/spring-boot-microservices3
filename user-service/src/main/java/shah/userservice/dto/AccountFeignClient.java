package shah.userservice.dto;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="account-service")
public interface AccountFeignClient {
  
  @GetMapping("/account/get-account/{userId}")
	public List<AccountResponseModel> getAccounts(@PathVariable Long userId);
}
