package shah.userservice.repository;

import org.springframework.data.repository.CrudRepository;

import shah.userservice.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

	User findByName(String name);
}
