package ujsu.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ujsu.entities.Vacancy;
import ujsu.repositories.VacancyResponseRepository;

@Service
@RequiredArgsConstructor
public class VacancyService {

	private final VacancyResponseRepository vacancyResponseRepo;

	public List<Vacancy> enrichWithCurrentStudentResponse(List<Vacancy> vacancies, int studentId) {
		return vacancies.stream()
				.map(v -> v.withHasCurrentStudentResponse(
						vacancyResponseRepo.existsByStudentIdAndVacancyId(studentId, v.getId())))
				.collect(Collectors.toList());
	}
}