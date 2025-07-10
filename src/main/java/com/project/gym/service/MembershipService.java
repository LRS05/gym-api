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
        MembershipEntity membership = buildMembership(dni, requestDTO);
        MembershipEntity savedMembership = membershipRepository.save(membership);

        customMetrics.incrementMemberships();
        customMetrics.incrementActiveMemberships();
        log.info("Created membership for user with DNI: {}, Type: {}, Payment Method: {}", dni, requestDTO.type(), requestDTO.paymentMethod());
        return membershipMapper.toDTO(savedMembership);
    }

    public List<MembershipResponseDTO> getMembershipsByDate(LocalDate start, LocalDate end)
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByPaymentDateBetween(start, end);
        return membershipMapper.toDTO(memberships);
    }

    public MembershipResponseDTO getMembershipById(int id)
    {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found."));
        return membershipMapper.toDTO(membership);
    }

    public List<MembershipResponseDTO> getMembershipsByDni(String dni)
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByDni(dni);
        return membershipMapper.toDTO(memberships);
    }

    public MembershipResponseDTO getLastMembershipByDni(String dni)
    {
        MembershipEntity membership = membershipRepository.findFirstByDniOrderByPaymentDateDesc(dni)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found."));
        return membershipMapper.toDTO(membership);
    }

    public List<MembershipResponseDTO> getActiveMemberships()
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByStatus(MembershipStatus.ACTIVE);
        return membershipMapper.toDTO(memberships);
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void deactivateMemberships()
    {
        List<MembershipEntity> memberships = membershipRepository
                .findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, LocalDate.now());

        if (memberships.isEmpty())
        {
            log.info("No memberships to deactivate today.");
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

        if (membership.getStatus().equals(requestDTO.status()))
        {
            log.info("Membership with ID {} was already in {} status, no update performed", id, requestDTO.status());
            return membershipMapper.toDTO(membership);
        }

        membership.setStatus(requestDTO.status());

        MembershipEntity updatedMembership = membershipRepository.save(membership);
        log.info("Updated membership with ID {} to status {}", id, requestDTO.status());
        return membershipMapper.toDTO(updatedMembership);
    }

    public void deleteMembershipById(int id)
    {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found."));

        membershipRepository.delete(membership);

        if (membership.getStatus().equals(MembershipStatus.ACTIVE))
        {
            customMetrics.decrementActiveMemberships();
        }
        customMetrics.decrementMemberships();
        log.info("Deleted membership with ID {}", id);
    }

    public void deleteMembershipsByDni(String dni)
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByDni(dni);

        if (!memberships.isEmpty())
        {
            membershipRepository.deleteAll(memberships);

            for (MembershipEntity m : memberships)
            {
                if (m.getStatus().equals(MembershipStatus.ACTIVE))
                {
                    customMetrics.decrementActiveMemberships();
                }
                customMetrics.decrementMemberships();
            }
        }
        log.info("Deleted {} memberships for DNI {}",memberships.size(), dni);
    }

    private MembershipEntity buildMembership(String dni, MembershipRequestDTO requestDTO)
    {
        UserEntity user = userRepository.findByDni(dni)
                .orElse(null);

        if (user != null && !user.getRole().equals(Role.USER))
        {
            throw new InvalidMembershipAssignmentException("Only users with role USER can have a membership.");
        }

        return MembershipEntity
                .builder()
                .user(user)
                .dni(dni)
                .status(MembershipStatus.ACTIVE)
                .type(requestDTO.type())
                .paymentMethod(requestDTO.paymentMethod())
                .paymentDate(LocalDate.now())
                .nextPaymentDate(LocalDate.now().plusMonths(requestDTO.type().getDuration()))
                .build();
    }
}
