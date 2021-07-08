package es.um.asio.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import es.um.asio.service.domain.Validator;

@Repository
public interface ValidatorRepository extends JpaRepository<Validator, String>, JpaSpecificationExecutor<Validator>{ 

	Validator findFirstByEntity(String entity);
}
