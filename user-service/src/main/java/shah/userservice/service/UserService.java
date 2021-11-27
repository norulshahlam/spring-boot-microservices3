package shah.userservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import shah.userservice.dto.AccountResponseModel;
import shah.userservice.model.User;
import shah.userservice.repository.UserRepository;

@Data
@Service
public class UserService {

	@Autowired
	private final UserRepository userRepo;

	@Autowired
	RestTemplate restTemplate;

	public UserService(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	public User getUser(Long id) {
		return userRepo.findById(id).get();
	}

	public List<AccountResponseModel> getUserAccounts(Long userId) {

		String accountUrl = "http://localhost:8011/account-service/account/get-account/" + userId;

		// DONT USE THIS METHOD
		// List<AccountResponseModel> userAccounts =
		// Arrays.asList(restTemplate.getForObject(accountUrl,
		// AccountResponseModel[].class));

		ResponseEntity<List<AccountResponseModel>> response = restTemplate.exchange(accountUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<AccountResponseModel>>() {
				});
		List<AccountResponseModel> userAccounts = response.getBody();
		return userAccounts;
	}
}
