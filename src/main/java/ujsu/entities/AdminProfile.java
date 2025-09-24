package ujsu.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AdminProfile implements UserProfile {
	
	private int id;
	private int userId;
	private int organisationId;
}