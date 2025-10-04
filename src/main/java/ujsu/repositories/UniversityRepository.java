package ujsu.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import ujsu.entities.University;

import java.util.List;
import java.util.Optional;

public interface UniversityRepository extends CrudRepository<University, Integer> {

    Optional<University> findByName(String name);
    
    List<University> findByNameContainingIgnoreCase(String name, Pageable pageable);
}