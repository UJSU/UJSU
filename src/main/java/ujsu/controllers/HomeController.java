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
import ujsu.entities.User;
import ujsu.enums.Role;
import ujsu.exceptions.InvalidPasswordException;
import ujsu.exceptions.UserNotFoundException;
import ujsu.services.UserService;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"user", "university"})
public class HomeController {

	private final UserService userService;

	@GetMapping("/")
	public String showMainPage() {
		return "redirect:/sign-in";
	}

	@GetMapping("/sign-in")
	public String showSignInPage(SignInDto signInDto) {
		return "sign-in";
	}

	@PostMapping("/sign-in")
	public String signIn(Model model, SignInDto signInDto) {
		try {
			User user = userService.signIn(signInDto);
			model.addAttribute(user);
			model.addAttribute(userService.getStudentUniversity(user));
		} catch (UserNotFoundException e) {
			model.addAttribute("errorMessage", "Неверный адрес электронной почты или пароль."); // TODO bind to fields
			return "sign-in";
		} catch (InvalidPasswordException e) {
			model.addAttribute("errorMessage", "Неверный адрес электронной почты или пароль."); // TODO bind to fields
			return "sign-in";
		}
		return "redirect:/vacancy";
	}

	@GetMapping("/sign-up")
	public String showSignUpPage(Model model) {
		model.addAttribute(new SignUpDto(Role.STUDENT));
		return "sign-up";
	}
	
	@PostMapping("/sign-up")
	public String SignUpUser(SignUpDto signUpDto) {
		userService.signUp(signUpDto);
		return "redirect:/vacancy";
	}

	@ModelAttribute
	public SignInDto createSignInDto() {
		return new SignInDto();
	}
}