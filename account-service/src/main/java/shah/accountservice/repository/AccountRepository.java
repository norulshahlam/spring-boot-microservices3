package shah.accountservice.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import shah.accountservice.model.Account;


public interface AccountRepository extends CrudRepository<Account, Long> {

	List<Account> findByUserId(Long userId);
}
