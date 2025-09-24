package ujsu.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ujsu.entities.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

	public Optional<User> findByEmail(String email);

	public <S extends User> S save(S user);
}
