package ujsu.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import lombok.RequiredArgsConstructor;
import ujsu.dto.StudentWithResponseDto;
import ujsu.entities.AdminProfile;
import ujsu.entities.Organisation;
import ujsu.entities.StudentProfile;
import ujsu.entities.University;
import ujsu.entities.User;
import ujsu.entities.Vacancy;
import ujsu.entities.VacancyResponse;
import ujsu.enums.Role;
import ujsu.exceptions.UnspecifiedRoleException;
import ujsu.repositories.UniversityRepository;
import ujsu.repositories.VacancyRepository;
import ujsu.repositories.VacancyResponseRepository;
import ujsu.services.OrganisationVacancyService;
import ujsu.services.UniversityVacancyService;
import ujsu.services.UserService;
import ujsu.services.VacancyService;

@Controller
@RequestMapping("/vacancies")
@RequiredArgsConstructor
public class VacancyController {

	private final UniversityRepository universityRepo;
	private final VacancyRepository vacancyRepo;
	private final VacancyResponseRepository vacancyResponseRepo;
	
	private final UserService userService;
	private final UniversityVacancyService universityVacancyService;
	private final OrganisationVacancyService organisationVacancyService;
	private final VacancyService vacancyService;

	@GetMapping
	public String showVacanciesPage(Model model, Authentication auth) throws UnspecifiedRoleException {
		User user = (User) auth.getPrincipal();
		return switch (user.getRole()) {
		case STUDENT -> {
			StudentProfile profile = ((StudentProfile) user.getProfile());
			int universityId = profile.getUniversityId();
	        University university = universityRepo.findById(universityId)
	            .orElseThrow(() -> new IllegalStateException("University not found"));
			List<Vacancy> vacancies = vacancyService.enrichWithCurrentStudentResponse(
					universityVacancyService.loadUniversityVacancies(university), user.getId());

			model.addAttribute("vacancies", vacancies);
			model.addAttribute("organisations", (HashSet<Organisation>) vacancies.stream().map(v -> v.getOrganisation())
					.collect(Collectors.toSet()));
			model.addAttribute("studentId", user.getId());
			yield "vacancies";
		}
		case ADMIN -> {
			Organisation adminOrg = ((AdminProfile) user.getProfile()).getOrganisation();
			List<Vacancy> vacancies = organisationVacancyService
					.loadOrganisationVacancies(adminOrg);
			model.addAttribute("vacancies", vacancies);
			model.addAttribute("adminOrg", adminOrg); 
			model.addAttribute("organisations", Collections.singleton(adminOrg));
			yield "vacancies_hr";
		}
		default -> throw new UnspecifiedRoleException();
		};
	}

	@GetMapping(value = "/filter", headers = "HX-Request=true")
	public String showSortedAndFilteredVacanciesFragment(Model model, Authentication auth,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String orgIds,
			@RequestParam(required = false) Integer minSalary,
			@RequestParam(required = false) Integer maxSalary,
			@RequestParam(required = false) String schedules) throws UnspecifiedRoleException {
		HashSet<Integer> orgIdsSet = new HashSet<>();
		if (orgIds != null && !orgIds.trim().isEmpty()) {
			try {
				String[] ids = orgIds.split(",");
				for (String id : ids) {
					orgIdsSet.add(Integer.parseInt(id.trim()));
				}
			} catch (NumberFormatException e) {
				System.err.println("Ошибка парсинга orgIds: " + orgIds);
			}
		}
		HashSet<String> schedulesSet = new HashSet<>();
		if (schedules != null && !schedules.trim().isEmpty()) {
			String[] scheduleArray = schedules.split(",");
			for (String s : scheduleArray) {
				schedulesSet.add(s.trim());
			}
		}
		User user = (User) auth.getPrincipal();
		Stream<Vacancy> vacancies;
		switch (user.getRole()) {
		case STUDENT -> {
			StudentProfile profile = ((StudentProfile) user.getProfile());
			int universityId = profile.getUniversityId();
	        University university = universityRepo.findById(universityId)
	            .orElseThrow(() -> new IllegalStateException("University not found"));
			vacancies = vacancyService.enrichWithCurrentStudentResponse(
					universityVacancyService.loadUniversityVacancies(university), user.getId()).stream();
		}
		case ADMIN -> {
			vacancies = organisationVacancyService
					.loadOrganisationVacancies(((AdminProfile) user.getProfile()).getOrganisation()).stream();
		}
		default -> throw new UnspecifiedRoleException();
		}
		
		if (search != null && !search.trim().isBlank()) {
			String lowCaseSearch = search.toLowerCase();
			vacancies = vacancies.filter(v -> v.getPosition().toLowerCase().contains(lowCaseSearch)
					|| (v.getDescription() != null && v.getDescription().toLowerCase().contains(lowCaseSearch))
					|| (v.getOrganisation() != null
							&& v.getOrganisation().getName().toLowerCase().contains(lowCaseSearch)));
		}
		if (!orgIdsSet.isEmpty()) {
			vacancies = vacancies
					.filter(v -> v.getOrganisation() != null && orgIdsSet.contains(v.getOrganisation().getId()));
		}
		if (minSalary != null) {
			vacancies = vacancies.filter(v -> v.getMinSalary() != null && v.getMinSalary() >= minSalary);
		}

		if (maxSalary != null) {
			vacancies = vacancies.filter(v -> v.getMaxSalary() != null && v.getMaxSalary() <= maxSalary);
		}
		if (!schedulesSet.isEmpty()) {
			vacancies = vacancies.filter(v -> {
				if (v.getSchedule() == null)
					return false;
				String schedule = v.getSchedule().toLowerCase();

				for (String selectedSchedule : schedulesSet) {
					switch (selectedSchedule) {
					case "full-time":
						if (schedule.contains("полная") || schedule.contains("full"))
							return true;
						break;
					case "part-time":
						if (schedule.contains("частич") || schedule.contains("part"))
							return true;
						break;
					case "internship":
						if (schedule.contains("стаж") || schedule.contains("intern"))
							return true;
						break;
					case "remote":
						if (schedule.contains("удален") || schedule.contains("remote"))
							return true;
						break;
					}
				}
				return false;
			});
		}
		List<Vacancy> vacancyList = vacancies.collect(Collectors.toList());
		model.addAttribute("vacancies", vacancyList);
		model.addAttribute("selectedSchedules", schedulesSet);
		model.addAttribute("selectedOrgIds", orgIdsSet);
		return "_fragments :: vacancies";
	}

