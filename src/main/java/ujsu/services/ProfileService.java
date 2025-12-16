package ujsu.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ujsu.dto.AdminProfileDto;
import ujsu.dto.StudentProfileDto;
import ujsu.dto.UserProfileDto;
import ujsu.entities.AdminProfile;
import ujsu.entities.Organisation;
import ujsu.entities.Speciality;
import ujsu.entities.StudentProfile;
import ujsu.entities.University;
import ujsu.entities.UserProfile;
import ujsu.entities.Vacancy;
import ujsu.enums.Role;
import ujsu.exceptions.UnspecifiedRoleException;
import ujsu.mappers.UserProfileMapper;
import ujsu.repositories.AdminProfileRepository;
import ujsu.repositories.OrganisationRepository;
import ujsu.repositories.SpecialityRepository;
import ujsu.repositories.StudentProfileRepository;
import ujsu.repositories.UniversityRepository;
import ujsu.repositories.VacancyResponseRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private final StudentProfileRepository studentProfileRepo;
	private final AdminProfileRepository adminProfileRepo;
	private final UniversityRepository universityRepo;
	private final SpecialityRepository specialityRepo;
	private final OrganisationRepository organisationRepo;
	private final VacancyResponseRepository vacancyResponseRepo;

	private final UserProfileMapper profileMapper;

	public UserProfile findProfile(int userId, Role role) {
		return switch (role) {
		case STUDENT -> {
			StudentProfile profile = studentProfileRepo.findByUserId(userId);
			profile.setUniversity(universityRepo.findById(profile.getUniversityId()).get());
			/*University university = universityRepo.findById(profile.getUniversityId()).get()
					.withOrganisations(organisationRepo.findWithPartnersByUniversityId(profile.getUniversityId()));
			profile.setUniversity(university);
			university.getOrganisations().forEach(o -> {
				o.setVacancies(enrichVacancies(o, userId));
				o.setUniversity(university);
			});*/
			profile.setSpeciality(specialityRepo.findById(profile.getSpecialityId()).get());
			profile.setResponses(vacancyResponseRepo.findByStudentId(userId));
			yield profile;
		}
		case ADMIN -> {
			AdminProfile profile = adminProfileRepo.findByUserId(userId);
			Organisation organisation = organisationRepo.findById(profile.getOrganisationId()).get();
			organisation.setUniversity(universityRepo.findById(organisation.getId()).orElse(null));
			// organisation.setVacancies(enrichVacancies(organisation, userId));
			profile.setOrganisation(organisation);
			yield profile;
		}
		default -> throw new UnspecifiedRoleException();
		};
	}

	public UserProfile saveProfileFromDto(int userId, Role role, UserProfileDto userProfileDto) {
		return switch (role) {
		case STUDENT -> {
			StudentProfileDto profileDto = (StudentProfileDto) userProfileDto;
			StudentProfile profile = profileMapper.createStudentProfile((StudentProfileDto) userProfileDto);
			profile.setUserId(userId);
			profile.setUniversity(universityRepo.findByName(profileDto.getUniversityName()).get());
			profile.setSpeciality(specialityRepo
					.findByCode(
							profileDto.getSpecialityCodeAndName().split(" ")[0].substring(0, Speciality.CODE_LENGTH))
					.get());
			yield studentProfileRepo.save(profile);
		}
		case ADMIN -> {
			AdminProfileDto profileDto = (AdminProfileDto) userProfileDto;
			AdminProfile profile = profileMapper.createAdminProfile((AdminProfileDto) userProfileDto);
			profile.setUserId(userId);
			profile.setOrganisation(organisationRepo.findByName(profileDto.getOrganisationName()).get());
			yield adminProfileRepo.save(profile);
		}
		default -> throw new UnspecifiedRoleException();
		};
	}

	private List<Vacancy> enrichVacancies(Organisation organisation, int userId) {
		List<Vacancy> result = new ArrayList<>(organisation.getVacancies().size());
		organisation.getVacancies().forEach(v -> result.add(v.withOrganisation(organisation)
				.withHasCurrentStudentResponse(vacancyResponseRepo.existsByStudentIdAndVacancyId(userId, v.getId()))));
		return result;
	}
}