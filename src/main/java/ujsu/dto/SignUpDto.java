package ujsu.dto;

import lombok.Getter;
import lombok.Setter;
import ujsu.enums.Role;
import ujsu.exceptions.UnspecifiedRoleException;

@Getter
@Setter
public class SignUpDto {
	
	private UserDto userDto;
	private UserProfileDto profileDto;
	
	public SignUpDto(Role role) {
		userDto = new UserDto();
		
		if (role != null)
			userDto.setRole(role == null ? Role.STUDENT : role);
	
		profileDto = switch (userDto.getRole()) {
		case STUDENT -> new StudentProfileDto();
		case ADMIN -> new AdminProfileDto();
		default -> throw new UnspecifiedRoleException();
		};
	}
}