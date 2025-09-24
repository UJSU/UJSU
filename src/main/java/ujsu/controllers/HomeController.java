package ujsu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import lombok.RequiredArgsConstructor;
import ujsu.dto.SignInDto;
import ujsu.dto.SignUpDto;
import ujsu.dto.StudentProfileDto;
import ujsu.services.UserService;

@Controller
@RequiredArgsConstructor
@SessionAttributes("user")
public class HomeController {

	public final UserService userService;

	@GetMapping("/")
	public String showMainPage() {
		return "index";
	}

	@PostMapping("/sign-in")
	public String signIn(Model model, @ModelAttribute SignInDto signInDto) {
		try {
			model.addAttribute(userService.signIn(signInDto));
		} catch (RuntimeException e) {
			model.addAttribute("messsage", e.getMessage()); // TODO bind to fields
			return "index";
		}
		return "redirect:/vacancy";
	}

	@GetMapping("/sign-up")
	public String showSignUpPage(@ModelAttribute SignUpDto signUpDto, @ModelAttribute StudentProfileDto profileDto) {
		return "sign-up";
	}
	
	@PostMapping("/sign-up")
	public String SignUpUser(@ModelAttribute SignUpDto signUpDto, @ModelAttribute StudentProfileDto profileDto) {
		userService.signUp(signUpDto, profileDto);
		return "redirect:/vacancy";
	}
	
	@ModelAttribute
	public SignInDto createSignInDto() {
		return new SignInDto();
	}
}