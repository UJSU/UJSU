package ujsu.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ujsu.entities.University;
import ujsu.entities.Vacancy;
import ujsu.repositories.OrganisationRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UniversityVacancyService {

	private final OrganisationRepository organisationRepo;
	
	private final OrganisationVacancyService organisationVacancyService;
	
	@Cacheable(value = "university-vacancies", key = "#university.id")
    public List<Vacancy> loadUniversityVacancies(University university) {
        List<Vacancy> result = new ArrayList<>();
        organisationRepo.findWithPartnersByUniversityId(university.getId())
                .forEach(o -> result.addAll(organisationVacancyService.loadOrganisationVacancies(o.withUniversity(university))));
        return result;
    }
}