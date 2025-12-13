package ujsu.repositories;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import ujsu.entities.Organisation;


public interface OrganisationRepository extends CrudRepository<Organisation, Integer> {
	
	Optional<Organisation> findByName(String organisationName);
	
	@Query("""
			SELECT DISTINCT o.* FROM organisation o 
			LEFT JOIN university_partner p 
			ON o.id = p.organisation_id
			WHERE o.university_id = :universityId OR p.university_id = :universityId
			""")
	Set<Organisation> findByUniversityId(int universityId);
}