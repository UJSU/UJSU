package ujsu.mappers;

import org.mapstruct.Mapper;

import ujsu.dto.SignInDto;
import ujsu.dto.SignUpDto;
import ujsu.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	User fromSignUpDto(SignUpDto dto);

	User fromSignInDto(SignInDto dto);
}