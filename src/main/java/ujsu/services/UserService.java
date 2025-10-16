package ujsu.services;

import java.time.LocalDate;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ujsu.dto.SignUpDto;
import ujsu.dto.UserDto;
import ujsu.entities.User;
import ujsu.exceptions.AuthException;
import ujsu.mappers.UserMapper;
import ujsu.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private final ProfileService profileService;

	private final UserRepository userRepo;

	private final UserMapper userMapper;

	private final PasswordEncoder passwordEncoder;

	@Transactional
	public User signUp(SignUpDto signUpDto) throws AuthException {
		UserDto userDto = signUpDto.getUserDto();
		if (userRepo.existsByEmail(userDto.getEmail()))
			throw new AuthException("Пользователь с указанной почтой уже зарегистрирован.");
		User user = userMapper.fromDto(userDto);
		user.setHashedPassword(passwordEncoder.encode(userDto.getPassword()));
		user.setSignUpDate(LocalDate.now());
		user = userRepo.save(user);
		user.setProfile(profileService.saveProfileFromDto(user.getId(), user.getRole(), signUpDto.getProfileDto()));
		return user;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepo.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
		user.setProfile(profileService.findProfile(user.getId(), user.getRole()));
		return user;
	}
}