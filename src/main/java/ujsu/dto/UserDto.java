package ujsu.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import ujsu.enums.Role;
import ujsu.enums.Sex;

@Getter
@Setter
public class UserDto {

	private String email;
	private String password;
	private String password2;
	private String name;
	private String surname;
	private String lastName;
	
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private LocalDate birthDate;

	private Sex sex;
	private Role role;
}