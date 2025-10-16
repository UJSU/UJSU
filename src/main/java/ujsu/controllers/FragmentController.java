package ujsu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import ujsu.services.UniversityService;

@Controller
@RequestMapping(path = "/fragments", headers = "hx-request=true")
@RequiredArgsConstructor
public class FragmentController {

	private final UniversityService universityService;

	@GetMapping("/get-universities-by-input")
	public String getUniversitiesByInput(Model model, String input) {
		if (input.isBlank())
			return "fragments/university-suggestions :: start-typing";
		model.addAttribute("universities", universityService.findByNameMatch(input.trim(), 1));
		return "fragments/university-suggestions :: suggestions";
	}

	@GetMapping("/get-specialities-by-input")
	public String getSpecialitiesByInput(Model model, String input, String universityName) {
		if (input.isBlank()) 
			return "fragments/speciality-suggestions :: start-typing";
		model.addAttribute("specialities", universityService.findSpecialitiesByNameOrCodeMatch(input.trim(), universityName, 1));
		return "fragments/speciality-suggestions :: suggestions";
	}
}