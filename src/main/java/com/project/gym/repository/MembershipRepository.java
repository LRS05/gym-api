package com.project.gym.repository;

import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Optional<MembershipEntity> findFirstByUserDniOrderByPaymentDateDesc(String dni);

    List<MembershipEntity> findAllByStatusAndNextPaymentDateBefore(MembershipStatus status, LocalDate date);

    List<MembershipEntity> findAllByStatusAndNextPaymentDate(MembershipStatus status, LocalDate date);
}
