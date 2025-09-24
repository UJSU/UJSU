package ujsu.repositories;

import org.springframework.stereotype.Repository;

import ujsu.entities.StudentProfile;

@Repository
public interface StudentProfileRepository extends UserProfileRepository<StudentProfile> {}