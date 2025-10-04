package ujsu.entities;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class University {
	
	@Id
	private int id;
	
	private String name;
	private String shortName;
	private String promo;
	
	private boolean isState;
	
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@MappedCollection(idColumn="university_id")
	private Set<Organisation> organisations;
}