package ujsu.dto;

import lombok.Getter;
import lombok.Setter;
import ujsu.enums.Role;

@Getter
@Setter
public class SignUpDto {

	private UserDto userDto;
	private UserProfileDto profileDto;

	public SignUpDto() {
		userDto = new UserDto();
	}

	public void changeRole(Role role) {
		userDto.setRole(role);
		profileDto = switch (role) {
		case null -> null;
		case STUDENT -> new StudentProfileDto();
		case ADMIN -> new AdminProfileDto();
		};
	}
}