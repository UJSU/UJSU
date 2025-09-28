package ujsu.dto;

import lombok.Getter;
import lombok.Setter;
import ujsu.enums.StudyType;

@Getter
@Setter
public class StudentProfileDto implements UserProfileDto {

	private String universityName;
	private String specialityCode;
	private StudyType studyType;
	private Byte courseNum;
}