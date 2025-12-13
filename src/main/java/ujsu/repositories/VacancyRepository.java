package ujsu.repositories;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ujsu.entities.Vacancy;

@Repository
public interface VacancyRepository extends CrudRepository<Vacancy, Integer> {
	@Query("""
			SELECT v.*, COUNT(vr.id) AS response_count
			FROM vacancy v
			LEFT JOIN vacancy_response vr ON v.id = vr.vacancy_id
			WHERE v.organisation_id = :organisationId
			GROUP BY v.id, v.organisation_id, v.position, v.min_salary, v.max_salary, v.schedule, v.description
			""")
	List<Vacancy> findByOrganisationIdWithResponseCount(int organisationId);
	
	boolean existsByIdAndOrganisationId(int id, int universityId);
}