package ujsu.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ujsu.entities.VacancyResponse;

@Repository
public interface VacancyResponseRepository extends CrudRepository<VacancyResponse, Integer> {
	
	List<VacancyResponse> findByStudentId(int studentId);
	
	List<VacancyResponse> findByVacancyId(int vacancyId);
}