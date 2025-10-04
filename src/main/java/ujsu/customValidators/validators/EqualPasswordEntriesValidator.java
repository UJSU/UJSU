package ujsu.customValidators.validators;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ujsu.customValidators.annotations.EqualPasswordEntries;
import ujsu.dto.UserDto;

public class EqualPasswordEntriesValidator implements ConstraintValidator<EqualPasswordEntries, UserDto> {

	@Override
	public void initialize(EqualPasswordEntries constraintAnnotation) {
	}

	@Override
	public boolean isValid(UserDto userDto, ConstraintValidatorContext context) {
		if (userDto.getPassword() == null || userDto.getPassword2() == null) 
			return true;
		if (!userDto.getPassword2().equals(userDto.getPassword())) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Пароли не совпадают.").addPropertyNode("password2")
					.addConstraintViolation();
			return false;
		}
		return true;
	}
}