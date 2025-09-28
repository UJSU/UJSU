package ujsu.entities;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class University {
	
	@Id
	private int id;
	
	private String name;
	
	private boolean isState;
}