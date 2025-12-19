package ujsu.entities;

import org.springframework.data.relational.core.mapping.Table;

@Table
public interface UserProfile {
	
	void setUserId(Integer id);
}