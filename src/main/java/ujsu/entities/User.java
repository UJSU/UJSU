package ujsu.entities;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ujsu.enums.Role;
import ujsu.enums.Sex;

@Data
@RequiredArgsConstructor
public class User {

	@Id
	private Integer id;

	private String email;
	private String hashedPassword;
	private String name;
	private String surname;
	private String lastName;

	private LocalDate birthDate;
	private LocalDate signUpDate;


	private Integer sex;
	private Integer role;
	
	@Transient
	private UserProfile profile;
	
	public Sex getSex() {
        return Sex.fromCode(sex);
    }

    public void setSex(Sex sex) {
        this.sex = sex.getCode();
    }
    
    public Role getRole() {
        return Role.fromCode(role);
    }

    public void setRole(Role role) {
        this.role = role.getCode();
    }
}