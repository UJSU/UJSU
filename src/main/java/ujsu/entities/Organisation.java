package ujsu.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.MappedCollection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {

	@Id
	private Integer id;

	private Boolean isUniversitySubdivision;
	private Integer universityId;
	private String name;

	@With
	@Transient
	private University university;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@MappedCollection(idColumn = "organisation_id", keyColumn = "position")
	private List<Vacancy> vacancies;
	
	public void setUniversity(University university) {
		this.university = university;
		this.universityId = university.getId();
	}
}