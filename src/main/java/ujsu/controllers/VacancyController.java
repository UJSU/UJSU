package ujsu.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import ujsu.entities.AdminProfile;
import ujsu.entities.Organisation;
import ujsu.entities.StudentProfile;
import ujsu.entities.University;
import ujsu.entities.User;
import ujsu.entities.Vacancy;
import ujsu.entities.VacancyResponse;
import ujsu.enums.Role;
import ujsu.exceptions.UnspecifiedRoleException;
import ujsu.repositories.OrganisationRepository;
import ujsu.repositories.VacancyRepository;
import ujsu.repositories.VacancyResponseRepository;

@Controller
@RequestMapping("/vacancies")
@RequiredArgsConstructor
public class VacancyController {

	private final OrganisationRepository organisationRepo;
	private final VacancyRepository vacancyRepo;
	private final VacancyResponseRepository vacancyResponseRepo;

	@GetMapping
	public String showVacanciesPage(Model model, Authentication auth) throws UnspecifiedRoleException {
		User user = (User) auth.getPrincipal();
		return switch (user.getRole()) {
		case STUDENT -> {
			University university = ((StudentProfile) user.getProfile()).getUniversity();
			model.addAttribute("organisations", university.getOrganisations());
			List<Vacancy> vacancies = loadUniversityVacancies(university);
			model.addAttribute("vacancies", vacancies);
			yield "vacancies";
		}
		case ADMIN -> {

			yield "vacancies";
		}
		default -> throw new UnspecifiedRoleException();
		};
	}

	// TODO: Add other filters
	@GetMapping(headers = "hx-request=true")
	public String showSortedAndFiltereVacanciesFragment(Model model, Authentication auth,
			@RequestParam(required = false) String search, @RequestParam(required = false) HashSet<Integer> orgIds,
			@RequestParam(required = false) Integer minSalary, @RequestParam(required = false) Integer maxSalary) {
		User user = (User) auth.getPrincipal();
		University university = ((StudentProfile) user.getProfile()).getUniversity();
		model.addAttribute("organisations", university.getOrganisations());
		Stream<Vacancy> vacancies = loadUniversityVacancies(university).stream();

		if (search != null && !search.trim().isBlank()) {
			String lowCaseSearch = search.toLowerCase();
			vacancies = vacancies.filter(v -> v.getPosition().toLowerCase().contains(lowCaseSearch));
		}
		vacancies = vacancies.filter(v -> orgIds == null || orgIds.isEmpty() || orgIds.contains(v.getOrganisationId()))
				.filter(v -> minSalary == null || v.getMinSalary() >= minSalary)
				.filter(v -> maxSalary == null || v.getMaxSalary() <= maxSalary);

		model.addAttribute("vacancies", vacancies.collect(Collectors.toList()));
		return "fragments :: vacancies";
	}

	@GetMapping("/create")
	public String showCreationPage(Model model, Authentication auth) throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.ADMIN)
			throw new AccessDeniedException("Доступ запрещён.");
		model.addAttribute("emptyVacancy", new Vacancy());
		return "create-vacancy";
	}
	
	@PostMapping("/create")
	public String createVacancy(Model model, Authentication auth, @ModelAttribute Vacancy vacancy) throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		vacancy.setOrganisation(((AdminProfile) user.getProfile()).getOrganisation());
		vacancyRepo.save(vacancy);
		return "vacancies";
	}

	@GetMapping(path = "/{id}/response", headers = "hx-request=true")
	public String responseVacancy(Model model, Authentication auth, Integer id) throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.STUDENT)
			throw new AccessDeniedException("Отправлять отклики могут только студенты.");
		if (!vacancyRepo.existsByIdAndUniversityId(id, ((StudentProfile) user.getProfile()).getUniversityId()))
			throw new AccessDeniedException("Этой вакансии не существует или доступна студентам другого ВУЗа.");
		VacancyResponse response = new VacancyResponse();
		response.setStudentId(user.getId());
		response.setVacancyId(id);
		vacancyResponseRepo.save(response);
		return "fragments :: btn-sent";
	}

	private List<Vacancy> loadUniversityVacancies(University university) {
		List<Vacancy> result = new ArrayList<>();
		university.getOrganisations()
				.forEach(o -> result.addAll(loadOrganisationVacancies(o)));
		return result;
	}

	private List<Vacancy> loadOrganisationVacancies(Organisation organisation) {
		List<Vacancy> result = new ArrayList<>();
		organisation.getVacancies().forEach(v -> result.add(v));
		return result;
	}
}