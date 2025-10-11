package ujsu.services;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ujsu.dto.AdminProfileDto;
import ujsu.dto.StudentProfileDto;
import ujsu.dto.UserProfileDto;
import ujsu.entities.AdminProfile;
import ujsu.entities.StudentProfile;
import ujsu.entities.UserProfile;
import ujsu.enums.Role;
import ujsu.exceptions.UnspecifiedRoleException;
import ujsu.mappers.UserProfileMapper;
import ujsu.repositories.AdminProfileRepository;
import ujsu.repositories.OrganisationRepository;
import ujsu.repositories.SpecialityRepository;
import ujsu.repositories.StudentProfileRepository;
import ujsu.repositories.UniversityRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private final StudentProfileRepository studentProfileRepo;
	private final AdminProfileRepository adminProfileRepo;
	private final UniversityRepository universityRepo;
	private final SpecialityRepository specialityRepo;
	private final OrganisationRepository organisationRepo;
	
	private final UserProfileMapper profileMapper;

	public UserProfile findProfile(int userId, Role role) {
		return switch (role) {
		case STUDENT -> {
			StudentProfile profile = studentProfileRepo.findByUserId(userId);
			profile.setUniversity(universityRepo.findById(profile.getUniversityId()).get());
			profile.setSpeciality(specialityRepo.findById(profile.getSpecialityId()).get());
			yield profile;
		}
		case ADMIN -> {
			AdminProfile profile = adminProfileRepo.findByUserId(userId);
			profile.setOrganisation(organisationRepo.findById(profile.getOrganisationId()).get());
			yield profile;
		}
		default -> throw new UnspecifiedRoleException();
		};
	}
	
	public UserProfile createProfileFromDto(int userId, Role role, UserProfileDto profileDto) {
		return switch (role) {
		case STUDENT -> {
			StudentProfile studentProfile = profileMapper.createStudentProfile((StudentProfileDto)profileDto);
			studentProfile.setUserId(userId);
			
			
			// ВРЕМЕННО - ТОЛЬКО ДЛЯ ОТЛАДКИ
			// TODO: заменить на рабочий код
			studentProfile.setUniversityId(0);
			studentProfile.setSpecialityId(0);
			
			
			yield studentProfileRepo.save(studentProfile);
		}
		case ADMIN -> {
			AdminProfile adminProfile = profileMapper.createAdminProfile((AdminProfileDto)profileDto);
			adminProfile.setUserId(userId);
			
			
			// ВРЕМЕННО - ТОЛЬКО ДЛЯ ОТЛАДКИ
			// TODO: заменить на рабочий код
			adminProfile.setOrganisationId(0);
			
			
			yield adminProfileRepo.save(adminProfile);
		}
		default -> throw new UnspecifiedRoleException();
		};
	}
}