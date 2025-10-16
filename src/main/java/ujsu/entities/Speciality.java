package ujsu.entities;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Speciality {

	public static final int CODE_LENGTH = 8;
	
	@Id
	private Integer id;
	
	private String code;
	private String name;
}