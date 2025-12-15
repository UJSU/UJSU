package ujsu.services;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ujsu.entities.University;
import ujsu.repositories.OrganisationRepository;

@Service
@RequiredArgsConstructor
public class OrganisationService {

	private final OrganisationRepository organisationRepo;

	private final int PAGE_LENGTH = 50;

	public List<University> findByNameMatch(String input, int pageNum) {
		return organisationRepo.findByNameMatch(input, PAGE_LENGTH * (pageNum - 1), PAGE_LENGTH * pageNum);
	}
}