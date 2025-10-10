package ujsu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ujsu.dto.SignInDto;
import ujsu.dto.SignUpDto;
import ujsu.entities.User;
import ujsu.enums.Role;
import ujsu.enums.Sex;
import ujsu.enums.StudyType;
import ujsu.exceptions.InvalidPasswordException;
import ujsu.exceptions.UserNotFoundException;
import ujsu.services.UserService;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"user", "university"})
@Slf4j
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
			model.addAttribute("errorMessage", "Неверный адрес электронной почты или пароль.");
			return "sign-in";
		} catch (InvalidPasswordException e) {
			model.addAttribute("errorMessage", "Неверный адрес электронной почты или пароль.");
			return "sign-in";
		}
		return "redirect:/vacancy";
	}

	@GetMapping("/sign-up")
public String showSignUpPage(Model model) {
    model.addAttribute("signUpDto", new SignUpDto(Role.STUDENT));
    model.addAttribute("Role", Role.class); // Добавляем класс Role в модель
    return "sign-up";
}
	
	@PostMapping("/sign-up")
	public String SignUpUser(@ModelAttribute SignUpDto signUpDto) {
		userService.signUp(signUpDto);
		return "redirect:/vacancy";
	}

	@GetMapping("/fragments/profile-fields")
public String getProfileFields(@RequestParam(value = "role", required = false) String roleStr, Model model) {
    log.info("Received role parameter: {}", roleStr);
    Role role = null; 
    
    if (roleStr != null && !roleStr.isEmpty()) {
        try {
            role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role '{}', using null", roleStr);
            role = null;
        }
    }
    log.info("Using role: {}", role);
    model.addAttribute("role", role);
    model.addAttribute("Role", Role.class);
    model.addAttribute("studyTypes", StudyType.values());
    model.addAttribute("sexes", Sex.values());
    return "_fragments :: profile-fields";
}
	@ModelAttribute
	public SignInDto createSignInDto() {
		return new SignInDto();
	}
}