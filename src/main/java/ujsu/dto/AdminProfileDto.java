package ujsu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NotBlank(message = "Заполните это поле.")
public class AdminProfileDto implements UserProfileDto {

	private String organisationName;
}