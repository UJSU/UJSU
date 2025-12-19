package ujsu.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table
@Data
public class Speciality {

	public static final int CODE_LENGTH = 8;
	
	@Id
	private Integer id;
	
	private String code;
	private String name;
}