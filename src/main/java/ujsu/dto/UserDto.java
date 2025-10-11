package ujsu.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ujsu.customValidators.annotations.EqualPasswordEntries;
import ujsu.enums.Role;
import ujsu.enums.Sex;

@Getter
@Setter
@NotBlank(message = "Заполните это поле.")
@EqualPasswordEntries
public class UserDto {
	
	@Email(message = "Некорректный адрес электронной почты.")
	@Size(max = 255, message = "Адрес электронной почты не должен быть длиннее 255 символов.")
	private String email;

	@Size(min=8, message="Пароль не должен быть короче 8 символов.")
	private String password;
	
	private String password2;
	private String name;
	private String surname;
	private String lastName;

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private LocalDate birthDate;

	private Sex sex;
	private Role role;
	
	
	UserDto() {
		sex = Sex.NULL;
		role = Role.STUDENT;
	}
}