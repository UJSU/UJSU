package ujsu.entities;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Speciality {

	@Id
	private Integer id;
	
	private String code;
	private String name;	
}