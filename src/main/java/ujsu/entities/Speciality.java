package ujsu.entities;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Speciality {

	@Id
	private Integer id;
	
	private String code;
	private String name;	
}