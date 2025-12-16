package ujsu.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ujsu.entities.Organisation;
import ujsu.entities.Vacancy;
import ujsu.repositories.VacancyRepository;

@Service
@RequiredArgsConstructor
public class OrganisationVacancyService {

	private final VacancyRepository vacancyRepo;

	@Cacheable(value = "organisation-vacancies", key = "#organisation.id")
	public List<Vacancy> loadOrganisationVacancies(Organisation organisation) {
		List<Vacancy> result = new ArrayList<>();
		vacancyRepo.findByOrganisationIdWithResponseCount(organisation.getId()).forEach(v -> {
			v.setOrganisation(organisation);
			result.add(v);
		});
		return result;
	}
}