package ujsu.controllers;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import lombok.RequiredArgsConstructor;
import ujsu.entities.Organisation;
import ujsu.entities.University;
import ujsu.entities.Vacancy;

@Controller
@RequiredArgsConstructor
@SessionAttributes({ "user", "university" })
public class VacancyController {

	@GetMapping("/vacancy")
	public String showVacancyPage(Model model, University university) {
		model.addAttribute("organisations", university.getOrganisations());
		Set<Vacancy> vacancies = new HashSet<>();
		for (Organisation o : university.getOrganisations()) {
			for (Vacancy v : o.getVacancies()) {
				v.setOrganisation(o);
				vacancies.add(v);
			}
		}
		model.addAttribute("vacancies", vacancies);
		return "vacancy";
	}
}