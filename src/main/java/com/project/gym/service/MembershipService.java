package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.MembershipMessageType;
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

    public MembershipResponseDTO create(MembershipRequestDTO requestDTO)
    {
        MembershipEntity savedMembership = membershipRepository.save(
                buildMembership(requestDTO)
        );

        customMetrics.incrementMemberships();
        customMetrics.incrementActiveMemberships();

        sendWhatsAppMessage(savedMembership.getUser(), savedMembership, MembershipMessageType.CREATED);

        log.info("Created a new membership for the user with dni={}, type={} and payment method={}", requestDTO.dni(), requestDTO.type(), requestDTO.paymentMethod());
        return membershipMapper.entityToDTO(savedMembership);
    }

    public MembershipResponseDTO updateStatusById(int id, MembershipStatusRequestDTO requestDTO)
    {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found."));

        if (membership.getStatus() == requestDTO.status())
        {
            log.info("Membership with id={} was already {}, no update performed", id, requestDTO.status());
            return membershipMapper.entityToDTO(membership);
        }

        updateMembershipStatus(membership, requestDTO.status());
        MembershipEntity updatedMembership = membershipRepository.save(membership);

        log.info("Updated membership with id={} to {}", id, requestDTO.status());
        return membershipMapper.entityToDTO(updatedMembership);
    }

    public void deleteById(int id)
    {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found."));

        // Deletes the membership and updates CustomMetrics counters.
        deleteMembership(membership);

        log.info("Deleted membership with id={}", id);
    }

    public void deleteAllByDni(String dni)
    {
        List<MembershipEntity> memberships = findAllByDniOrThrow(dni);

        // Delete the memberships and update CustomMetrics counters.
        memberships.forEach(this::deleteMembership);

        log.info("Deleted {} memberships of the user with dni={}",memberships.size(), dni);
    }

    public MembershipResponseDTO getById(int id)
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findById(id)
                        .orElseThrow(() -> new MembershipNotFoundException("Membership not found."))
        );
    }

    public MembershipResponseDTO getLastByDni(String dni)
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findLastByDni(dni)
                        .orElseThrow(() -> new MembershipNotFoundException("Membership not found."))
        );
    }

    public List<MembershipResponseDTO> getAllByDni(String dni)
    {
        List<MembershipEntity> memberships = findAllByDniOrThrow(dni);
        return membershipMapper.entityToDTO(memberships);
    }

    public List<MembershipResponseDTO> getAllByDate(LocalDate start, LocalDate end)
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findAllByPaymentDateBetween(start, end)
        );
    }

    public List<MembershipResponseDTO> getAllActive()
    {
        return membershipMapper.entityToDTO(
                membershipRepository.findAllByStatus(MembershipStatus.ACTIVE)
        );
    }

    // Activates when the API starts and every 2 hours.
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000, initialDelay = 0)
    @Transactional
    public void dailyDeactivation()
    {
        List<MembershipEntity> memberships = membershipRepository
                .findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, LocalDate.now());

        memberships.forEach(this::deactivateMembership);
        membershipRepository.saveAll(memberships);

        log.info("DAILY MEMBERSHIPS DEACTIVATION: {} were deactivated today", memberships.size());
    }

    // Activates when the API starts and every 2 hours.
    // If the membership expires tomorrow, and the owner has a phone number, a WhatsApp message is sent.
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000, initialDelay = 0)
    @Transactional
    public void sendExpiryReminder()
    {
        List<MembershipEntity> membershipsAboutToExpire = membershipRepository
                .findAllByStatusAndNextPaymentDate(MembershipStatus.ACTIVE, LocalDate.now().plusDays(1));

        membershipsAboutToExpire.forEach(m -> sendWhatsAppMessage(m.getUser(), m, MembershipMessageType.EXPIRING));
    }

    private MembershipEntity buildMembership(MembershipRequestDTO requestDTO)
    {
        UserEntity user = userRepository.findByDni(requestDTO.dni()).orElse(null);
        if (user != null && user.getRole() != Role.USER)
        {
            throw new InvalidMembershipAssignmentException("Only users with role USER can have a membership.");
        }

        return MembershipEntity
                .builder()
                .user(user)
                .dni(requestDTO.dni())
                .status(MembershipStatus.ACTIVE)
                .type(requestDTO.type())
                .paymentMethod(requestDTO.paymentMethod())
                .paymentDate(LocalDate.now())
                .nextPaymentDate(LocalDate.now().plusMonths(requestDTO.type().getDuration()))
                .build();
    }

    private void deleteMembership(MembershipEntity membership)
    {
        membershipRepository.delete(membership);
        sendWhatsAppMessage(membership.getUser(), membership, MembershipMessageType.DELETED);

        customMetrics.decrementMemberships();
        if (membership.getStatus() == MembershipStatus.ACTIVE)
        {
            customMetrics.decrementActiveMemberships();
        }
    }

    private void updateMembershipStatus(MembershipEntity membership, MembershipStatus status)
    {
        membership.setStatus(status);
        if (status == MembershipStatus.ACTIVE)
        {
            customMetrics.incrementActiveMemberships();
        }
        else
        {
            customMetrics.decrementActiveMemberships();
        }
    }

    private void deactivateMembership(MembershipEntity membership)
    {
        // Set the membership as INACTIVE and decrement CustomMetrics totalActiveMemberships.
        updateMembershipStatus(membership, MembershipStatus.INACTIVE);

        sendWhatsAppMessage(membership.getUser(), membership, MembershipMessageType.EXPIRED);
    }

    private void sendWhatsAppMessage(UserEntity user, MembershipEntity membership, MembershipMessageType messageType)
    {
        if (user != null && user.getPhoneNumber() != null)
        {
            log.info("WhatsApp message of type {} was sent to user with dni={} and phoneNumber={}",
                    messageType,
                    user.getDni(),
                    user.getPhoneNumber()
            );
            whatsAppService.sendMessage(user.getPhoneNumber(), messageType.format(user, membership));
        }
    }

    private List<MembershipEntity> findAllByDniOrThrow(String dni)
    {
        List<MembershipEntity> memberships = membershipRepository.findAllByDni(dni);
        if (memberships.isEmpty())
        {
            throw new MembershipNotFoundException("Memberships not found.");
        }
        return memberships;
    }
}
