package ujsu.controllers;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import ujsu.entities.Organisation;
import ujsu.entities.StudentProfile;
import ujsu.entities.University;
import ujsu.entities.User;
import ujsu.entities.Vacancy;
import ujsu.enums.Role;
import ujsu.repositories.UniversityRepository;

@Controller
@RequiredArgsConstructor
public class VacancyController {

	private final UniversityRepository universityRepo;

	@GetMapping("/vacancy")
	public String showVacancyPage(Model model, Authentication auth) {
		User user = (User) auth.getPrincipal();
		System.out.println(user);
		Role role = user.getRole();
		if (role == Role.STUDENT) {
			University university = universityRepo.findById(((StudentProfile) user.getProfile()).getUniversityId()).get();
			model.addAttribute("organisations", university.getOrganisations());
			Set<Vacancy> vacancies = new HashSet<>();
			for (Organisation o : university.getOrganisations()) {
				o.setUniversity(university);
				for (Vacancy v : o.getVacancies()) {
					v.setOrganisation(o);
					vacancies.add(v);
				}
			}
			model.addAttribute("vacancies", vacancies);
			return "vacancy";
		}
		else if (role == Role.ADMIN) {
			return null;
		}
		return null;
	}
}