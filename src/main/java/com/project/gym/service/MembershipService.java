package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.Role;
import com.project.gym.exception.InvalidMembershipAssignmentException;
import com.project.gym.exception.MembershipNotFoundException;
import com.project.gym.mapper.MembershipMapper;
import com.project.gym.repository.MembershipRepository;
import com.project.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService
{
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MembershipMapper membershipMapper;
    private final CustomMetrics customMetrics;

    public MembershipResponseDTO createMembership(String dni, MembershipRequestDTO requestDTO)
    {
        MembershipEntity savedMembership = membershipRepository.save(
                buildMembership(dni, requestDTO)
        );

        customMetrics.incrementMemberships();
        customMetrics.incrementActiveMemberships();

        log.info("Created a membership for the user with dni={}, type={}, payment method={}", dni, requestDTO.type(), requestDTO.paymentMethod());
        return membershipMapper.entityToDTO(savedMembership);
    }

    public List<MembershipResponseDTO> getMembershipsByDate(LocalDate start, LocalDate end)
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findAllByPaymentDateBetween(start, end)
        );
    }

    public MembershipResponseDTO getMembershipById(int id)
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findById(id)
                        .orElseThrow(() -> new MembershipNotFoundException("Membership not found."))
        );
    }

    public List<MembershipResponseDTO> getMembershipsByDni(String dni)
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findAllByUserDni(dni)
        );
    }

    public MembershipResponseDTO getLastMembershipByDni(String dni)
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findFirstByUserDniOrderByPaymentDateDesc(dni)
                        .orElseThrow(() -> new MembershipNotFoundException("Membership not found."))
        );
    }

    public List<MembershipResponseDTO> getActiveMemberships()
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findAllByStatus(MembershipStatus.ACTIVE)
        );
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void deactivateMemberships()
    {
        List<MembershipEntity> memberships = membershipRepository
                .findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, LocalDate.now());

        if (memberships.isEmpty())
        {
            log.info("No memberships to deactivate today");
            return;
        }

        memberships.forEach(m ->
        {
            m.setStatus(MembershipStatus.INACTIVE);
            customMetrics.decrementActiveMemberships();
        });
        membershipRepository.saveAll(memberships);
        log.info("Deactivated {} memberships", memberships.size());
    }

    public MembershipResponseDTO updateMembershipStatusById(int id, MembershipStatusRequestDTO requestDTO)
    {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found."));

        if (membership.getStatus() == requestDTO.status())
        {
            log.info("Membership with id={} was already {}, no update performed", id, requestDTO.status());
            return membershipMapper.entityToDTO(membership);
        }

        membership.setStatus(requestDTO.status());
        MembershipEntity updatedMembership = membershipRepository.save(membership);

        log.info("Updated membership with id={} to {}", id, requestDTO.status());
        return membershipMapper.entityToDTO(updatedMembership);
    }

    public void deleteMembershipById(int id)
    {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found."));

        membershipRepository.delete(membership);

        // Esto lo podemos meter en un metodo privado.
        if (membership.getStatus() == MembershipStatus.ACTIVE)
        {
            customMetrics.decrementActiveMemberships();
        }
        customMetrics.decrementMemberships();
        log.info("Deleted membership with id={}", id);
    }

    public void deleteMembershipsByDni(String dni)
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByUserDni(dni);

        if (!memberships.isEmpty())
        {
            membershipRepository.deleteAll(memberships);

            // Meter este bucle en un metodo privado (o el if).
            for (MembershipEntity m : memberships)
            {
                if (m.getStatus().equals(MembershipStatus.ACTIVE))
                {
                    customMetrics.decrementActiveMemberships();
                }
                customMetrics.decrementMemberships();
            }
        }
        log.info("Deleted {} memberships with user_dni={}",memberships.size(), dni);
    }

    private MembershipEntity buildMembership(String dni, MembershipRequestDTO requestDTO)
    {
        UserEntity user = userRepository.findByDni(dni).orElse(null);
        if (user != null && user.getRole() != Role.USER)
        {
            throw new InvalidMembershipAssignmentException("Only users with role USER can have a membership.");
        }

        return MembershipEntity
                .builder()
                .user(user)
                .userDni(dni)
                .status(MembershipStatus.ACTIVE)
                .type(requestDTO.type())
                .paymentMethod(requestDTO.paymentMethod())
                .paymentDate(LocalDate.now())
                .nextPaymentDate(LocalDate.now().plusMonths(requestDTO.type().getDuration()))
                .build();
    }
}
