package ujsu.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import ujsu.entities.Organisation;
import ujsu.entities.University;


public interface OrganisationRepository extends CrudRepository<Organisation, Integer> {
	
	Optional<Organisation> findByName(String organisationName);
	
	@Query("""
			SELECT DISTINCT o.* FROM organisation o 
			LEFT JOIN university_partner p 
			ON o.id = p.organisation_id
			WHERE o.university_id = :universityId OR p.university_id = :universityId
			""")
	Set<Organisation> findWithPartnersByUniversityId(int universityId);
	
	@Query("SELECT o.* FROM organisation o WHERE "
			+ "REGEXP_LIKE(o.name, CONCAT('.*\\\\b', :input)) "
			+ "OR REGEXP_LIKE(o.short_name, CONCAT('.*\\\\b', :input))"
			+ "ORDER BY o.name LIMIT :offset, :pageLength")
	List<University> findByNameMatch(String input, int offset, int pageLength);
}