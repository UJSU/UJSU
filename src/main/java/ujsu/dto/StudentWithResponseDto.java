package ujsu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ujsu.entities.User;
import ujsu.entities.VacancyResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentWithResponseDto {
	
	private User student;
	private VacancyResponse response;
}