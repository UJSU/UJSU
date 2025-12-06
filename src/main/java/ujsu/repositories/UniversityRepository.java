package ujsu.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import ujsu.entities.Speciality;
import ujsu.entities.University;

public interface UniversityRepository extends CrudRepository<University, Integer> {
	Optional<University> findByName(String name);

	@Query("SELECT u.* FROM University u WHERE "
			+ "REGEXP_LIKE(u.name, CONCAT('.*\\\\b', :input)) "
			+ "OR REGEXP_LIKE(u.short_name, CONCAT('.*\\\\b', :input))"
			+ "ORDER BY u.name LIMIT :offset, :pageLength")
	List<University> findByNameMatch(String input, int offset, int pageLength);

	@Query("SELECT s.* FROM Speciality s JOIN Speciality_University su ON s.id = su.speciality_id JOIN University u ON su.university_id = u.id WHERE "
			+ "u.name = :universityName "
			+ "AND (REGEXP_LIKE(s.name, CONCAT('.*\\\\b', :input)) OR REGEXP_LIKE(s.code, CONCAT('.*\\\\b', :input))) "
			+ "ORDER BY s.name LIMIT :offset, :pageLength")
	List<Speciality> findSpecialitiesByNameOrCodeMatch(String input, String universityName, int offset, int pageLength);
}