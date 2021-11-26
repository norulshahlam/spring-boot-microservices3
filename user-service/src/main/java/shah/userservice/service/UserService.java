package shah.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import shah.userservice.model.User;
import shah.userservice.repository.UserRepository;

@Data
@Service
public class UserService {

	@Autowired
	private final UserRepository userRepo;
	
	public UserService(UserRepository userRepo) {
		this.userRepo=userRepo;
	}
	
	public User getUser(Long id){
		return userRepo.findById(id).get();
	}
}