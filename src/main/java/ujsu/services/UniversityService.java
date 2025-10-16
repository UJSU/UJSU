package ujsu.services;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ujsu.entities.Speciality;
import ujsu.entities.University;
import ujsu.repositories.UniversityRepository;

@Service
@RequiredArgsConstructor
public class UniversityService {

	private final UniversityRepository universityRepo;

	private final int PAGE_LENGTH = 50;

	public List<University> findByNameMatch(String input, int pageNum) {
		return universityRepo.findByNameMatch(input, PAGE_LENGTH * (pageNum - 1), PAGE_LENGTH * pageNum);
	}

	public List<Speciality> findSpecialitiesByNameOrCodeMatch(String input, String universityName, int pageNum) {
		return universityRepo.findSpecialitiesByNameOrCodeMatch(input, universityName, PAGE_LENGTH * (pageNum - 1),
				PAGE_LENGTH * pageNum);
	}
}