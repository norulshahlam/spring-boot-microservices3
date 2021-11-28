package shah.accountservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;
import shah.accountservice.model.Account;
import shah.accountservice.repository.AccountRepository;

@Data
@Service
public class AccountService {

	@Autowired
	private final AccountRepository accountRepo;
	
	public AccountService(AccountRepository accountRepo) {
		this.accountRepo=accountRepo;
	}
	
	public List<Account> getUser(Long userId){
		System.out.println(4);
		List<Account> accounts=accountRepo.findByUserId(userId);
		return accounts;
	}
}
