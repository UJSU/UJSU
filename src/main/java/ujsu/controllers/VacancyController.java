package ujsu.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import ujsu.entities.AdminProfile;
import ujsu.entities.Organisation;
import ujsu.entities.StudentProfile;
import ujsu.entities.University;
import ujsu.entities.User;
import ujsu.entities.Vacancy;
import ujsu.entities.VacancyResponse;
import ujsu.enums.Role;
import ujsu.exceptions.UnspecifiedRoleException;
import ujsu.repositories.VacancyRepository;
import ujsu.repositories.VacancyResponseRepository;

@Controller
@RequestMapping("/vacancies")
@RequiredArgsConstructor
public class VacancyController {

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
			model.addAttribute("studentId", user.getId());
			yield "vacancies";
		}
		case ADMIN -> {
			yield "vacancies";
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
			@RequestParam(required = false) String schedules) {
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
		University university = ((StudentProfile) user.getProfile()).getUniversity();
		model.addAttribute("organisations", university.getOrganisations());
		Stream<Vacancy> vacancies = loadUniversityVacancies(university).stream();
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
				if (v.getShedule() == null)
					return false;
				String schedule = v.getShedule().toLowerCase();

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
	public String createVacancy(Model model, Authentication auth, @ModelAttribute Vacancy vacancy)
			throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		vacancy.setOrganisation(((AdminProfile) user.getProfile()).getOrganisation());
		vacancyRepo.save(vacancy);
		return "vacancies";
	}

	@GetMapping(path = "/{id}/response", headers = "hx-request=true")
	public String responseVacancy(Model model, Authentication auth, @PathVariable int id) throws AccessDeniedException {
		User user = (User) auth.getPrincipal();
		if (user.getRole() != Role.STUDENT)
			throw new AccessDeniedException("Отправлять отклики могут только студенты.");
		model.addAttribute("studentId", user.getId());
		model.addAttribute("vacancyId", id);
		boolean hasResponse = vacancyResponseRepo.existsByStudentIdAndVacancyId(user.getId(), id);
		if (hasResponse) {
			vacancyResponseRepo.deleteByStudentIdAndVacancyId(user.getId(), id);
			return "fragments :: btn-apply";
		} else {
			VacancyResponse response = new VacancyResponse();
			response.setStudentId(user.getId());
			response.setVacancyId(id);
			vacancyResponseRepo.save(response);
			return "fragments :: btn-sent";
		}
	}

	private List<Vacancy> loadUniversityVacancies(University university) {
		List<Vacancy> result = new ArrayList<>();
		university.getOrganisations().forEach(o -> result.addAll(loadOrganisationVacancies(o)));
		return result;
	}

	private List<Vacancy> loadOrganisationVacancies(Organisation organisation) {
		List<Vacancy> result = new ArrayList<>();
		organisation.getVacancies().forEach(v -> result.add(v));
		return result;
	}
}