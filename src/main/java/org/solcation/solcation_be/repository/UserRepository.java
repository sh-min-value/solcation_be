package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
