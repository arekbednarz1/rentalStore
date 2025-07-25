package com.arekbednarz.model.repository;

import com.arekbednarz.model.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

	@Query(value = "select t from Token t inner join User u on t.user.id = u.id where u.id = :id and (t.expired = false or t.revoked = false)")
	List<Token> findAllValidTokenByUser(@Param("id") final Long id);

	Optional<Token> findByToken(String token);

}
