package ujsu.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ujsu.entities.University;
import ujsu.repositories.UniversityRepository;

import java.util.List;

@Service
public class UniversityService {
    
    private final UniversityRepository universityRepository;
    
    public UniversityService(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }
    
    public List<University> findByNameContaining(String name) {
        return universityRepository.findByNameContainingIgnoreCase(name, 
            PageRequest.of(0, 10, Sort.by("name")));
    }
}