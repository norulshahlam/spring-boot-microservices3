package shah.userservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.netflix.hystrix.exception.HystrixTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import feign.FeignException;

@FeignClient(name = "account-service", fallbackFactory = AccountFallBackFactory.class)
public interface AccountFeignClient {

  @GetMapping("/account/get-account/{userId}")
  public List<AccountResponseModel> getAccounts(@PathVariable Long userId);
}

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
      logger.error("Feign Exception occured: " + cause);
    } else if (cause instanceof HystrixTimeoutException) {
      logger.error("Timeout occured connecting to service: " + cause.getLocalizedMessage());
    } else {
      logger.error("Other exception occured: " + cause);
    }
    return new ArrayList<AccountResponseModel>();
  }
}