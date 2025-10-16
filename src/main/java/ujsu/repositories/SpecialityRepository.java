package ujsu.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import ujsu.entities.Speciality;


public interface SpecialityRepository extends CrudRepository<Speciality, Integer> {
	
	Optional<Speciality> findByCode(String code);
}