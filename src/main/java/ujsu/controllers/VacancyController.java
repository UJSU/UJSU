package ujsu.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import ujsu.entities.Organisation;
import ujsu.entities.StudentProfile;
import ujsu.entities.University;
import ujsu.entities.User;
import ujsu.entities.Vacancy;
import ujsu.enums.Role;
import ujsu.repositories.VacancyRepository;

@Controller
@RequestMapping("/vacancies")
@RequiredArgsConstructor
public class VacancyController {

	private final VacancyRepository vacancyRepo;

	@GetMapping
	public String showVacanciesPage(Model model, Authentication auth) {
		User user = (User) auth.getPrincipal();
		Role role = user.getRole();
		if (role == Role.STUDENT) {
			University university = ((StudentProfile) user.getProfile()).getUniversity();
			model.addAttribute("organisations", university.getOrganisations());
			List<Vacancy> vacancies = loadUniversityVacancies(university);
			model.addAttribute("vacancies", vacancies);
			return "vacancies";
		} else if (role == Role.ADMIN) {
			return null;
		}
		return null;
	}

	// TODO: Add other filters
	@GetMapping(headers = "hx-request=true")
	public String showSortedAndFiltereVacanciesFragment(Model model, Authentication auth,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) HashSet<Integer> orgIds,
			@RequestParam(required = false) Integer minSalary,
			@RequestParam(required = false) Integer maxSalary) {
		User user = (User) auth.getPrincipal();
		University university = ((StudentProfile) user.getProfile()).getUniversity();
		model.addAttribute("organisations", university.getOrganisations());
		Stream<Vacancy> vacancies = loadUniversityVacancies(university).stream();

		if (search != null && !search.trim().isBlank()) {
			String lowCaseSearch = search.toLowerCase();
			vacancies.filter(v -> v.getPosition().toLowerCase().contains(lowCaseSearch));
		}
		vacancies = vacancies.filter(v -> orgIds == null || orgIds.isEmpty() || orgIds.contains(v.getOrganisationId()))
				.filter(v -> minSalary == null || v.getMinSalary() >= minSalary)
				.filter(v -> maxSalary == null || v.getMaxSalary() <= maxSalary);

		model.addAttribute("vacancies", vacancies.collect(Collectors.toList()));
		return "fragments :: vacancies";
	}

	@GetMapping("/{id}")
	public String showVacancyPage(Model model, Authentication auth, int id) {
		model.addAttribute(vacancyRepo.findById(id).orElseThrow());
		User user = (User) auth.getPrincipal();
		Role role = user.getRole();
		if (role == Role.STUDENT)
			model.addAttribute("canResponse",
					vacancyRepo.existsByIdAndOrganisationId(id, ((StudentProfile) user.getProfile()).getUniversityId()));
		return "vacancy";
	}

	private List<Vacancy> loadUniversityVacancies(University university) {
		List<Vacancy> result = new ArrayList<>();
		for (Organisation organisation : university.getOrganisations()) {
			Organisation enrichedOrganisation = organisation.withUniversity(university);
			for (Vacancy vacancy : organisation.getVacancies()) {
				Vacancy enrichedVacancy = vacancy.withOrganisation(enrichedOrganisation);
				result.add(enrichedVacancy);
			}
		}
		return result;
	}
}