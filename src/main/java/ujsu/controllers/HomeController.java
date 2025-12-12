package ujsu.controllers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import ujsu.dto.SignInDto;
import ujsu.dto.SignUpDto;
import ujsu.entities.User;
import ujsu.enums.Role;
import ujsu.exceptions.AuthException;
import ujsu.services.ProfileService;
import ujsu.services.UniversityService;
import ujsu.services.UserService;

@Controller
@SessionAttributes("signUpDto")
@RequiredArgsConstructor
public class HomeController {

	private final UserService userService;
	private final ProfileService profileService;
	private final UniversityService universityService;

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
		return "sign-up";
	}

	@PostMapping("/sign-up")
	public String SignUpUser(HttpServletRequest request, Model model, SignUpDto signUpDto) {
		try {
			User user = userService.signUp(signUpDto);
			authenticateUserAndSetSession(user, request);
			model.addAttribute(user);
		} catch (AuthException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "sign-up";
		}
		return "redirect:/vacancies";
	}

	@GetMapping(path = "/fragments/profile-fields", headers = "hx-request=true")
	public String changeRoleAndGetProfileFields(Model model, @RequestParam(value = "role", required = false) String roleStr, SignUpDto signUpDto) {
		Role role;
		try {
			role = Role.valueOf(roleStr);
			signUpDto.changeRole(role);
			model.addAttribute(role);
		} catch (IllegalArgumentException e) {}
		return "_fragments :: profile-fields";
	}
	
	@GetMapping(path = "/fragments/get-universities-by-input", headers = "hx-request=true")
	public String getUniversitiesByInput(Model model, String input) {
		if (input.isBlank())
			return "fragments/university-suggestions :: start-typing";
		model.addAttribute("universities", universityService.findByNameMatch(input.trim(), 1));
		return "fragments/university-suggestions :: suggestions";
	}

	@GetMapping(path = "/fragments/get-specialities-by-input", headers = "hx-request=true")
	public String getSpecialitiesByInput(Model model, String input, String universityName) {
		if (input.isBlank()) 
			return "fragments/speciality-suggestions :: start-typing";
		model.addAttribute("specialities", universityService.findSpecialitiesByNameOrCodeMatch(input.trim(), universityName, 1));
		return "fragments/speciality-suggestions :: suggestions";
	}
	
	@GetMapping(path = "/error", headers = "hx-request=true")
	@ResponseBody
	public String showErrorPage() {
		return "";
	}
	
	@ModelAttribute
	public SignInDto createSignInDto() {
		return new SignInDto();
	}

	@ModelAttribute
	public SignUpDto createSignUpDto() {
		return new SignUpDto();
	}
	
	private void authenticateUserAndSetSession(User user, HttpServletRequest request) {
		user.setProfile(profileService.findProfile(user.getId(), user.getRole()));
	    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
	        user, null, user.getAuthorities());
	    SecurityContext context = SecurityContextHolder.getContext();
	    context.setAuthentication(auth);
	    HttpSession session = request.getSession(true);
	    session.setAttribute("SPRING_SECURITY_CONTEXT", context);
	}
}