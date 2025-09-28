package ujsu.entities;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Organisation {

	@Id
	private Integer id;
	private Integer name;
}