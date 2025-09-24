package ujsu.services;

import java.time.LocalDate;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ujsu.dto.AdminProfileDto;
import ujsu.dto.SignInDto;
import ujsu.dto.SignUpDto;
import ujsu.dto.StudentProfileDto;
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
import lombok.RequiredArgsConstructor;
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

	public User signUp(SignUpDto userDto, UserProfileDto profileDto) {
		if (userSignedUp(userDto.getEmail()))
			throw new UserSignedUpException("Пользователь с этой почтой уже зарегистрирован.");
		User user = userMapper.fromSignUpDto(userDto);
		user.setRegistrationDate(LocalDate.now());
		user = userRepo.save(user);
		UserProfile profile;
		switch (user.getRole()) {
		case Role.STUDENT:
			profile = profileMapper.createStudentProfile((StudentProfileDto)profileDto);
			profile.setUserId(user.getId());
			profile = studentProfileRepo.save((StudentProfile)profile);
			break;
		case Role.ADMIN:
			profile = profileMapper.createAdminProfile((AdminProfileDto)profileDto);
			profile.setUserId(user.getId());
			profile = adminProfileRepo.save((AdminProfile)profile);
			break;
		default:
			throw new UnspecifiedRoleException("Необработанная роль пользователя.");
		}
		user.setProfile(profile);
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

	public boolean userSignedUp(String email) {
		return userRepo.findByEmail(email).isPresent();
	}
}