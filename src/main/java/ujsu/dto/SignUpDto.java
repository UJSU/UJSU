package ujsu.dto;

import lombok.Getter;
import lombok.Setter;
import ujsu.enums.Role;
import ujsu.enums.Sex;
import ujsu.exceptions.UnspecifiedRoleException;

@Getter
@Setter
public class SignUpDto {
	
	private UserDto userDto;
	private UserProfileDto profileDto;
	
	public SignUpDto(Role role) {
		userDto = new UserDto();
		
		switch (role) {
		case null:
			userDto.setSex(Sex.NULL);
			userDto.setRole(Role.STUDENT);
			profileDto = new StudentProfileDto();
			break;
		case Role.STUDENT:
			userDto.setRole(Role.STUDENT);
			profileDto = new StudentProfileDto();
			break;
		case Role.ADMIN:
			userDto.setRole(Role.ADMIN);
			profileDto = new AdminProfileDto();
			break;
		default:
			throw new UnspecifiedRoleException("Необработанная роль пользователя.");
		}
	}
}