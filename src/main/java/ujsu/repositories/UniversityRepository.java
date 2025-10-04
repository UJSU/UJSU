package ujsu.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import ujsu.entities.University;

public interface UniversityRepository extends CrudRepository<University, Integer> {

	Optional<University> findByName(String name);
}