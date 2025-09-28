package ujsu.services;

import java.time.LocalDate;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ujsu.dto.AdminProfileDto;
import ujsu.dto.SignInDto;
import ujsu.dto.SignUpDto;
import ujsu.dto.StudentProfileDto;
import ujsu.dto.UserDto;
import ujsu.dto.UserProfileDto;
import ujsu.entities.AdminProfile;
import ujsu.entities.StudentProfile;
import ujsu.entities.User;
import ujsu.entities.UserProfile;
import ujsu.enums.Role;
import ujsu.exceptions.InvalidPasswordException;
import ujsu.exceptions.UnspecifiedRoleException;
import ujsu.exceptions.UserNotFoundException;
import ujsu.exceptions.UserSignedUpException;
import ujsu.mappers.UserMapper;
import ujsu.mappers.UserProfileMapper;
import ujsu.repositories.AdminProfileRepository;
import ujsu.repositories.StudentProfileRepository;
import ujsu.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepo;
	private final StudentProfileRepository studentProfileRepo;
	private final AdminProfileRepository adminProfileRepo;

	private final UserMapper userMapper;
	private final UserProfileMapper profileMapper;

	public User signUp(SignUpDto signUpDto) {
		
		UserDto userDto = signUpDto.getUserDto();
		UserProfileDto profileDto = signUpDto.getProfileDto();
		
		if (userRepo.existsByEmail(userDto.getEmail()))
			throw new UserSignedUpException("Пользователь с этой почтой уже зарегистрирован.");
		User user = userMapper.fromDto(userDto);
		user.setHashedPassword(BCrypt.hashpw(userDto.getPassword(), BCrypt.gensalt()));
		user.setSignUpDate(LocalDate.now());
		user = userRepo.save(user);
		UserProfile profile;
		switch (user.getRole()) {
		case Role.STUDENT:
			StudentProfile studentProfile = profileMapper.createStudentProfile((StudentProfileDto)profileDto);
			studentProfile.setUserId(user.getId());
			
			
			// ВРЕМЕННО - ТОЛЬКО ДЛЯ ОТЛАДКИ
			// TODO: заменить на рабочий код
			studentProfile.setUniversityId(0);
			studentProfile.setSpecialityId(0);
			
			
			profile = studentProfileRepo.save((StudentProfile)studentProfile);
			break;
		case Role.ADMIN:
			AdminProfile adminProfile = profileMapper.createAdminProfile((AdminProfileDto)profileDto);
			adminProfile.setUserId(user.getId());
			
			
			// ВРЕМЕННО - ТОЛЬКО ДЛЯ ОТЛАДКИ
			// TODO: заменить на рабочий код
			adminProfile.setOrganisationId(0);
			
			
			profile = adminProfileRepo.save((AdminProfile)adminProfile);
			break;
		default:
			throw new UnspecifiedRoleException("Необработанная роль пользователя.");
		}
		profile.setUserId(user.getId());
		return user;
	}

	public User signIn(SignInDto dto) {
		User user = userRepo.findByEmail(dto.getEmail())
				.orElseThrow(() -> new UserNotFoundException("Пользователь не найден."));
		
		if (!BCrypt.checkpw(dto.getPassword(), user.getHashedPassword()))
			throw new InvalidPasswordException("Неверный пароль.");
		switch (user.getRole()) {
		case Role.STUDENT:
			user.setProfile(studentProfileRepo.findUserProfileByUserId(user.getId()));
			break;
		case Role.ADMIN:
			user.setProfile(adminProfileRepo.findUserProfileByUserId(user.getId()));
			break;
		}
		return user;
	}
}