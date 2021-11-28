package shah.userservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import shah.userservice.dto.AccountFeignClient;
import shah.userservice.dto.AccountResponseModel;
import shah.userservice.model.User;
import shah.userservice.repository.UserRepository;

@Data
@Service
@RequiredArgsConstructor
public class UserService {

	@Value("${account.url}")
	private String accountUrl;

	@Autowired
	private final UserRepository userRepo;

	// @Autowired
	// RestTemplate restTemplate;

	@Autowired
	AccountFeignClient accountFeignClient;

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
		List<AccountResponseModel> userAccounts = accountFeignClient.getAccounts(userId);

		return userAccounts;
	}
}
