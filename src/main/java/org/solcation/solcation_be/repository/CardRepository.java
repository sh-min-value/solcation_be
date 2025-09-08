package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.Card;
import org.solcation.solcation_be.entity.GroupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
}
