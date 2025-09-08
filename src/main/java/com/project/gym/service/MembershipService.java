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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService
{
    private final WhatsAppService whatsAppService;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MembershipMapper membershipMapper;
    private final CustomMetrics customMetrics;

    public MembershipResponseDTO createMembershipByDni(String dni, MembershipRequestDTO requestDTO)
    {
        MembershipEntity savedMembership = membershipRepository.save(
                buildMembership(dni, requestDTO)
        );

        incrementActiveMembershipsMetrics();
        sendMembershipCreatedMessage(savedMembership);

        log.info("Created a membership for the user with dni={}, type={}, payment method={}", dni, requestDTO.type(), requestDTO.paymentMethod());
        return membershipMapper.entityToDTO(savedMembership);
    }

    public List<MembershipResponseDTO> getAllByDate(LocalDate start, LocalDate end)
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

    public List<MembershipResponseDTO> getAllByDni(String dni)
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

    public List<MembershipResponseDTO> getAllActive()
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findAllByStatus(MembershipStatus.ACTIVE)
        );
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
        updateActiveMembershipMetrics(requestDTO.status());

        log.info("Updated membership with id={} to {}", id, requestDTO.status());
        return membershipMapper.entityToDTO(updatedMembership);
    }

    public void deleteMembershipById(int id)
    {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found."));
        membershipRepository.delete(membership);
        decrementActiveMembershipMetrics(membership);
        log.info("Deleted membership with id={}", id);
    }

    public void deleteAllByDni(String dni)
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByUserDni(dni);

        if (memberships.isEmpty())
        {
            throw new MembershipNotFoundException("Memberships not found.");
        }

        membershipRepository.deleteAll(memberships);
        decrementActiveMembershipsMetrics(memberships);
        log.info("Deleted {} memberships with user_dni={}",memberships.size(), dni);
    }

    // Activates when the API starts and every 2 hours.
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000, initialDelay = 0)
    @Transactional
    public void deactivateMemberships()
    {
        List<MembershipEntity> memberships = membershipRepository
                .findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, LocalDate.now());

        memberships.forEach(this::deactivateMembership);

        membershipRepository.saveAll(memberships);
        log.info("SERVER: Deactivated {} memberships today", memberships.size());
    }

    // Activates when the API starts and every 2 hours.
    // If the membership expires tomorrow, and the owner has a phone number, a WhatsApp message is sent.
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000, initialDelay = 0)
    @Transactional
    public void sendExpiryReminder()
    {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<MembershipEntity> memberships = membershipRepository
                .findAllByStatusAndNextPaymentDate(MembershipStatus.ACTIVE, tomorrow);

        memberships.forEach(this::sendExpiryReminderMessage);
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

    private void incrementActiveMembershipsMetrics()
    {
        customMetrics.incrementActiveMemberships();
    }

    private void decrementActiveMembershipsMetrics(List<MembershipEntity> memberships)
    {
        for (MembershipEntity m : memberships)
        {
            if (m.getStatus() == MembershipStatus.ACTIVE)
            {
                customMetrics.decrementActiveMemberships();
            }
        }
    }

    private void decrementActiveMembershipMetrics(MembershipEntity membership)
    {
        if (membership.getStatus() == MembershipStatus.ACTIVE)
        {
            customMetrics.decrementActiveMemberships();
        }
    }

    private void updateActiveMembershipMetrics(MembershipStatus status)
    {
        if (status == MembershipStatus.ACTIVE)
        {
            customMetrics.incrementActiveMemberships();
            return;
        }
        customMetrics.decrementActiveMemberships();
    }

    private void deactivateMembership(MembershipEntity membership)
    {
        membership.setStatus(MembershipStatus.INACTIVE);
        if (membership.getUser() != null && membership.getUser().getPhoneNumber() != null)
        {
            whatsAppService.sendMembershipExpiredMessage(membership, membership.getUser());
        }
        customMetrics.decrementActiveMemberships();
    }

    private void sendExpiryReminderMessage(MembershipEntity membership)
    {
        if (membership.getUser() != null && membership.getUser().getPhoneNumber() != null)
        {
            whatsAppService.sendMembershipExpiryReminderMessage(membership, membership.getUser());
        }
    }

    private void sendMembershipCreatedMessage(MembershipEntity membership)
    {
        if (membership.getUser() != null && membership.getUser().getPhoneNumber() != null)
        {
            whatsAppService.sendMembershipCreatedMessage(membership, membership.getUser());
        }
    }
}
