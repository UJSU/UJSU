package ujsu.entities;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import ujsu.enums.Role;
import ujsu.enums.Sex;

@Table
@Data
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;
	
	@Id
	private Integer id;

	private String email;
	private String hashedPassword;
	private String name;
	private String surname;
	private String lastName;

	private LocalDate birthDate;
	private LocalDate signUpDate;

	private Integer sex;
	private Integer role;
	
	@Transient
	private UserProfile profile;
	
	public Sex getSex() {
        return Sex.fromCode(sex);
    }

    public void setSex(Sex sex) {
        this.sex = sex.getCode();
    }
    
    public Role getRole() {
        return Role.fromCode(role);
    }

    public void setRole(Role role) {
        this.role = role.getCode();
    }
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + this.getRole().name()));
	}

	@Override
	public String getPassword() {
		return this.getHashedPassword();
	}

	@Override
	public String getUsername() {
		return this.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}