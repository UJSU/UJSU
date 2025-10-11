package ujsu.repositories;

import org.springframework.data.repository.CrudRepository;

import ujsu.entities.Organisation;

public interface OrganisationRepository extends CrudRepository<Organisation, Integer> {}