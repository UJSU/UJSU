package ujsu.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@Table
public class VacancyResponse {

	@Id
	private int id;
	
	private int studentId;
	private int vacancyId;
	
	private Boolean employerVerdict;
	private String employerComment;
	
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Transient
	private Vacancy vacancy;
}