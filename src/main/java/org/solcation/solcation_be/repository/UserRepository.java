package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String id);
    Optional<User> findByTel(String tel);
}
