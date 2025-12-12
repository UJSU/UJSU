package ujsu.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import lombok.Data;
import ujsu.enums.StudyType;

@Data
public class StudentProfile implements UserProfile {

	@Id
	private Integer id;
	private Integer userId;
	private Integer universityId;
	private Integer specialityId;

	private Byte courseNum;
	private Integer studyType;
	
	@Transient
	private List<VacancyResponse> responses;
    
	@Transient
	private University university;
	
	@Transient
	private Speciality speciality;
	
    public StudyType getStudyType() {
        return StudyType.fromCode(studyType);
    }

    public void setStudyType(StudyType studyType) {
        this.studyType = studyType.getCode();
    }
    
    public void setUniversity(University university) {
    	this.university = university;
    	this.universityId = university != null ? university.getId() : null;
    }
    
    public void setSpeciality(Speciality speciality) {
    	this.speciality = speciality;
    	this.specialityId = speciality != null ? speciality.getId() : null;
    }
}