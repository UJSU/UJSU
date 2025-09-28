package ujsu.mappers;

import org.mapstruct.Mapper;

import ujsu.dto.SignInDto;
import ujsu.dto.UserDto;
import ujsu.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	User fromDto(UserDto dto);

	User fromDto(SignInDto dto);
}