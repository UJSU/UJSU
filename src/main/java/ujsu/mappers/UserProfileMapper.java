package ujsu.mappers;

import org.mapstruct.Mapper;

import ujsu.dto.AdminProfileDto;
import ujsu.dto.StudentProfileDto;
import ujsu.entities.AdminProfile;
import ujsu.entities.StudentProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
	StudentProfile createStudentProfile(StudentProfileDto dto);
	AdminProfile createAdminProfile(AdminProfileDto dto);
}