	@GetMapping("/create")
	public String showCreationPage(Model model, Authentication auth) throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.ADMIN)
			throw new AccessDeniedException("Доступ запрещён.");
		Organisation adminOrg = ((AdminProfile) user.getProfile()).getOrganisation();
		model.addAttribute("emptyVacancy", new Vacancy());
		model.addAttribute("adminOrg", adminOrg);
		return "create-vacancy";
	}

	@PostMapping("/create")
	public String createVacancy(Model model, Authentication auth, @ModelAttribute Vacancy vacancy)
			throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.ADMIN)
			throw new AccessDeniedException("Доступ запрещён.");
		vacancy.setOrganisation(((AdminProfile) user.getProfile()).getOrganisation());
		vacancyRepo.save(vacancy);
		return "redirect:/vacancies";
	}
	
	@GetMapping("/{id}/edit")
	public String showEditionPage(Model model, Authentication auth, @PathVariable int id) throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.ADMIN)
			throw new AccessDeniedException("Доступ запрещён.");
		Organisation adminOrg = ((AdminProfile) user.getProfile()).getOrganisation();
		model.addAttribute("emptyVacancy", vacancyRepo.findById(id).orElseThrow());
		model.addAttribute("adminOrg", adminOrg);
		return "edit-vacancy";
	}

	@PostMapping("/{id}/edit")
	public String editVacancy(Model model, Authentication auth, @ModelAttribute Vacancy vacancy)
			throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.ADMIN)
			throw new AccessDeniedException("Доступ запрещён.");
		vacancy.setOrganisation(((AdminProfile) user.getProfile()).getOrganisation());
		vacancyRepo.save(vacancy);
		return "redirect:/vacancies";
	}
	
	@PostMapping("/{id}/delete")
	public String deleteVacancy(Model model, Authentication auth, @PathVariable int id)
			throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.ADMIN)
			throw new AccessDeniedException("Доступ запрещён.");
		vacancyRepo.deleteById(id);
		return "redirect:/vacancies";
	}

	@GetMapping(path = "/{id}/response", headers = "hx-request=true")
	public String responseVacancy(Model model, Authentication auth, @PathVariable int id) throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.STUDENT)
			throw new AccessDeniedException("Отправлять отклики могут только студенты.");
		boolean hasCurrentStudentResponse = vacancyResponseRepo.existsByStudentIdAndVacancyId(user.getId(), id);
		if (hasCurrentStudentResponse) {
			vacancyResponseRepo.deleteByStudentIdAndVacancyId(user.getId(), id);
			hasCurrentStudentResponse = false;
		} else {
			VacancyResponse response = new VacancyResponse();
			response.setStudentId(user.getId());
			response.setVacancyId(id);
			vacancyResponseRepo.save(response);
			hasCurrentStudentResponse = true;
		}
		return "_fragments :: btn-apply(vacancyId=" + id + ",hasCurrentStudentResponse=" + hasCurrentStudentResponse + ")";
	}
	
	@GetMapping(path = "/{id}/responses")
	public String showResponses(Model model, Authentication auth, @PathVariable int id)
			throws AccessDeniedException, NoSuchElementException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.ADMIN)
			throw new AccessDeniedException("Доступ запрещён.");
		Vacancy vacancy = vacancyRepo.findById(id).orElseThrow();
		model.addAttribute(vacancy);
		List<StudentWithResponseDto> studentWithResponseDtoList = new ArrayList<>();
		vacancyResponseRepo.findByVacancyId(id).forEach(r -> studentWithResponseDtoList.add(new StudentWithResponseDto(userService.loadUserWithProfileById(r.getStudentId()), r)));
		model.addAttribute("studentWithResponseDtoList", studentWithResponseDtoList);
		return "vacancy-responses";
	}
}