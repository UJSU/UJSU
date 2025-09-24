package ujsu.repositories;

import org.springframework.stereotype.Repository;

import ujsu.entities.AdminProfile;

@Repository
public interface AdminProfileRepository extends UserProfileRepository<AdminProfile> {}