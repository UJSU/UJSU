package ujsu.entities;

import ujsu.enums.StudyType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class StudentProfile implements UserProfile {

	private int id;
	private int userId;
	private int universityId;
	private int specialityId;

	private byte courseNum;
	private StudyType studyType;
}