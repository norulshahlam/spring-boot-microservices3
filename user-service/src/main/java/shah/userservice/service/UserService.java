package shah.userservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shah.userservice.dto.AccountFeignClient;
import shah.userservice.dto.AccountResponseModel;
import shah.userservice.model.User;
import shah.userservice.repository.UserRepository;

@Data
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

	@Value("${account.url}")
	private String accountUrl;

	@Autowired
	private final UserRepository userRepo;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	AccountFeignClient accountFeignClient;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public User getUser(Long id) {
		return userRepo.findById(id).get();
	}

	public List<AccountResponseModel> getUserAccounts(Long userId) {

		String url = accountUrl + "account/get-account/" + userId;

		// DONT USE THIS METHOD
		// List<AccountResponseModel> userAccounts =
		// Arrays.asList(restTemplate.getForObject(accountUrl,
		// AccountResponseModel[].class));

		// ResponseEntity<List<AccountResponseModel>> response =
		// restTemplate.exchange(url, HttpMethod.GET, null,
		// new ParameterizedTypeReference<List<AccountResponseModel>>() {
		// });
		// List<AccountResponseModel> userAccounts = response.getBody();

		logger.info("1");
		List<AccountResponseModel> userAccounts = accountFeignClient.getAccounts(userId);
		logger.info("2");
		return userAccounts;
	}
}
