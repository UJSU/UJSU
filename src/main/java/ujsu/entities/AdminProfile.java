package ujsu.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Table("admin_profile")
public class AdminProfile implements UserProfile {
	
	@Id
	private Integer id;
	
	private Integer userId;
	private Integer organisationId;
}