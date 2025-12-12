package ujsu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("/profile")
public class ProfileController {

	@GetMapping
	public String showProfile() {
		return "profile";
	}
}