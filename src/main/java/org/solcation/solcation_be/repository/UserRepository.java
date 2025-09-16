package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.auth.dto.UserAuthDTO;
import org.solcation.solcation_be.domain.group.dto.UserDTO;
import org.solcation.solcation_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
    select new org.solcation.solcation_be.domain.auth.dto.UserAuthDTO (
        u.userPk,
        u.userId,
        u.userPw,
        u.tel,
        u.userName,
        u.role,
        u.email
        )
    FROM User u
    WHERE u.userId = :userId
    """)
    Optional<UserAuthDTO> findByUserIdToDTO(String userId);

    Optional<User> findByUserId(String id);

    @Query("""
            select new org.solcation.solcation_be.domain.group.dto.UserDTO(
                u.userPk,
                u.userId,
                u.tel,
                u.userName,
                u.dateOfBirth,
                u.gender,
                u.email,
                case when exists(
                    select gm from GroupMember gm
                    where gm.user = u
                        and gm.group.groupPk = :groupPk
                        and gm.isAccepted = true
                ) then true else false end,
                                case when exists(
                    select gm from GroupMember gm
                    where gm.user = u
                        and gm.group.groupPk = :groupPk
                        and gm.isAccepted is null
                ) then true else false end
            )
            FROM User u
            WHERE u.tel = :tel
            """)
    Optional<UserDTO> findByTelWithGroupCheck(@Param("tel") String tel, @Param("groupPk") Long groupPk);
}
