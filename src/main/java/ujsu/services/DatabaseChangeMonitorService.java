package ujsu.services;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseChangeMonitorService {

	private final JdbcTemplate jdbcTemplate;
	private final CacheManager cacheManager;

	@Scheduled(fixedDelay = 5000)
	public void pollDbChanges() {
		String sql = """
				SELECT id, table_name, operation, record_id
				FROM db_changes_log
				WHERE processed = FALSE
				ORDER BY id
				LIMIT 100
				""";
		for (Map<String, Object> row : jdbcTemplate.queryForList(sql)) {
			String tableName = (String) row.get("table_name");
			String operation = (String) row.get("operation");
			Integer recordId = (Integer) row.get("record_id");

			handleDbChange(tableName, operation, recordId);

			jdbcTemplate.update("UPDATE db_changes_log SET processed = TRUE WHERE id = ?", row.get("id"));
		}
	}

	private void handleDbChange(String tableName, String operation, Integer recordId) {
	    switch (tableName) {
	        case "vacancy" -> {
	            Integer orgId = jdbcTemplate.queryForObject(
	                "SELECT organisation_id FROM vacancy WHERE id = ?",
	                Integer.class, 
	                recordId
	            );
	            if (orgId != null) {
	                evictOrganisationVacancies(orgId);
	                evictUniversityVacanciesForOrganisation(orgId);
	                evictPartnerUniversitiesForOrganisation(orgId);
	            }
	        }
	        case "organisation" -> {
	            evictUniversityVacanciesForOrganisation(recordId);
	            evictPartnerUniversitiesForOrganisation(recordId);
	            Integer universityId = jdbcTemplate.queryForObject(
	                    "SELECT university_id FROM organisation WHERE id = ?",
	                    Integer.class,
	                    recordId
	                );
	                if (universityId != null)
	                    Objects.requireNonNull(cacheManager.getCache("universities")).evict(universityId);
	        }
	    }
	}

	private void evictOrganisationVacancies(Integer organisationId) {
		Objects.requireNonNull(cacheManager.getCache("organisation-vacancies")).evict(organisationId);
	}

	private void evictUniversityVacanciesForOrganisation(Integer organisationId) {
		Integer universityId = jdbcTemplate.queryForObject("SELECT university_id FROM organisation WHERE id = ?",
				Integer.class, organisationId);
		if (universityId != null) {
			Objects.requireNonNull(cacheManager.getCache("university-vacancies")).evict(universityId);
		}
	}

	private void evictPartnerUniversitiesForOrganisation(Integer organisationId) {
		List<Map<String, Object>> partnerRows = jdbcTemplate
				.queryForList("SELECT university_id FROM university_partner WHERE organisation_id = ?", organisationId);
		for (Map<String, Object> row : partnerRows) {
			Integer partnerUniId = (Integer) row.get("university_id");
			if (partnerUniId != null) {
				Objects.requireNonNull(cacheManager.getCache("university-vacancies")).evict(partnerUniId);
			}
		}
	}
}