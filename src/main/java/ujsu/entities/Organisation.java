package ujsu.entities;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.MappedCollection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class Organisation {

	@Id
	private Integer id;
	
	private Boolean isUniversitySubdivision;
	private Integer universityId;
	private String name;
	
	@Transient
	private University university;
	
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@MappedCollection(idColumn="organisation_id")
	private Set<Vacancy> vacancies;
}