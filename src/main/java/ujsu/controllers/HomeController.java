package ujsu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import ujsu.dto.SignInDto;
import ujsu.dto.SignUpDto;
import ujsu.entities.User;
import ujsu.enums.Role;
import ujsu.exceptions.AuthException;
import ujsu.services.UserService;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final UserService userService;

	@GetMapping("/")
	public String showMainPage() {
		return "redirect:/sign-in";
	}

	@GetMapping("/sign-in")
	public String showSignInPage(HttpSession session, Model model, SignInDto signInDto) {
		model.addAttribute("errorMessage", session.getAttribute("errorMessage"));
		session.removeAttribute("errorMessage");
		return "sign-in";
	}

	@GetMapping("/sign-up")
	public String showSignUpPage(Model model) {
		model.addAttribute(new SignUpDto(Role.STUDENT));
		return "sign-up";
	}

	@PostMapping("/sign-up")
	public String SignUpUser(Model model, SignUpDto signUpDto) {
		try {
			User user = userService.signUp(signUpDto);
			userService.authorizeUser(user.getEmail());
			model.addAttribute(user);
		} catch (AuthException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "sign-up";
		}
		return "redirect:/vacancy";
	}

	@ModelAttribute
	public SignInDto createSignInDto() {
		return new SignInDto();
	}
}