package ujsu.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {
	
	@Id
	private Integer id;
	
	private String position;
	
	private Integer organisationId;
	private Integer minSalary;
	private Integer maxSalary;
	
	private String schedule;
	private String description;
	
	@EqualsAndHashCode.Exclude
	private int responseCount;
	
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Transient
	private Organisation organisation;
	
	@With
	@Transient
	@EqualsAndHashCode.Exclude
	private boolean hasCurrentStudentResponse;
	
	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
		this.organisationId = organisation.getId();
	}
}