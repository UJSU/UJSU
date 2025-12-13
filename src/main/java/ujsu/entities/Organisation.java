package ujsu.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.With;

@Data
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
	private List<Vacancy> vacancies;
}