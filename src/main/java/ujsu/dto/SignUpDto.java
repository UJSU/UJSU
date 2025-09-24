package ujsu.dto;

import java.util.Date;

import ujsu.enums.Role;
import ujsu.enums.Sex;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpDto {
	
	private String email;
	private String password;
	private String name;
	private String surname;
	private String lastName;

	private Date birthDate;

	private Sex sex;
	private Role role;
}