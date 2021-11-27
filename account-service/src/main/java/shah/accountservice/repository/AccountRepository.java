package shah.accountservice.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import shah.accountservice.model.Account;


public interface AccountRepository extends CrudRepository<Account, String> {

	List<Account> findByUserId(Long userId);
}
