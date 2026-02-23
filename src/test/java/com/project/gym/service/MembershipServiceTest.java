package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import com.project.gym.entity.enums.Role;
import com.project.gym.exception.InvalidMembershipAssignmentException;
import com.project.gym.exception.MembershipNotFoundException;
import com.project.gym.mapper.MembershipMapper;
import com.project.gym.repository.MembershipRepository;
import com.project.gym.repository.UserRepository;
import com.project.gym.factory.MembershipFactory;
import com.project.gym.factory.UserFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipServiceTest
{
    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipMapper membershipMapper;

    @Mock
    private CustomMetrics customMetrics;

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void createMembership_whenRequestIsValidAndUserHasPhoneNumber_thenReturnMembershipAndSendWhatsappMessage()
    {
        // Given
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );
        UserEntity expectedUser = UserFactory.userUser();
        MembershipEntity expectedMembership = MembershipFactory.userRegisteredMembership();
        MembershipResponseDTO expectedMembershipDTO = MembershipFactory.userRegisteredMembershipDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(membershipRepository.save(any(MembershipEntity.class))).thenReturn(expectedMembership);
        when(membershipMapper.entityToDTO(expectedMembership)).thenReturn(expectedMembershipDTO);

        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        MembershipResponseDTO result = membershipService.createByDni(dni, requestDTO);

        // Then
        verify(membershipRepository).save(captor.capture());

        assertEquals(expectedUser, captor.getValue().getUser());
        assertEquals(expectedMembershipDTO, result);
        assertNotNull(expectedMembership.getUser().getPhoneNumber());

        verify(userRepository).findByDni(dni);
        verify(whatsAppService).sendMessage(anyString(), anyString());
        verify(membershipMapper).entityToDTO(expectedMembership);
    }

    @Test
    void createMembership_whenUserDoesNotHavePhoneNumber_thenReturnMembership()
    {
        // Given
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );
        UserEntity expectedUser = UserFactory.userUser();
        expectedUser.setPhoneNumber(null);

        MembershipEntity expectedMembership = MembershipFactory.userRegisteredMembership();
        MembershipResponseDTO expectedMembershipDTO = MembershipFactory.userRegisteredMembershipDTO();
        expectedMembership.getUser().setPhoneNumber(null);

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(membershipRepository.save(any(MembershipEntity.class))).thenReturn(expectedMembership);
        when(membershipMapper.entityToDTO(expectedMembership)).thenReturn(expectedMembershipDTO);

        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        MembershipResponseDTO result = membershipService.createByDni(dni, requestDTO);

        // Then
        verify(membershipRepository).save(captor.capture());

        assertEquals(result.dni(), captor.getValue().getDni());
        assertEquals(expectedMembershipDTO, result);
        assertNull(expectedMembership.getUser().getPhoneNumber());

        verify(userRepository).findByDni(dni);
        verify(membershipMapper).entityToDTO(expectedMembership);
        verifyNoInteractions(whatsAppService);
    }

    @Test
    void createMembership_whenUserIsAdminOrStaff_thenThrowException()
    {
        // Given
        String dni = "12345678";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );
        UserEntity expectedUser = UserFactory.userStaff();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        assertThrows(InvalidMembershipAssignmentException.class, () -> membershipService.createByDni(dni, requestDTO));

        // Then
        assertNotEquals(Role.USER, expectedUser.getRole());

        verify(userRepository).findByDni(dni);
        verifyNoInteractions(membershipRepository);
        verifyNoInteractions(whatsAppService);
    }

    @Test
    void createMembership_whenUserDoesNotHaveAnAccount_thenReturnMembershipWithoutUserId()
    {
        // Given
        String dni = "99999999";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );
        MembershipEntity expectedMembersip = MembershipFactory.userNotRegisteredMembership();
        MembershipResponseDTO expectedMembershipDTO = MembershipFactory.userNotRegisteredMembershipDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());
        when(membershipRepository.save(any(MembershipEntity.class))).thenReturn(expectedMembersip);
        when(membershipMapper.entityToDTO(expectedMembersip)).thenReturn(expectedMembershipDTO);

        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        MembershipResponseDTO result = membershipService.createByDni(dni, requestDTO);

        // Then
        verify(membershipRepository).save(captor.capture());

        assertNull(captor.getValue().getUser());
        assertEquals(expectedMembershipDTO, result);

        verify(userRepository).findByDni(dni);
        verify(membershipMapper).entityToDTO(expectedMembersip);
        verifyNoInteractions(whatsAppService);
    }

    @Test
    void getMembershipsByDate_whenMembershipsExists_thenReturnMemberships()
    {
        // Given
        List<MembershipEntity> expectedMemberships = MembershipFactory.membershipListOf2025();
        List<MembershipResponseDTO> expectedDTOs = MembershipFactory.membershipListOf2025DTO();
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        // When
        when(membershipRepository.findAllByPaymentDateBetween(start, end)).thenReturn(expectedMemberships);
        when(membershipMapper.entityToDTO(expectedMemberships)).thenReturn(expectedDTOs);

        List<MembershipResponseDTO> result = membershipService.getAllByDate(start, end);

        // Then
        assertEquals(expectedDTOs, result);
        assertTrue(result.stream().allMatch(m ->
                !m.paymentDate().isBefore(start) && !m.paymentDate().isAfter(end)
        ));

        verify(membershipRepository).findAllByPaymentDateBetween(start, end);
        verify(membershipMapper).entityToDTO(expectedMemberships);
    }

    @Test
    void getMembershipsByDate_whenMembershipsDoNotExist_thenReturnEmptyList()
    {
        // Given
        LocalDate start = LocalDate.of(2100, 1, 1);
        LocalDate end = LocalDate.of(2100, 12, 31);

        // When
        when(membershipRepository.findAllByPaymentDateBetween(start, end)).thenReturn(new ArrayList<>());
        when(membershipMapper.entityToDTO(anyList())).thenReturn(new ArrayList<>());

        List<MembershipResponseDTO> result = membershipService.getAllByDate(start, end);

        // Then
        assertTrue(result.isEmpty());

        verify(membershipRepository).findAllByPaymentDateBetween(start, end);
        verify(membershipMapper).entityToDTO(anyList());
    }

    @Test
    void getMembershipsById_WhenMembershipsExists_ThenReturnMembership()
    {
        // Given
        int id = 1;
        MembershipEntity expectedMembership = MembershipFactory.userRegisteredMembership();
        MembershipResponseDTO expectedDTO = MembershipFactory.userRegisteredMembershipDTO();

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.of(expectedMembership));
        when(membershipMapper.entityToDTO(expectedMembership)).thenReturn(expectedDTO);

        MembershipResponseDTO result = membershipService.getById(id);

        // Then
        assertEquals(expectedDTO, result);

        verify(membershipRepository).findById(id);
        verify(membershipMapper).entityToDTO(expectedMembership);
    }

    @Test
    void getById_WhenMembershipsDoesNotExist_ThenThrowException()
    {
        // Given
        int id = -1;

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.empty());

        // Then
        assertThrows(MembershipNotFoundException.class, () -> membershipService.getById(id));

        verify(membershipRepository).findById(id);
    }

    @Test
    void getMembershipsByDni_WhenMembershipsExistForUser_ThenReturnMemberships()
    {
        // Given
        String dni = "87654321";
        List<MembershipEntity> expectedMemberships = MembershipFactory.defaultUserMemberships();
        List<MembershipResponseDTO> expectedDTOs = MembershipFactory.defaultUserMembershipsDTO();

        // When
        when(membershipRepository.findAllByDni(dni)).thenReturn(expectedMemberships);
        when(membershipMapper.entityToDTO(expectedMemberships)).thenReturn(expectedDTOs);

        List<MembershipResponseDTO> result = membershipService.getAllByDni(dni);

        // Then
        assertEquals(expectedDTOs, result);
        assertTrue(result.stream().allMatch(m ->
                m.dni().equals(dni)
        ));

        verify(membershipRepository).findAllByDni(dni);
        verify(membershipMapper).entityToDTO(expectedMemberships);
    }

    @Test
    void getMembershipsByDni_whenNoMembershipsExistsForUser_thenThrowNotFound()
    {
        // Given
        String dni = "88888888";

        // When
        when(membershipRepository.findAllByDni(dni)).thenReturn(new ArrayList<>());

        // Then
        assertThrows(MembershipNotFoundException.class, () -> membershipService.getAllByDni(dni));

        verify(membershipRepository).findAllByDni(dni);
    }

    @Test
    void getLastByDni_whenMembershipExists_thenReturnMembership()
    {
        // Given
        String dni = "87654321";
        MembershipEntity expectedMembership = MembershipFactory.userRegisteredMembership();
        MembershipResponseDTO expectedDTO = MembershipFactory.userNotRegisteredMembershipDTO();

        // When
        when(membershipRepository.findLastByDni(dni)).thenReturn(Optional.of(expectedMembership));
        when(membershipMapper.entityToDTO(expectedMembership)).thenReturn(expectedDTO);

        MembershipResponseDTO result = membershipService.getLastByDni(dni);

        // Then
        assertEquals(expectedDTO, result);

        verify(membershipRepository).findLastByDni(dni);
        verify(membershipMapper).entityToDTO(expectedMembership);
    }

    @Test
    void getLastByDni_whenDoesNotExist_thenThrowException()
    {
        // Given
        String dni = "88888888";

        // When
        when(membershipRepository.findLastByDni(dni)).thenReturn(Optional.empty());

        // Then
        assertThrows(MembershipNotFoundException.class, () -> membershipService.getLastByDni(dni));

        verify(membershipRepository).findLastByDni(dni);
    }

    @Test
    void getActiveMemberships_whenActiveMembershipsExist_thenReturnActiveMemberships()
    {
        // Given
        List<MembershipEntity> expectedMemberships = MembershipFactory.activeMembershipsList();
        List<MembershipResponseDTO> expectedDTOs = MembershipFactory.activeMembershipsListDTO();

        // When
        when(membershipRepository.findAllByStatus(MembershipStatus.ACTIVE)).thenReturn(expectedMemberships);
        when(membershipMapper.entityToDTO(expectedMemberships)).thenReturn(expectedDTOs);

        List<MembershipResponseDTO> result = membershipService.getAllActive();

        // Then
        assertEquals(expectedDTOs, result);
        assertTrue(result.stream().allMatch(m ->
                m.status().equals(MembershipStatus.ACTIVE)
        ));

        verify(membershipRepository).findAllByStatus(MembershipStatus.ACTIVE);
        verify(membershipMapper).entityToDTO(expectedMemberships);
    }

    @Test
    void getActiveMemberships_whenActiveMembershipsDoNotExist_thenReturnEmptyList()
    {
        // When
        when(membershipRepository.findAllByStatus(MembershipStatus.ACTIVE)).thenReturn(new ArrayList<>());
        when(membershipMapper.entityToDTO(anyList())).thenReturn(new ArrayList<>());

        List<MembershipResponseDTO> result = membershipService.getAllActive();

        // Then
        assertTrue(result.isEmpty());

        verify(membershipRepository).findAllByStatus(MembershipStatus.ACTIVE);
        verify(membershipMapper).entityToDTO(anyList());
    }

    @Test
    void dailyDeactivation_whenMembershipsExist_thenDailyDeactivation()
    {
        // Given
        LocalDate actualDate = LocalDate.now();
        List<MembershipEntity> expectedMemberships = List.of(MembershipFactory.expiredMembership());

        // When
        when(membershipRepository.findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, actualDate)).thenReturn(expectedMemberships);
        when(membershipRepository.saveAll(anyList())).then(invocation -> invocation.getArgument(0));

        ArgumentCaptor<List<MembershipEntity>> captor = ArgumentCaptor.forClass(List.class);

        membershipService.dailyDeactivation();

        // Then
        verify(membershipRepository).saveAll(captor.capture());

        assertTrue(captor.getValue().stream().allMatch(m ->
                m.getStatus().equals(MembershipStatus.INACTIVE)
        ));

        verify(membershipRepository).findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, actualDate);
        verify(whatsAppService).sendMessage(anyString(), anyString());
    }

    @Test
    void dailyDeactivation_whenExpiredMembershipsDoNotExist_thenDoNothing()
    {
        // Given
        LocalDate actualDate = LocalDate.now();

        // When
        when(membershipRepository.findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, actualDate)).thenReturn(new ArrayList<>());

        membershipService.dailyDeactivation();

        // Then
        verify(membershipRepository).findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, actualDate);
        verifyNoInteractions(whatsAppService);
        verify(membershipRepository).saveAll(new ArrayList<>());
    }

    @Test
    void updateStatusById_whenMembershipExists_thenReturnMembershipUpdated()
    {
        // Given
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.INACTIVE);
        MembershipEntity expectedMembership = MembershipFactory.userRegisteredMembership();
        MembershipEntity expectedMembershipUpdated = MembershipFactory.userRegisteredMembership();
        expectedMembershipUpdated.setStatus(MembershipStatus.INACTIVE);

        MembershipResponseDTO expectedMembershipUpdatedDTO = new MembershipResponseDTO(
                expectedMembershipUpdated.getId(),
                expectedMembershipUpdated.getUser().getId(),
                expectedMembershipUpdated.getDni(),
                expectedMembershipUpdated.getStatus(),
                expectedMembershipUpdated.getType(),
                expectedMembershipUpdated.getPaymentMethod(),
                expectedMembershipUpdated.getNextPaymentDate(),
                expectedMembershipUpdated.getNextPaymentDate(),
                expectedMembershipUpdated.getCreatedBy(),
                expectedMembershipUpdated.getLastModifiedBy()
        );

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.of(expectedMembership));
        when(membershipRepository.save(any(MembershipEntity.class))).thenReturn(expectedMembershipUpdated);
        when(membershipMapper.entityToDTO(expectedMembershipUpdated)).thenReturn(expectedMembershipUpdatedDTO);


        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        MembershipResponseDTO result = membershipService.updateStatusById(id, requestDTO);

        // Then
        verify(membershipRepository).save(captor.capture());

        assertEquals(requestDTO.status(), captor.getValue().getStatus());
        assertEquals(expectedMembershipUpdatedDTO, result);

        verify(membershipRepository).findById(id);
    }

    @Test
    void updateStatusById_whenMembershipDoesNotExist_thenThrowException()
    {
        // Given
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.empty());

        // Then
        assertThrows(MembershipNotFoundException.class, () -> membershipService.updateStatusById(id, requestDTO));

        verify(membershipRepository).findById(id);
        verifyNoMoreInteractions(membershipRepository);
    }

    @Test
    void updateStatusById_WhenMembershipAlreadyHasRequestedStatus_ThenDoNothing()
    {
        // Given
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);
        MembershipEntity expectedMembership = MembershipFactory.userRegisteredMembership();
        MembershipResponseDTO expectedMembershipDTO = MembershipFactory.userRegisteredMembershipDTO();

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.of(expectedMembership));
        when(membershipMapper.entityToDTO(expectedMembership)).thenReturn(expectedMembershipDTO);

        MembershipResponseDTO result = membershipService.updateStatusById(id, requestDTO);

        // Then
        assertEquals(requestDTO.status(), result.status());

        verify(membershipRepository).findById(id);
        verifyNoMoreInteractions(membershipRepository);
    }

    @Test
    void deleteById_whenMembershipExists_thenDeleteMembership()
    {
        // Given
        int id = 1;
        MembershipEntity expectedMembership = MembershipFactory.userRegisteredMembership();

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.of(expectedMembership));

        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        membershipService.deleteMembershipById(id);

        // Then
        verify(membershipRepository).delete(captor.capture());

        assertEquals(expectedMembership, captor.getValue());

        verify(membershipRepository).findById(id);
    }

    @Test
    void deleteById_whenMembershipDoesNotExist_thenThrowException()
    {
        // Given
        int id = -1;

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.empty());

        // Then
        assertThrows(MembershipNotFoundException.class, () -> membershipService.deleteMembershipById(id));

        verify(membershipRepository).findById(id);
        verifyNoMoreInteractions(membershipRepository);
    }

    @Test
    void deleteAllByDni_whenMembershipsExist_thenDeleteMemberships()
    {
        // Given
        String dni = "87654321";
        List<MembershipEntity> expectedMemberships = List.of(
                MembershipFactory.userRegisteredMembership(),
                MembershipFactory.expiredMembership()
        );

        // When
        when(membershipRepository.findAllByDni(dni)).thenReturn(expectedMemberships);

        membershipService.deleteAllByDni(dni);

        // Then
        assertEquals(2, expectedMemberships.size());
        assertEquals(dni, expectedMemberships.get(0).getDni());
        assertEquals(dni, expectedMemberships.get(1).getDni());

        verify(membershipRepository).findAllByDni(dni);
        verify(membershipRepository, times(2)).delete(any(MembershipEntity.class));
    }

    @Test
    void deleteAllByDni_whenMembershipsDoNotExist_thenThrowException()
    {
        // Given
        String dni = "99999999";

        // When
        when(membershipRepository.findAllByDni(dni)).thenReturn(new ArrayList<>());

        // Then
        assertThrows(MembershipNotFoundException.class, () -> membershipService.deleteAllByDni(dni));

        verify(membershipRepository).findAllByDni(dni);
        verifyNoMoreInteractions(membershipRepository);
    }

    @Test
    void sendExpiryReminder_whenMembershipExpiresInOneDay_thenSendWhatsappMessage()
    {
        // Given
        MembershipEntity membership = MembershipFactory.userRegisteredMembership();
        membership.setNextPaymentDate(LocalDate.now().plusDays(1));
        List<MembershipEntity> expectedMemberships = List.of(membership);

        // When
        when(membershipRepository.findAllByStatusAndNextPaymentDate(MembershipStatus.ACTIVE, LocalDate.now().plusDays(1)))
                .thenReturn(expectedMemberships);

        membershipService.sendExpiryReminder();

        // Then
        assertEquals(membership.getNextPaymentDate(), LocalDate.now().plusDays(1));
        assertNotNull(membership.getUser().getPhoneNumber());

        verify(membershipRepository).findAllByStatusAndNextPaymentDate(MembershipStatus.ACTIVE, LocalDate.now().plusDays(1));
        verify(whatsAppService).sendMessage(anyString(), anyString());
    }
}
