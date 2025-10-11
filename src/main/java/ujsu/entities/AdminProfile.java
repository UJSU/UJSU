package ujsu.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Data
@Table("admin_profile")
public class AdminProfile implements UserProfile {
	
	@Id
	private Integer id;
	
	private Integer userId;
	private Integer organisationId;
	
	@Transient
	private Organisation organisation;
}