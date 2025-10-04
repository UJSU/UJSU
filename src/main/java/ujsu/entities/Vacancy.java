package ujsu.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class Vacancy {
	
	@Id
	private Integer id;
	
	private String position;
	
	private Integer organisation_id;
	private Integer minSalary;
	private Integer maxSalary;
	
	private String shedule;
	private String description;
	
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Transient
	private Organisation organisation;
}