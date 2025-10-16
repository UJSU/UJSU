package ujsu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ujsu.enums.StudyType;

@Getter
@Setter
@NotBlank(message = "Заполните это поле.")
public class StudentProfileDto implements UserProfileDto {

	private String universityName;
	private String specialityCodeAndName;
	private StudyType studyType;
	private Byte courseNum;
}