package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.Card;
import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    //해당 그룹에서 특정 유저가 개설한 카드 존재 여부 조회
    boolean existsBySaPk_GroupAndGmPk_UserAndCancellationFalse(@Param("group") Group group, @Param("user") User user);

    //해당 그룹에서 특정 유저가 개설한 카드 조회
    Optional<Card> findBySaPk_GroupAndGmPk_UserAndCancellationFalse(@Param("group") Group group, @Param("user") User user);
    Optional<Card> findBySacPkAndSaPk_GroupAndGmPk_UserAndCancellationFalse(@Param("sacPk") Long sacPk, @Param("group") Group group, @Param("user") User user);

    //카드 번호 모두 조회
    @Query("SELECT c.sacNum FROM Card c")
    List<String> findAllSacNums();

    //해당 그룹에서 해당 유저가 개설한 카드가 있는지 확인
    boolean existsBySacPkAndSaPk_Group_GroupPkAndGmPk_User_UserPkAndCancellationFalse(@Param("sacPk") Long sacPk, @Param("groupPk") Long groupPk, @Param("userPk") Long userPk);

}
