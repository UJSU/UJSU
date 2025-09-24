package ujsu.entities;

import java.time.LocalDate;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ujsu.enums.Role;
import ujsu.enums.Sex;

@Data
@RequiredArgsConstructor
public class User {

	private int id;

	private String email;
	private String hashedPassword;
	private String name;
	private String surname;
	private String lastName;

	private LocalDate birthDate;
	private LocalDate registrationDate;

	private Sex sex;

	private Role role;
	private UserProfile profile;
}