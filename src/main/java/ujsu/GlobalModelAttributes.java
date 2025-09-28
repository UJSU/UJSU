package ujsu;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import ujsu.enums.Role;
import ujsu.enums.Sex;
import ujsu.enums.StudyType;

@ControllerAdvice
public class GlobalModelAttributes {

	@ModelAttribute("sexes")
    public Sex[] getSexes() {
        return Sex.values();
    }
	
    @ModelAttribute("roles")
    public Role[] getUserRoles() {
        return Role.values();
    }
    
    @ModelAttribute("studyTypes")
    public StudyType[] getStudyTypes() {
        return StudyType.values();
    }
}