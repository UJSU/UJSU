package ujsu.entities;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ujsu.enums.StudyType;

@Getter
@Setter
@RequiredArgsConstructor
public class StudentProfile implements UserProfile {

	@Id
	private Integer id;
	private Integer userId;
	private Integer universityId;
	private Integer specialityId;

	private Byte courseNum;
	private Integer studyType;
    
    public StudyType getStudyType() {
        return StudyType.fromCode(studyType);
    }

    public void setStudyType(StudyType studyType) {
        this.studyType = studyType.getCode();
    }
}