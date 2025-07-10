package com.project.gym.repository;

import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer>
{
    List<UserEntity> findAllByRole(Role role);

    Optional<UserEntity> findByDni(String dni);

    boolean existsByDni(String dni);
}
