package com.project.gym.repository;

import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity, Integer>
{
    List<MembershipEntity> findAllByPaymentDateBetween(LocalDate start, LocalDate end);

    List<MembershipEntity> findAllByStatus(MembershipStatus status);

    List<MembershipEntity> findAllByUserDni(String dni);

    @Query(
            value = "SELECT * " +
                    "FROM memberships " +
                    "WHERE user_dni = :dni " +
                    "ORDER BY payment_date DESC " +
                    "LIMIT 1",
            nativeQuery = true
    )
    Optional<MembershipEntity> findLastByUserDni(@Param("dni") String dni);

    List<MembershipEntity> findAllByStatusAndNextPaymentDateBefore(MembershipStatus status, LocalDate date);

    List<MembershipEntity> findAllByStatusAndNextPaymentDate(MembershipStatus status, LocalDate date);
}
