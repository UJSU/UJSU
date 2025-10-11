package ujsu.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import ujsu.entities.UserProfile;

@NoRepositoryBean
public interface UserProfileRepository<T extends UserProfile> extends CrudRepository<T, Integer>{

	public T findByUserId(int id);
}
