package ujsu.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import ujsu.entities.Organisation;


public interface OrganisationRepository extends CrudRepository<Organisation, Integer> {
	Optional<Organisation> findByName(String organisationName);
}