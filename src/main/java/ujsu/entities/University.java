package ujsu.entities;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class University {
	
	@Id
	private int id;
	
	private String name;
	private String shortName;
	private String promo;
	
	private boolean isState;
	
	@With
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Transient
	private Set<Organisation> organisations;
}